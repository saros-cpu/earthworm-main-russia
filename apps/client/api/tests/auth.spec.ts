import { describe, expect, it } from "vitest";

import { clearAuth, isAuthenticated, safeSignInTarget, setStoredUser } from "../auth";

describe("auth", () => {
  it("should use stored profile as a browser-side session hint", () => {
    clearAuth();
    expect(isAuthenticated()).toBe(false);

    setStoredUser({ userId: "user-1", username: "reader", nickname: "Reader" });
    expect(isAuthenticated()).toBe(true);
  });

  it("should remove legacy bearer tokens from local storage", () => {
    localStorage.setItem("ew_token", "legacy-token");

    clearAuth();

    expect(localStorage.getItem("ew_token")).toBeNull();
  });

  it("should restrict sign-in navigation to local application paths", () => {
    expect(safeSignInTarget("/dashboard?from=expired#login")).toBe("/dashboard?from=expired#login");
    expect(safeSignInTarget("//evil.example/phish")).toBe("/login");
    expect(safeSignInTarget("/\\evil.example/phish")).toBe("/login");
    expect(safeSignInTarget("https://evil.example/phish")).toBe("/login");
  });
});
