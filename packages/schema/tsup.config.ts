import { defineConfig } from "tsup";

export default defineConfig({
  entry: ["src/index.ts"],
  splitting: false,
  sourcemap: true,
  clean: true,
  format: ["cjs", "esm"],
  dts: false,
  // Keep drizzle-orm external - let runtime require() resolve it
  external: ["drizzle-orm"],
});
