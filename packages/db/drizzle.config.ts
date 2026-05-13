import path from "path";
import type { Config } from "drizzle-kit";

import * as dotenv from "dotenv";

dotenv.config({ path: path.resolve(__dirname, "../../apps/api/.env") });

const dbUrl = new URL(process.env.DATABASE_URL || "");

console.log("drizzle config using DB:", dbUrl.host);

export default {
  schema: "../schema/src/schema/*",
  out: "./drizzle",
  dialect: "mysql",
  dbCredentials: {
    host: dbUrl.hostname,
    port: Number(dbUrl.port) || 3306,
    user: dbUrl.username,
    password: decodeURIComponent(dbUrl.password),
    database: dbUrl.pathname.replace(/^\//, ""),
  },
} satisfies Config;
