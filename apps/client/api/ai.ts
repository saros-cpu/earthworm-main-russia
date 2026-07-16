import { getHttp } from "./http";

export interface AiExplainRequest {
  sentenceRussian: string;
  sentenceChinese: string;
  userAnswer: string;
}

export async function fetchAiGrammarExplanation(req: AiExplainRequest): Promise<string> {
  const http = getHttp();
  const question = [
    `我正在学习俄语，以下句子我答错了。`,
    `俄语句子：${req.sentenceRussian}`,
    `中文翻译：${req.sentenceChinese}`,
    `我的错误答案：${req.userAnswer}`,
    `请帮我分析错误原因，解释应该用什么语法规则。用中文回答，简洁明了，控制在 100 字以内。`,
  ].join("\n");

  const res = await http<{ answer: string }>("/ai/ask", {
    method: "post",
    body: { question },
  });
  return res.answer;
}

export async function fetchAiExampleSentences(word: string, language = "ru"): Promise<string[]> {
  const http = getHttp();
  const question = [
    `请为俄语单词 "${word}" 生成 3 个例句，每个句子要包含该单词的不同用法。`,
    `格式要求：每行一个例句，句末加中文翻译。`,
    `示例：Это мой новый дом. 这是我的新家。`,
  ].join("\n");

  const res = await http<{ answer: string }>("/ai/ask", {
    method: "post",
    body: { question },
  });
  return res.answer.split("\n").filter(Boolean);
}
