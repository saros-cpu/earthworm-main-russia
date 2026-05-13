import type { $Fetch } from "ofetch";

import { useRuntimeConfig } from "#app";
import { ofetch } from "ofetch";

function getAuthToken(): string | null {
  if (typeof window !== "undefined") {
    return localStorage.getItem("ew_token");
  }
  return null;
}

let http: $Fetch;
export function setupHttp() {
  if (http) return http;

  const config = useRuntimeConfig();
  const baseURL = config.public.apiBase as string;

  http = ofetch.create({
    baseURL,
    headers: { "Content-Type": "application/json" },
    onRequest({ options }) {
      const token = getAuthToken();
      if (token) {
        options.headers = { ...options.headers, Authorization: `Bearer ${token}` };
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
    retry: 3,
    retryDelay: 1000,
  });
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
