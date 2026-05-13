import { Injectable, Logger } from "@nestjs/common";

import { UserEntity } from "../user/user.decorators";
import { UserService } from "../user/user.service";

// 定义周期枚举
export enum RankPeriod {
  WEEKLY = "weekly",
  MONTHLY = "monthly",
  YEARLY = "yearly",
}

export type RankPeriodAlias = "weekly" | "monthly" | "yearly";

/**
 * 测试模式：使用内存 Map 替代 Redis，无需启动 Redis 服务
 */
@Injectable()
export class RankService {
  private readonly logger = new Logger(RankService.name);

  // 内存排行榜 Map<period, Map<userId, count>>
  private readonly rankStore: Record<string, Map<string, number>> = {
    [RankPeriod.WEEKLY]: new Map(),
    [RankPeriod.MONTHLY]: new Map(),
    [RankPeriod.YEARLY]: new Map(),
  };

  constructor(private readonly userService: UserService) {}

  async userFinishCourse(userId: string) {
    const counts: Record<string, number> = {};
    for (const period of Object.values(RankPeriod)) {
      const store = this.rankStore[period];
      const current = store.get(userId) ?? 0;
      store.set(userId, current + 1);
      counts[period] = current + 1;
    }
    return counts;
  }

  async getRankList(user: UserEntity, period: RankPeriodAlias = RankPeriod.WEEKLY) {
    const store = this.rankStore[period];
    const sorted = Array.from(store.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 25)
      .map(([userId, count]) => ({ userId, count, username: "" }));

    let self: { userId: string; count: number; rank: number; username: string } | null = null;
    if (user?.userId) {
      const userCount = store.get(user.userId) ?? -1;
      const allSorted = Array.from(store.entries()).sort((a, b) => b[1] - a[1]);
      const rankIndex = allSorted.findIndex(([uid]) => uid === user.userId);
      self = {
        userId: user.userId,
        count: userCount,
        rank: rankIndex === -1 ? -1 : rankIndex + 1,
        username: "",
      };
    }

    return { self, list: sorted };
  }

  async resetRankList(period: RankPeriodAlias = RankPeriod.WEEKLY) {
    this.rankStore[period].clear();
    this.logger.verbose(`${period} 排行榜已重置: ${new Date()}`);
  }
}
