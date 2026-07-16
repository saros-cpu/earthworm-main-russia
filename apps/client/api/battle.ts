import { getHttp } from "./http";

export function fetchCreateBattleRoom(coursePackId: string) {
  return getHttp()<{ roomId: string; status: string }>("/battle/create", {
    method: "POST",
    body: { coursePackId },
  });
}

export function fetchJoinBattleRoom(roomId: string) {
  return getHttp()<{ roomId: string; coursePackId: string; status: string }>("/battle/join", {
    method: "POST",
    body: { roomId },
  });
}

export function fetchSubmitBattleScore(roomId: string, score: number) {
  return getHttp()<{ roomId: string; status: string }>("/battle/submit", {
    method: "POST",
    body: { roomId, score },
  });
}

export function fetchBattleStatus(roomId: string) {
  return getHttp()<Record<string, any>>("/battle/status/" + roomId);
}

export function fetchBattleResult(roomId: string) {
  return getHttp()<Record<string, any>>("/battle/result/" + roomId);
}
