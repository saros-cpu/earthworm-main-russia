import type { $Fetch } from "ofetch";

import { useRuntimeConfig } from "#app";
import { ofetch } from "ofetch";

let csrfTokenValue: string | null = null;

function isStateChanging(method: string | undefined): boolean {
  return ["POST", "PUT", "PATCH", "DELETE"].includes((method || "GET").toUpperCase());
}

export function csrfRequestHeaders(): Record<string, string> {
  return csrfTokenValue ? { "X-XSRF-TOKEN": csrfTokenValue } : {};
}

let http: $Fetch;
export function setupHttp() {
  if (http) return http;

  const config = useRuntimeConfig();
  const baseURL = config.public.apiBase as string;

  http = ofetch.create({
    baseURL,
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    onRequest({ options }) {
      if (isStateChanging(options.method as string | undefined)) {
        options.headers = { ...options.headers, ...csrfRequestHeaders() };
      }
    },
    async onResponseError({ response }) {
      const { message } = response._data ?? {};
      if (Array.isArray(message)) {
        message.forEach((item) => {
          httpStatusErrorHandler?.(item, response.status);
        });
      } else if (message) {
        httpStatusErrorHandler?.(message, response.status);
      }
      return Promise.reject(response._data);
    },
    retry: 0,
  });
}

export async function initializeCsrf() {
  const data = await getHttp()<{ token: string }>("/auth/csrf", { method: "get" });
  csrfTokenValue = data.token;
}

type HttpStatusErrorHandler = (message: string, statusCode: number) => void;
let httpStatusErrorHandler: HttpStatusErrorHandler;

export function injectHttpStatusErrorHandler(handler: HttpStatusErrorHandler) {
  httpStatusErrorHandler = handler;
}

export function getHttp() {
  if (!http) {
    throw new Error("HTTP client not initialized. Call setupHttp first.");
  }
  return http;
}
