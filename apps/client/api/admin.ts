import { getHttp } from "./http";
import type { PdfImportJobStatus } from "./course-pack";

export interface AdminCoursePack {
  id: string;
  title: string;
  description: string;
  cover: string;
  isFree: boolean;
  shareLevel: string;
  order: number;
  archived: boolean;
  courses?: AdminCourse[];
}

export interface AdminCourse {
  id: string;
  title: string;
  description: string;
  video?: string;
  order: number;
  coursePackId: string;
  archived: boolean;
  statements?: AdminStatement[];
}

export interface AdminVocabularyItem {
  word: string;
  meaning: string;
  partOfSpeech?: string;
  example?: string;
  exampleTranslation?: string;
}

export interface AdminStatement {
  id: string;
  order: number;
  sourceText: string;
  targetText: string;
  phonetic: string;
  archived: boolean;
  translation?: string;
  vocabulary?: AdminVocabularyItem[];
  grammarNote?: string;
  difficulty?: string;
  refinementMode?: string;
}

export interface CourseTopicSuggestion {
  topic: string;
  level: string;
  count: number;
  score: number;
  sourceNote: string;
  summary?: string;
  sources: Array<{
    title: string;
    url: string;
    note: string;
  }>;
}

export interface LocalPdfImportResult {
  directory: string;
  recursive: boolean;
  foundCount: number;
  createdCount: number;
  skippedCount: number;
  jobs: PdfImportJobStatus[];
  skipped: Array<{
    filename: string;
    reason: string;
  }>;
}

export interface VocabularyCoursePackResult {
  coursePackId: string;
  title: string;
  courseCount: number;
  wordCount: number;
  withMeaningCount: number;
}

export interface VocabularyPromptRefreshResult {
  coursePackId: string;
  statementCount: number;
  withMeaningCount: number;
}

export interface VocabularyEnrichResult {
  coursePackId: string;
  candidateCount: number;
  enrichedCount: number;
  aiAvailable: boolean;
}

export interface AdminUser {
  id: string;
  username: string;
  nickname: string;
  email: string;
  avatar: string;
  role: string;
  createdAt: string;
}

export interface VocabularyOrganizeResult {
  coursePackId: string;
  courseCount: number;
  statementCount: number;
  buckets: Array<{
    title: string;
    count: number;
  }>;
}

export function fetchAdminCoursePacks() {
  return getHttp()<AdminCoursePack[]>("/admin/course-packs");
}

export function fetchAdminCoursePack(id: string) {
  return getHttp()<AdminCoursePack>(`/admin/course-packs/${id}`);
}

export function updateAdminCoursePack(id: string, body: Partial<AdminCoursePack>) {
  return getHttp()<AdminCoursePack>(`/admin/course-packs/${id}`, {
    method: "put",
    body,
  });
}

export function deleteAdminCoursePack(id: string) {
  return getHttp()<boolean>(`/admin/course-packs/${id}`, {
    method: "delete",
  });
}

export function fetchAdminUsers() {
  return getHttp()<AdminUser[]>("/admin/users");
}

export function updateAdminUserRole(id: string, role: string) {
  return getHttp()<AdminUser>(`/admin/users/${id}/role`, {
    method: "put",
    body: { role },
  });
}

export function updateAdminUser(id: string, body: { nickname?: string; email?: string; avatar?: string; password?: string }) {
  return getHttp()<AdminUser>(`/admin/users/${id}`, {
    method: "put",
    body,
  });
}

export function deleteAdminUser(id: string) {
  return getHttp()<{ deleted: boolean }>(`/admin/users/${id}`, {
    method: "delete",
  });
}

export function createAdminCourse(coursePackId: string, body: Partial<AdminCourse>) {
  return getHttp()<AdminCourse>(`/admin/course-packs/${coursePackId}/courses`, {
    method: "post",
    body,
  });
}

export function generateAdminCourse(
  coursePackId: string,
  body: { topic: string; level: string; count: number },
) {
  return getHttp()<AdminCourse>(`/admin/course-packs/${coursePackId}/generate-course`, {
    method: "post",
    body,
  });
}

export function fetchCourseTopicSuggestions(keyword: string, online = false) {
  return getHttp()<CourseTopicSuggestion[]>(
    `/admin/course-topic-suggestions?keyword=${encodeURIComponent(keyword)}&online=${online}`,
  );
}

export function fetchAdminCourse(id: string) {
  return getHttp()<AdminCourse>(`/admin/courses/${id}`);
}

export function updateAdminCourse(id: string, body: Partial<AdminCourse>) {
  return getHttp()<AdminCourse>(`/admin/courses/${id}`, {
    method: "put",
    body,
  });
}

export function deleteAdminCourse(id: string) {
  return getHttp()<boolean>(`/admin/courses/${id}`, {
    method: "delete",
  });
}

export function createAdminStatement(courseId: string, body: Partial<AdminStatement>) {
  return getHttp()<AdminStatement>(`/admin/courses/${courseId}/statements`, {
    method: "post",
    body,
  });
}

export function updateAdminStatement(id: string, body: Partial<AdminStatement>) {
  return getHttp()<AdminStatement>(`/admin/statements/${id}`, {
    method: "put",
    body,
  });
}

export function deleteAdminStatement(id: string) {
  return getHttp()<boolean>(`/admin/statements/${id}`, {
    method: "delete",
  });
}

export function refineAdminStatement(id: string) {
  return getHttp()<AdminStatement>(`/admin/statements/${id}/refine`, {
    method: "post",
  });
}

export function fetchAdminPdfImportJobs(limit = 20) {
  return getHttp()<PdfImportJobStatus[]>(`/admin/pdf-import-jobs?limit=${limit}`);
}

export function deleteAdminPdfImportJob(jobId: string) {
  return getHttp()<boolean>(`/admin/pdf-import-jobs/${jobId}`, {
    method: "delete",
  });
}

export function createLocalPdfImportJobs(directory: string, recursive = true) {
  return getHttp()<LocalPdfImportResult>("/admin/pdf-import-jobs/local-directory", {
    method: "post",
    body: { directory, recursive },
  });
}

export function generateVocabularyCoursePack(body: { title?: string; limit?: number }) {
  return getHttp()<VocabularyCoursePackResult>("/admin/vocabulary-course-pack", {
    method: "post",
    body,
  });
}

export function refreshVocabularyPrompts(coursePackId: string) {
  return getHttp()<VocabularyPromptRefreshResult>(
    `/admin/course-packs/${coursePackId}/refresh-vocabulary-prompts`,
    {
      method: "post",
    },
  );
}

export function enrichVocabulary(coursePackId: string, limit = 40) {
  return getHttp()<VocabularyEnrichResult>(`/admin/course-packs/${coursePackId}/enrich-vocabulary`, {
    method: "post",
    body: { limit },
  });
}

export function organizeVocabularyCourses(coursePackId: string) {
  return getHttp()<VocabularyOrganizeResult>(
    `/admin/course-packs/${coursePackId}/organize-vocabulary-courses`,
    {
      method: "post",
    },
  );
}
