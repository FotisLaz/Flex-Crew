// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createRoot, Root } from "react-dom/client";
import { act } from "react-dom/test-utils";
import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import CheckInCard from "./CheckInCard";

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

type Reply = { status: number; data?: unknown };

let server: (config: InternalAxiosRequestConfig) => Reply;
let requests: InternalAxiosRequestConfig[];
let root: Root | null = null;
let container: HTMLElement;

const api = axios.create();
api.defaults.adapter = async (config: InternalAxiosRequestConfig): Promise<AxiosResponse> => {
  requests.push(config);
  const { status, data } = server(config);
  const response: AxiosResponse = { status, statusText: "", data, headers: {}, config };
  if (status >= 400) {
    throw new AxiosError(`Request failed with status ${status}`, undefined, config, null, response);
  }
  return response;
};

const nineToday = new Date();
nineToday.setHours(9, 5, 0, 0);
const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000);

const render = async () => {
  await act(async () => {
    root!.render(<CheckInCard api={api} employeeId={1} email="ana@test.local" />);
  });
};

const button = () => container.querySelector("button");
const alertText = () => container.querySelector('[role="alert"]')?.textContent;

const clickCheckIn = async () => {
  await act(async () => {
    button()!.click();
  });
};

beforeEach(() => {
  requests = [];
  container = document.createElement("div");
  root = createRoot(container);
});

afterEach(() => {
  act(() => root?.unmount());
});

describe("CheckInCard", () => {
  it("sends only the employee, not a time, and shows the time the server recorded", async () => {
    server = (config) =>
      config.method === "post"
        ? { status: 201, data: { id: 5, startTime: nineToday.toISOString() } }
        : { status: 200, data: [{ id: 4, startTime: yesterday.toISOString() }] };
    await render();

    expect(button()?.textContent).toBe("Check in");
    await clickCheckIn();

    const post = requests.find((r) => r.method === "post")!;
    expect(post.url).toBe("/api/records");
    expect(JSON.parse(post.data)).toEqual({ employee: { id: 1 } });
    expect(container.textContent).toContain("Checked in today at");
    expect(button()).toBeNull();
  });

  it("shows the server's message when the check-in is rejected", async () => {
    server = (config) =>
      config.method === "post"
        ? { status: 400, data: { message: "No schedule assigned" } }
        : { status: 200, data: [] };
    await render();

    await clickCheckIn();

    expect(alertText()).toBe("No schedule assigned");
    expect(button()?.textContent).toBe("Check in");
  });

  it("shows today's check-in instead of the button when there already is one", async () => {
    server = () => ({ status: 200, data: [{ id: 4, startTime: nineToday.toISOString() }] });
    await render();

    expect(requests[0].params).toEqual({ employeeEmail: "ana@test.local" });
    expect(container.textContent).toContain("Checked in today at");
    expect(button()).toBeNull();
  });
});
