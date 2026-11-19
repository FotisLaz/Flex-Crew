import { describe, expect, it } from "vitest";
import { AxiosError, AxiosHeaders, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import { getErrorMessage } from "./getErrorMessage";

const axiosErrorWithBody = (data: unknown) => {
  const config = { headers: new AxiosHeaders() } as InternalAxiosRequestConfig;
  const response: AxiosResponse = { status: 400, statusText: "", data, headers: {}, config };
  return new AxiosError("Request failed with status code 400", "ERR_BAD_REQUEST", config, null, response);
};

describe("getErrorMessage", () => {
  it("prefers the message the server sent", () => {
    expect(getErrorMessage(axiosErrorWithBody({ message: "Schedule is full" }), "fallback")).toBe(
      "Schedule is full",
    );
  });

  it("uses the error's own message when the server sent none", () => {
    expect(getErrorMessage(axiosErrorWithBody("plain text body"), "fallback")).toBe(
      "Request failed with status code 400",
    );
    expect(getErrorMessage(new Error("Network Error"), "fallback")).toBe("Network Error");
  });

  it("falls back when there is no usable message", () => {
    expect(getErrorMessage("boom", "fallback")).toBe("fallback");
    expect(getErrorMessage(new Error(""), "fallback")).toBe("fallback");
  });
});
