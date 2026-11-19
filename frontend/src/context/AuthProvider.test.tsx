// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createRoot, Root } from "react-dom/client";
import { act } from "react-dom/test-utils";
import { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import api from "../api/axiosConfig";
import useAuth from "../hooks/useAuth";
import { AuthProvider } from "./AuthProvider";

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

let root: Root;
let auth: ReturnType<typeof useAuth>;
let requests: InternalAxiosRequestConfig[];
let logoutStatus: number;

const CaptureAuth = () => {
  auth = useAuth();
  return null;
};

beforeEach(() => {
  requests = [];
  logoutStatus = 204;
  api.defaults.adapter = async (config: InternalAxiosRequestConfig): Promise<AxiosResponse> => {
    requests.push(config);
    const response: AxiosResponse = {
      status: logoutStatus,
      statusText: "",
      data: "",
      headers: {},
      config,
    };
    if (logoutStatus >= 400) {
      throw new AxiosError("Request failed", undefined, config, null, response);
    }
    return response;
  };
  localStorage.setItem("token", "access");
  localStorage.setItem("refreshToken", "refresh-1");
  localStorage.setItem("userEmail", "user@test.local");
  localStorage.setItem("userRole", "USER");
  root = createRoot(document.createElement("div"));
  act(() => {
    root.render(
      <AuthProvider>
        <CaptureAuth />
      </AuthProvider>,
    );
  });
});

afterEach(() => {
  act(() => root.unmount());
  localStorage.clear();
});

describe("logout", () => {
  it("revokes the refresh token on the server and clears the session", async () => {
    await act(async () => auth.logout());

    expect(requests).toHaveLength(1);
    expect(requests[0].url).toBe("/api/auth/logout");
    expect(requests[0].headers.get("Authorization")).toBe("Bearer refresh-1");
    expect(auth.isAuthenticated).toBe(false);
    expect(localStorage.getItem("refreshToken")).toBeNull();
  });

  it("clears the session even when the server call fails", async () => {
    logoutStatus = 500;

    await act(async () => auth.logout());

    expect(requests).toHaveLength(1);
    expect(auth.isAuthenticated).toBe(false);
    expect(localStorage.getItem("token")).toBeNull();
  });
});
