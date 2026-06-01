import type { SetupUserApiResponse } from "~/api/user";
import { type UserApiResponse } from "~/api/user";

export interface SetupUser extends SetupUserApiResponse {}

export type User = UserApiResponse & {
    avatar: string;
    id: string;
    username: string;
    nickname: string;
    role?: string;
  };
