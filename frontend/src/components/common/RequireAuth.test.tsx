// @vitest-environment jsdom
import { afterEach, describe, expect, it } from "vitest";
import { createRoot, Root } from "react-dom/client";
import { act } from "react-dom/test-utils";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from "../../context/AuthProvider";
import RequireAuth from "./RequireAuth";

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

let root: Root | null = null;

const renderProtectedRoute = (): HTMLElement => {
  const container = document.createElement("div");
  root = createRoot(container);
  act(() => {
    root!.render(
      <AuthProvider>
        <MemoryRouter initialEntries={["/"]}>
          <Routes>
            <Route path="/authentication" element={<p>login page</p>} />
            <Route element={<RequireAuth />}>
              <Route path="/" element={<p>protected page</p>} />
            </Route>
          </Routes>
        </MemoryRouter>
      </AuthProvider>,
    );
  });
  return container;
};

afterEach(() => {
  act(() => root?.unmount());
  localStorage.clear();
});

describe("RequireAuth", () => {
  it("redirects to the login page when there is no token", () => {
    expect(renderProtectedRoute().textContent).toBe("login page");
  });

  it("renders the protected page when the user is logged in", () => {
    localStorage.setItem("token", "access");
    localStorage.setItem("refreshToken", "refresh");
    localStorage.setItem("userEmail", "user@test.local");
    localStorage.setItem("userRole", "USER");

    expect(renderProtectedRoute().textContent).toBe("protected page");
  });
});
