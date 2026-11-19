import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from "axios";
import { AuthResponse } from "../types/AuthResponse";

const BASE_URL = "http://localhost:8081";

const api = axios.create({
  baseURL: BASE_URL,
});

// Attach interceptor to this api
export const apiPrivate = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

type Tokens = { accessToken: string; refreshToken: string };
// Called with the new tokens after a refresh, or with null when the session has ended.
type TokensListener = (tokens: Tokens | null) => void;

const listeners = new Set<TokensListener>();
let refreshInFlight: Promise<string> | null = null;

export const onTokensChanged = (listener: TokensListener) => {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
};

const requestNewTokens = async (): Promise<string> => {
  try {
    // Read at call time, not from React state, so a rotated token is never sent twice.
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
      throw new Error("No refresh token available");
    }
    const { data } = await api.post<AuthResponse>(
      "/api/auth/refresh-token",
      {},
      { headers: { Authorization: `Bearer ${refreshToken}` } },
    );
    localStorage.setItem("token", data.access_token);
    localStorage.setItem("refreshToken", data.refresh_token);
    listeners.forEach((listener) =>
      listener({ accessToken: data.access_token, refreshToken: data.refresh_token }),
    );
    return data.access_token;
  } catch (error) {
    ["token", "refreshToken", "userEmail", "userRole"].forEach((key) =>
      localStorage.removeItem(key),
    );
    listeners.forEach((listener) => listener(null));
    throw error;
  }
};

// The server rotates refresh tokens and treats a reused one as stolen, so requests that get a
// 401 at the same time must share one refresh call instead of each sending the same token.
export const refreshTokens = (): Promise<string> => {
  if (!refreshInFlight) {
    refreshInFlight = requestNewTokens().finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
};

type RetriableRequest = InternalAxiosRequestConfig & { _retry?: boolean };

export const attachRefreshOn401 = (instance: AxiosInstance) => {
  instance.interceptors.response.use(undefined, async (error: AxiosError) => {
    const request = error.config as RetriableRequest | undefined;
    // Each request is retried at most once, and auth endpoints (including the refresh call
    // itself) are never retried, so a failing refresh cannot loop.
    if (
      error.response?.status !== 401 ||
      !request ||
      request._retry ||
      request.url?.includes("/api/auth/")
    ) {
      return Promise.reject(error);
    }
    request._retry = true;
    const accessToken = await refreshTokens();
    request.headers.Authorization = `Bearer ${accessToken}`;
    return instance(request);
  });
};

attachRefreshOn401(api);
attachRefreshOn401(apiPrivate);

export default api;
