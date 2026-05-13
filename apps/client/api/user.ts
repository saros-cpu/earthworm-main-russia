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
  // 测试模式：直接从后端获取用户信息，无需 Logto
  const extraInfo = await http<UserApiResponse>("/user", { method: "get" }).catch(() => ({
    membership: { isMember: false, details: null },
  }));

  return {
    iss: "local-dev",
    sub: "dev-user-001",
    aud: "local-dev",
    exp: Math.floor(Date.now() / 1000) + 86400,
    iat: Math.floor(Date.now() / 1000),
    id: "dev-user-001",
    username: "dev-user",
    primaryEmail: "dev@example.com",
    avatar: "",
    picture: "",
    ...extraInfo,
  } as User;
}
