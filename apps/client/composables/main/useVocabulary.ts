import { toast } from "vue-sonner";
import { fetchAddVocabulary } from "~/api/learning";

export function useVocabulary() {
  async function addWordsFromStatement(english: string, statementId?: string) {
    if (!english) return;
    const words = english.split(" ").filter((w) => /[а-яё]/i.test(w));
    if (words.length === 0) {
      toast.info("没有可收藏的俄语单词");
      return;
    }
    let added = 0;
    for (const word of words) {
      try {
        await fetchAddVocabulary({ word, sourceStatementId: statementId });
        added++;
      } catch (_) {}
    }
    toast.success(`已添加 ${added} 个单词到生词本`);
  }

  return { addWordsFromStatement };
}
