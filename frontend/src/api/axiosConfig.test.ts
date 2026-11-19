// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import api, { apiPrivate, onTokensChanged } from "./axiosConfig";
import { apiPrivate as apiV1Private } from "../services/api/apiClient";

type Reply = { status: number; data?: unknown };

let server: (config: InternalAxiosRequestConfig) => Reply;
let requests: InternalAxiosRequestConfig[] = [];
let unsubscribe = () => {};

const fakeAdapter = async (config: InternalAxiosRequestConfig): Promise<AxiosResponse> => {
  requests.push(config);
  const { status, data } = server(config);
  const response: AxiosResponse = { status, statusText: "", data, headers: {}, config };
  if (status >= 400) {
    throw new AxiosError(`Request failed with status ${status}`, undefined, config, null, response);
  }
  return response;
};

const authHeader = (config: InternalAxiosRequestConfig) => config.headers.get("Authorization");
const refreshRequests = () => requests.filter((r) => r.url === "/api/auth/refresh-token");

/** Rotates refresh-1 into new tokens; every other endpoint only accepts the new access token. */
const rotatingServer = (config: InternalAxiosRequestConfig): Reply => {
  if (config.url === "/api/auth/refresh-token") {
    return authHeader(config) === "Bearer refresh-1"
      ? { status: 200, data: { access_token: "new-access", refresh_token: "refresh-2" } }
      : { status: 401 };
  }
  return authHeader(config) === "Bearer new-access" ? { status: 200, data: "ok" } : { status: 401 };
};

const getEmployees = () =>
  apiPrivate.get("/api/employees", { headers: { Authorization: "Bearer old-access" } });

beforeEach(() => {
  api.defaults.adapter = fakeAdapter;
  apiPrivate.defaults.adapter = fakeAdapter;
  apiV1Private.defaults.adapter = fakeAdapter;
  requests = [];
  server = rotatingServer;
  localStorage.setItem("token", "old-access");
  localStorage.setItem("refreshToken", "refresh-1");
  localStorage.setItem("userEmail", "user@test.local");
  localStorage.setItem("userRole", "USER");
});

afterEach(() => {
  unsubscribe();
  localStorage.clear();
});

describe("refresh on 401", () => {
  it("refreshes once and retries the request with the new access token", async () => {
    const response = await getEmployees();

    expect(response.data).toBe("ok");
    expect(refreshRequests()).toHaveLength(1);
    expect(authHeader(refreshRequests()[0])).toBe("Bearer refresh-1");
  });

  it("stores both rotated tokens and tells subscribers", async () => {
    const seen: unknown[] = [];
    unsubscribe = onTokensChanged((tokens) => seen.push(tokens));

    await getEmployees();

    expect(localStorage.getItem("token")).toBe("new-access");
    expect(localStorage.getItem("refreshToken")).toBe("refresh-2");
    expect(seen).toEqual([{ accessToken: "new-access", refreshToken: "refresh-2" }]);
  });

  it("shares one refresh between parallel requests", async () => {
    const responses = await Promise.all([getEmployees(), getEmployees(), getEmployees()]);

    expect(responses.map((r) => r.data)).toEqual(["ok", "ok", "ok"]);
    expect(refreshRequests()).toHaveLength(1);
  });

  it("retries a request only once", async () => {
    server = (config) =>
      config.url === "/api/auth/refresh-token" ? rotatingServer(config) : { status: 401 };

    await expect(getEmployees()).rejects.toMatchObject({ response: { status: 401 } });
    expect(refreshRequests()).toHaveLength(1);
    expect(requests.filter((r) => r.url === "/api/employees")).toHaveLength(2);
  });

  it("ends the session when the refresh fails", async () => {
    localStorage.setItem("refreshToken", "revoked");
    const seen: unknown[] = [];
    unsubscribe = onTokensChanged((tokens) => seen.push(tokens));

    await expect(getEmployees()).rejects.toMatchObject({ response: { status: 401 } });

    expect(refreshRequests()).toHaveLength(1);
    expect(seen).toEqual([null]);
    expect(localStorage.getItem("token")).toBeNull();
    expect(localStorage.getItem("refreshToken")).toBeNull();
  });

  it("does not refresh for a 401 from an auth endpoint", async () => {
    await expect(api.post("/api/auth/authenticate", {})).rejects.toMatchObject({
      response: { status: 401 },
    });
    expect(refreshRequests()).toHaveLength(0);
  });

  it("does not refresh on 403", async () => {
    server = () => ({ status: 403 });

    await expect(getEmployees()).rejects.toMatchObject({ response: { status: 403 } });
    expect(refreshRequests()).toHaveLength(0);
  });

  it("also refreshes for the /api/v1 client", async () => {
    const response = await apiV1Private.get("/projects");

    expect(response.data).toBe("ok");
    expect(refreshRequests()).toHaveLength(1);
  });
});
