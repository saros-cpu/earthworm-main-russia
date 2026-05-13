import dotenv from "dotenv";
import path from "path";
import { drizzle } from "drizzle-orm/mysql2";
import mysql from "mysql2/promise";
import { sql } from "drizzle-orm";

dotenv.config({ path: path.resolve(__dirname, "../../apps/api/.env") });

const dbUrl = new URL(process.env.DATABASE_URL || "");
const password = decodeURIComponent(dbUrl.password);

async function main() {
  const connection = await mysql.createConnection({
    host: dbUrl.hostname,
    port: Number(dbUrl.port) || 3306,
    user: dbUrl.username,
    password: password,
    database: dbUrl.pathname.replace(/^\//, ""),
  });

  console.log("Connected to MySQL");

  // Read schema and push using drizzle
  const db = drizzle(connection);

  // Create tables manually based on schema
  const statements = [
    `CREATE TABLE IF NOT EXISTS course (
      id INT PRIMARY KEY,
      type VARCHAR(50),
      status VARCHAR(20),
      name VARCHAR(255),
      description TEXT,
      category VARCHAR(100),
      image VARCHAR(500),
      totalWords INT,
      totalLessons INT,
      totalPhrases INT,
      sort INT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`,
    // ... this approach is too manual
  ];

  await connection.end();
}

main().catch(console.error);
