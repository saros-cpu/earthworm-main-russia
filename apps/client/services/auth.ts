import { useRuntimeConfig } from "nuxt/app";

const TOKEN_KEY = "ew_token";
const USER_KEY = "ew_user";

function backendUrl(path: string): string {
  const base = useRuntimeConfig().public.apiBase as string;
  return `${base}${path}`;
}

export interface AuthUser {
  userId: string;
  username: string;
  nickname: string;
  avatar?: string;
  role?: string;
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function isAuthenticated(): boolean {
  return !!getToken();
}

export function getStoredUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function storeUser(user: AuthUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export async function login(username: string, password: string): Promise<AuthUser> {
  const res = await fetch(backendUrl("/auth/login"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const data = await res.json();
  if (data.error) throw new Error(data.error);
  setToken(data.token);
  const user: AuthUser = {
    userId: data.userId,
    username: data.username,
    nickname: data.nickname,
    role: data.role || "USER",
  };
  storeUser(user);
  return user;
}

export async function register(
  username: string,
  password: string,
  nickname: string,
): Promise<AuthUser> {
  const res = await fetch(backendUrl("/auth/register"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password, nickname }),
  });
  const data = await res.json();
  if (data.error) throw new Error(data.error);
  setToken(data.token);
  const user: AuthUser = {
    userId: data.userId,
    username: data.username,
    nickname: data.nickname,
    role: data.role || "USER",
  };
  storeUser(user);
  return user;
}

export function logout() {
  clearAuth();
  window.location.href = "/login";
}

export function signIn(callback?: string) {
  window.location.href = callback || "/login";
}

export function signOut() {
  clearAuth();
  window.location.href = "/login";
}
