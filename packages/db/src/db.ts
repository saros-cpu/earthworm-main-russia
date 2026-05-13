import path from "path";
import dotenv from "dotenv";
import { drizzle } from "drizzle-orm/mysql2";
import mysql from "mysql2/promise";

import { schemas } from "@earthworm/schema/dist";

const envName = process.env.NODE_ENV === "prod" ? ".env.prod" : ".env";
dotenv.config({ path: path.resolve(__dirname, `../../../apps/api/${envName}`) });

const dbUrl = new URL(process.env.DATABASE_URL ?? "");
const password = decodeURIComponent(dbUrl.password);

const pool = mysql.createPool({
  host: dbUrl.hostname,
  port: Number(dbUrl.port) || 3306,
  user: dbUrl.username,
  password,
  database: dbUrl.pathname.replace(/^\//, ""),
  connectionLimit: 10,
});

export const db = drizzle(pool, {
  schema: schemas,
  mode: "default",
});
