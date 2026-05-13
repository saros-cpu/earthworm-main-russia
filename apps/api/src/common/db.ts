import { Logger } from "@nestjs/common";
import { DefaultLogger, LogWriter } from "drizzle-orm";
import { drizzle } from "drizzle-orm/mysql2";
import * as mysql from "mysql2/promise";

import { schemas } from "@earthworm/schema";

let connection: mysql.Connection;

async function createConnection() {
  return mysql.createConnection(process.env.DATABASE_URL ?? "");
}

export async function endDB() {
  if (connection) {
    await connection.end();
    connection = null;
  }
}

export async function setupDB() {
  if (connection) return;

  const logger = new Logger("DB");

  class CustomDbLogWriter implements LogWriter {
    write(message: string) {
      logger.verbose(message);
    }
  }

  logger.debug(`Connecting to ${process.env.DATABASE_URL}`);
  logger.debug(`SECRET: ${process.env.SECRET}`);

  connection = await createConnection();

  return drizzle(connection, {
    schema: schemas,
    mode: "default",
    logger: new DefaultLogger({ writer: new CustomDbLogWriter() }),
  });
}
