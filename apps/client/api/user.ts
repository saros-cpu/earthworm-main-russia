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

export async function fetchSetupNewUser(data: { username: string; avatar: string }) {
  const http = getHttp();
  return (await http<SetupUserApiResponse>("/user/setup", {
    method: "post",
    body: data,
  })) as SetupUser;
}

export async function fetchCurrentUser() {
  const http = getHttp();
  const userInfo = await http<UserApiResponse>("/user", { method: "get" }).catch(() => null);
  const stored = getStoredUser();

  return {
    iss: "local-dev",
    sub: stored?.userId || "dev-user-001",
    aud: "local-dev",
    exp: Math.floor(Date.now() / 1000) + 86400,
    iat: Math.floor(Date.now() / 1000),
    id: stored?.userId || "dev-user-001",
    username: stored?.username || "dev-user",
    nickname: stored?.nickname || stored?.username || "dev-user",
    name: stored?.nickname || stored?.username || "dev-user",
    primaryEmail: "",
    avatar: stored?.avatar || "",
    picture: "",
    ...(userInfo || { membership: { isMember: false, details: null } }),
  } as User;
}

function getStoredUser(): {
  userId?: string;
  username?: string;
  nickname?: string;
  avatar?: string;
  role?: string;
} | null {
  try {
    const raw = localStorage.getItem("ew_user");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}
