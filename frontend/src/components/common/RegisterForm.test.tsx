// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createRoot, Root } from "react-dom/client";
import { act } from "react-dom/test-utils";
import { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import RegisterForm from "./RegisterForm";
import { registerUser } from "../../services/registerUser";

vi.mock("../../services/registerUser", () => ({ registerUser: vi.fn() }));

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

let root: Root | null = null;
let container: HTMLElement;

const fill = (id: string, value: string) => {
  const input = container.querySelector<HTMLInputElement>(`#${id}`)!;
  // React tracks the input's value itself, so set it through the native setter.
  Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, "value")!.set!.call(input, value);
  act(() => {
    input.dispatchEvent(new Event("input", { bubbles: true }));
  });
};

const fillRequiredFields = () => {
  fill("floating_email", "ana@test.local");
  fill("floating_names", "Ana");
  fill("floating_firstSurname", "Lopez");
  fill("floating_password", "Passw0rd!");
  fill("confirm_pwd", "Passw0rd!");
};

const submit = async () => {
  await act(async () => {
    container.querySelector("form")!.requestSubmit();
  });
};

const alertText = () => container.querySelector('[role="alert"]')?.textContent;

beforeEach(() => {
  vi.mocked(registerUser).mockReset();
  container = document.createElement("div");
  root = createRoot(container);
  act(() => root!.render(<RegisterForm onRegistered={() => {}} />));
});

afterEach(() => {
  act(() => root?.unmount());
});

describe("RegisterForm", () => {
  it("registers without a second surname", async () => {
    vi.mocked(registerUser).mockResolvedValue({
      access_token: "a",
      refresh_token: "r",
      role: "USER",
    });
    fillRequiredFields();

    await submit();

    expect(registerUser).toHaveBeenCalledWith("ana@test.local", "Ana", "Lopez", "", "Passw0rd!");
    expect(container.textContent).toContain("Account created");
  });

  it("names the missing required fields instead of failing silently", async () => {
    fill("floating_email", "ana@test.local");

    await submit();

    expect(alertText()).toBe(
      "Please fill in: Names, 1st Surname, Password, Confirm Password",
    );
    expect(registerUser).not.toHaveBeenCalled();
  });

  it("shows a message when the email is already registered", async () => {
    const config = { headers: {} } as InternalAxiosRequestConfig;
    const response = { status: 409, data: {}, config } as AxiosResponse;
    vi.mocked(registerUser).mockRejectedValue(
      new AxiosError("Conflict", undefined, config, null, response),
    );
    fillRequiredFields();

    await submit();

    expect(alertText()).toBe("This email is already registered");
  });
});
