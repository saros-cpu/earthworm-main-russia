import { BadRequestException, Inject, Injectable } from "@nestjs/common";
import { and, eq, gte, lte, sql, sum } from "drizzle-orm";

import { userLearningActivities as userLearningActivitiesSchema } from "@earthworm/schema";
import { DB, DbType } from "../global/providers/db.provider";

@Injectable()
export class UserLearningActivityService {
  constructor(@Inject(DB) private db: DbType) {}

  async upsertActivity(
    userId: string,
    date: Date,
    activityType: string,
    duration: number,
    courseId?: string,
    metadata?: any,
  ) {
    if (duration < 0) {
      throw new BadRequestException("Duration cannot be negative");
    }

    // MySQL date 字段需要传入 Date 对象（drizzle-orm/mysql-core 的 date 类型使用 Date）
    await this.db
      .insert(userLearningActivitiesSchema)
      .values({
        userId,
        date,
        activityType,
        duration,
        courseId,
        metadata,
      })
      .onDuplicateKeyUpdate({
        set: {
          duration: sql`${userLearningActivitiesSchema.duration} + ${duration}`,
          updatedAt: new Date(),
        },
      });

    return true;
  }

  async getDailyTotalTime(userId: string, activityType: string, startDate?: Date, endDate?: Date) {
    const conditions: any[] = [
      eq(userLearningActivitiesSchema.userId, userId),
      eq(userLearningActivitiesSchema.activityType, activityType),
    ];

    if (startDate) {
      conditions.push(gte(userLearningActivitiesSchema.date, startDate));
    }
    if (endDate) {
      conditions.push(lte(userLearningActivitiesSchema.date, endDate));
    }

    const result = await this.db
      .select({
        date: userLearningActivitiesSchema.date,
        totalDuration: sum(userLearningActivitiesSchema.duration),
      })
      .from(userLearningActivitiesSchema)
      .where(and(...conditions))
      .groupBy(userLearningActivitiesSchema.date);

    return result.map((item) => ({
      date: item.date,
      duration: Number(item.totalDuration) || 0,
    }));
  }

  async getTotalLearningTime(
    userId: string,
    activityType: string,
    startDate?: Date,
    endDate?: Date,
  ) {
    const conditions: any[] = [
      eq(userLearningActivitiesSchema.userId, userId),
      eq(userLearningActivitiesSchema.activityType, activityType),
    ];

    if (startDate) {
      conditions.push(gte(userLearningActivitiesSchema.date, startDate));
    }
    if (endDate) {
      conditions.push(lte(userLearningActivitiesSchema.date, endDate));
    }

    const result = await this.db
      .select({ totalDuration: sum(userLearningActivitiesSchema.duration) })
      .from(userLearningActivitiesSchema)
      .where(and(...conditions));

    return Number(result[0]?.totalDuration || 0);
  }
}
