import { getHttp } from "./http";

const LEGACY_TOKEN_KEY = "ew_token";
const USER_KEY = "ew_user";

export interface AuthUser {
  userId: string;
  username: string;
  nickname: string;
  avatar?: string;
  role?: string;
}

export function clearLegacyToken() {
  localStorage.removeItem(LEGACY_TOKEN_KEY);
}

export function clearAuth() {
  clearLegacyToken();
  localStorage.removeItem(USER_KEY);
}

export function isAuthenticated(): boolean {
  return getStoredUser() !== null;
}

export function getStoredUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function setStoredUser(user: AuthUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export async function login(username: string, password: string): Promise<AuthUser> {
  const http = getHttp();
  const data = await http<Record<string, any>>("/auth/login", {
    method: "POST",
    body: { username, password },
  });
  clearLegacyToken();
  const user: AuthUser = {
    userId: data.userId,
    username: data.username,
    nickname: data.nickname,
    role: data.role || "USER",
  };
  setStoredUser(user);
  return user;
}

export async function register(
  username: string,
  password: string,
  nickname: string,
): Promise<AuthUser> {
  const http = getHttp();
  const data = await http<Record<string, any>>("/auth/register", {
    method: "POST",
    body: { username, password, nickname },
  });
  clearLegacyToken();
  const user: AuthUser = {
    userId: data.userId,
    username: data.username,
    nickname: data.nickname,
    role: data.role || "USER",
  };
  setStoredUser(user);
  return user;
}

async function clearBrowserSession() {
  try {
    await getHttp()("/auth/logout", { method: "post" });
  } catch {
  } finally {
    clearAuth();
  }
}

export function safeSignInTarget(callback?: string): string {
  if (!callback || !callback.startsWith("/")) {
    return "/login";
  }
  try {
    const target = new URL(callback, window.location.origin);
    if (target.origin !== window.location.origin) {
      return "/login";
    }
    return `${target.pathname}${target.search}${target.hash}`;
  } catch {
    return "/login";
  }
}

export function signIn(callback?: string) {
  window.location.href = safeSignInTarget(callback);
}

export async function signOut(target = "/login") {
  await clearBrowserSession();
  window.location.href = target;
}
