import type { MembershipType, SetupUser, User } from "~/types";
import { getHttp } from "./http";

export interface SetupUserApiResponse {
  avatar: string;
  username: string;
}

export interface UserApiResponse {
  membership: {
    details: {
      endDate: string;
      type: MembershipType;
      startDate: string;
    } | null;
    isMember: boolean;
  };
}

export interface AuthIdentityApiResponse {
  userId: string;
  username: string;
  nickname: string;
  avatar?: string;
  role?: string;
}

export interface AuthSessionApiResponse extends Partial<AuthIdentityApiResponse> {
  authenticated: boolean;
}

export async function fetchSetupNewUser(data: { username: string; avatar: string }) {
  const http = getHttp();
  return (await http<SetupUserApiResponse>("/user/setup", {
    method: "post",
    body: data,
  })) as SetupUser;
}

export async function fetchAuthenticatedIdentity() {
  return getHttp()<AuthIdentityApiResponse>("/auth/me", { method: "get" });
}

export async function fetchBrowserSession() {
  return getHttp()<AuthSessionApiResponse>("/auth/session", { method: "get" });
}

export async function fetchCurrentUser() {
  const http = getHttp();
  const identity = await fetchAuthenticatedIdentity();
  const userInfo = await http<UserApiResponse>("/user", { method: "get" }).catch(() => null);

  return {
    iss: "backend",
    sub: identity.userId,
    aud: "backend",
    exp: Math.floor(Date.now() / 1000) + 86400,
    iat: Math.floor(Date.now() / 1000),
    id: identity.userId,
    username: identity.username,
    nickname: identity.nickname || identity.username,
    name: identity.nickname || identity.username,
    primaryEmail: "",
    avatar: identity.avatar || "",
    picture: "",
    role: identity.role || "USER",
    ...(userInfo || { membership: { isMember: false, details: null } }),
  } as User;
}
