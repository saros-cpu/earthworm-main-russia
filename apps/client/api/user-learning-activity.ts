import type { UserLearningDailyTime } from "~/types/models/user-learning-activity";
import { getHttp } from "./http";

export interface LearningTimeApiResponse {
  date: string;
  duration: number;
}

interface DailyStatsResponse {
  date: string;
  totalTimeSeconds?: number;
}

interface TotalStatsResponse {
  totalTimeSeconds?: number;
}

function toLearningTime(stat: DailyStatsResponse): LearningTimeApiResponse {
  return {
    date: stat.date,
    duration: Number(stat.totalTimeSeconds || 0),
  };
}

export async function fetchTodayLearningTime() {
  const http = getHttp();
  const today = new Date().toISOString().split("T")[0];
  const learningTimeList = await http<DailyStatsResponse[]>("/stats/daily", {
    method: "get",
    params: {
      startDate: today,
      endDate: today,
    },
  });

  if (learningTimeList.length === 0) {
    return 0;
  }

  return Number(learningTimeList[0].totalTimeSeconds || 0);
}

export async function fetchAllLearningTime() {
  const http = getHttp();
  const end = new Date();
  const start = new Date();
  start.setDate(start.getDate() - 365);
  const stats = await http<DailyStatsResponse[]>("/stats/daily", {
    method: "get",
    params: {
      startDate: start.toISOString().split("T")[0],
      endDate: end.toISOString().split("T")[0],
    },
  });
  return stats.map(toLearningTime) as UserLearningDailyTime[];
}

/**获取总的学习时长 */
export async function fetchTotalLearningTime() {
  const http = getHttp();
  const result = await http<TotalStatsResponse>("/stats/total", {
    method: "get",
  });

  return Number(result.totalTimeSeconds || 0);
}
