import assert from "node:assert/strict";
import { request } from "node:http";
import { test } from "node:test";

import { createFrontendServer, resolveStaticFile } from "./prod-frontend-server.mjs";

function get(server, path, headers = {}) {
  const address = server.address();
  return new Promise((resolve, reject) => {
    const req = request(
      {
        host: "127.0.0.1",
        port: address.port,
        path,
        headers,
      },
      (res) => {
        let body = "";
        res.setEncoding("utf8");
        res.on("data", (chunk) => {
          body += chunk;
        });
        res.on("end", () => resolve({ status: res.statusCode, body }));
      },
    );
    req.on("error", reject);
    req.end();
  });
}

function listen(server) {
  return new Promise((resolve, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", resolve);
  });
}

function close(server) {
  return new Promise((resolve, reject) => {
    server.close((error) => {
      if (error) reject(error);
      else resolve();
    });
  });
}

test("malformed encoded paths return 400 without stopping the frontend server", async () => {
  const server = createFrontendServer();
  await listen(server);
  try {
    const first = await get(server, "/%E0%A4%A");
    assert.equal(first.status, 400);
    assert.equal(first.body, "Bad request");
    assert.equal((await get(server, "/%E0%A4%A")).status, 400);
  } finally {
    await close(server);
  }
});

test("encoded traversal does not resolve outside the public directory", () => {
  const resolvedFile = resolveStaticFile("/..%2fpublic-secret%2findex.html");
  assert.deepEqual(resolvedFile, { status: 403, message: "Forbidden" });
});

test("invalid host headers cannot interrupt static responses", async () => {
  const server = createFrontendServer();
  await listen(server);
  try {
    const response = await get(server, "/_nuxt/builds/meta/test.json", { Host: "[" });
    assert.equal(response.status, 200);
    assert.equal((await get(server, "/%E0%A4%A")).status, 400);
  } finally {
    await close(server);
  }
});
