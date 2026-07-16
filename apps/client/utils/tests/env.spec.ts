import { describe, expect, it } from "vitest";

import { isDev, isProd } from "../env";

describe("env", () => {
  it("should detect Nuxt test environment", () => {
    // Nuxt test environment sets NODE_ENV=production
    expect(isProd()).toBe(true);
    expect(isDev()).toBe(false);
  });
});
