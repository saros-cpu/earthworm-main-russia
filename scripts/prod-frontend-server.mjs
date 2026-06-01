import { createReadStream, existsSync, statSync } from "node:fs";
import { createServer, request as httpRequest } from "node:http";
import { extname, isAbsolute, join, relative, resolve, sep } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const root = fileURLToPath(new URL("..", import.meta.url));
const publicDir = join(root, "apps", "client", ".output", "public");
const backendHost = process.env.BACKEND_HOST || "127.0.0.1";
const backendPort = Number(process.env.BACKEND_PORT || 8080);
const host = process.env.NITRO_HOST || process.env.HOST || "0.0.0.0";
const port = Number(process.env.NITRO_PORT || process.env.PORT || 3000);

const mimeTypes = new Map([
  [".html", "text/html; charset=utf-8"],
  [".js", "text/javascript; charset=utf-8"],
  [".mjs", "text/javascript; charset=utf-8"],
  [".css", "text/css; charset=utf-8"],
  [".json", "application/json; charset=utf-8"],
  [".png", "image/png"],
  [".jpg", "image/jpeg"],
  [".jpeg", "image/jpeg"],
  [".gif", "image/gif"],
  [".svg", "image/svg+xml"],
  [".ico", "image/x-icon"],
  [".webp", "image/webp"],
  [".woff", "font/woff"],
  [".woff2", "font/woff2"],
  [".ttf", "font/ttf"],
  [".mp3", "audio/mpeg"],
  [".mp4", "video/mp4"],
]);

function sendFile(res, filePath) {
  const ext = extname(filePath).toLowerCase();
  const stat = statSync(filePath);
  res.writeHead(200, {
    "Content-Type": mimeTypes.get(ext) || "application/octet-stream",
    "Content-Length": stat.size,
    "Cache-Control": ext === ".html" ? "no-cache" : "public, max-age=31536000, immutable",
  });
  createReadStream(filePath).pipe(res);
}

export function resolveStaticFile(pathname) {
  let decodedPath;
  try {
    decodedPath = decodeURIComponent(pathname);
  } catch {
    return { status: 400, message: "Bad request" };
  }

  const requestedPath = decodedPath.replace(/^([/\\])+/, "");
  const filePath = resolve(publicDir, requestedPath);
  const relativePath = relative(publicDir, filePath);
  if (
    relativePath === ".."
    || relativePath.startsWith(`..${sep}`)
    || isAbsolute(relativePath)
  ) {
    return { status: 403, message: "Forbidden" };
  }
  return { filePath };
}

function serveStatic(req, res) {
  const url = new URL(req.url || "/", "http://localhost");
  if (url.pathname.startsWith("/_nuxt/builds/meta/")) {
    const body = JSON.stringify({
      matcher: { static: {}, wildcard: {}, dynamic: {} },
      prerendered: [],
    });
    res.writeHead(200, {
      "Content-Type": "application/json; charset=utf-8",
      "Content-Length": Buffer.byteLength(body),
      "Cache-Control": "no-cache",
    });
    res.end(body);
    return;
  }

  const resolvedFile = resolveStaticFile(url.pathname);
  if (resolvedFile.status) {
    res.writeHead(resolvedFile.status, { "Content-Type": "text/plain; charset=utf-8" });
    res.end(resolvedFile.message);
    return;
  }
  let filePath = resolvedFile.filePath;

  if (existsSync(filePath) && statSync(filePath).isDirectory()) {
    filePath = join(filePath, "index.html");
  }

  if (existsSync(filePath) && statSync(filePath).isFile()) {
    sendFile(res, filePath);
    return;
  }

  const fallback = join(publicDir, "index.html");
  if (existsSync(fallback)) {
    sendFile(res, fallback);
    return;
  }

  res.writeHead(404);
  res.end("Not found");
}

function proxyBackend(req, res) {
  const originalUrl = req.url || "/";
  const targetPath = originalUrl.replace(/^\/api\/backend/, "") || "/";
  const proxyReq = httpRequest(
    {
      hostname: backendHost,
      port: backendPort,
      path: targetPath,
      method: req.method,
      headers: { ...req.headers, host: `${backendHost}:${backendPort}` },
    },
    (proxyRes) => {
      res.writeHead(proxyRes.statusCode || 502, proxyRes.headers);
      proxyRes.pipe(res);
    },
  );

  proxyReq.on("error", () => {
    res.writeHead(502, { "Content-Type": "application/json; charset=utf-8" });
    res.end(JSON.stringify({ error: "Backend proxy failed" }));
  });

  req.pipe(proxyReq);
}

export function createFrontendServer() {
  return createServer((req, res) => {
    if ((req.url || "").startsWith("/api/backend")) {
      proxyBackend(req, res);
      return;
    }
    serveStatic(req, res);
  });
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const server = createFrontendServer();
  server.listen(port, host, () => {
    console.log(`Frontend production server listening on http://${host}:${port}`);
    console.log(`Proxying /api/backend to http://${backendHost}:${backendPort}`);
  });
}
