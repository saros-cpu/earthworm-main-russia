import type { CourseApiResponse } from "./course";
import type { CoursePack, CoursePacksItem } from "~/types";
import { useRuntimeConfig } from "#app";
import { ofetch } from "ofetch";
import { csrfRequestHeaders, getHttp } from "./http";

export type CoursePacksItemApiResponse = {
  id: string;
  title: string;
  isFree: boolean;
  description: string;
  cover: string;
};

export interface CoursePackApiResponse {
  id: string;
  title: string;
  description: string;
  isFree: boolean;
  cover: string;
  courses: CourseApiResponse[];
}

export interface PdfImportResponse {
  coursePackId: string;
  title: string;
  courseCount: number;
  statementCount: number;
  wordCount?: number;
  letterCount?: number;
  refinedCount?: number;
  importMode: "text" | "ocr";
}

export interface PdfImportStatus {
  textPdfAvailable: boolean;
  ocrAvailable: boolean;
  aiRefinementAvailable: boolean;
  aiProvider: string;
  aiModel: string;
  ocrMessage: string;
}

export interface PdfImportJobResponse {
  jobId: string;
  status: "queued" | "running" | "completed" | "failed";
  progress: number;
  message: string;
}

export interface PdfImportJobStatus extends PdfImportJobResponse {
  title?: string;
  filename?: string;
  coursePackId?: string;
  errorMessage?: string;
  result?: PdfImportResponse;
  createdAt?: string;
  updatedAt?: string;
}

export async function fetchCoursePacks() {
  const http = getHttp();
  return (await http<CoursePacksItemApiResponse[]>("/course-pack", {
    method: "get",
  })) as CoursePacksItem[];
}

export async function fetchCoursePack(coursePackId: string) {
  const http = getHttp();
  return (await http<CoursePackApiResponse>(`/course-pack/${coursePackId}`, {
    method: "get",
  })) as CoursePack;
}

export async function uploadPdfCoursePack(file: File, title?: string) {
  const config = useRuntimeConfig();
  const formData = new FormData();
  formData.append("file", file);
  if (title) {
    formData.append("title", title);
  }

  return await ofetch<PdfImportResponse>("/course-pack/import/pdf", {
    baseURL: config.public.apiBase as string,
    method: "POST",
    credentials: "include",
    headers: csrfRequestHeaders(),
    body: formData,
  });
}

export async function createPdfImportJob(file: File, title?: string) {
  const config = useRuntimeConfig();
  const formData = new FormData();
  formData.append("file", file);
  if (title) {
    formData.append("title", title);
  }

  return await ofetch<PdfImportJobResponse>("/course-pack/import/pdf/jobs", {
    baseURL: config.public.apiBase as string,
    method: "POST",
    credentials: "include",
    headers: csrfRequestHeaders(),
    body: formData,
  });
}

export async function fetchPdfImportJob(jobId: string) {
  const http = getHttp();
  return await http<PdfImportJobStatus>(`/course-pack/import/pdf/jobs/${jobId}`, {
    method: "get",
  });
}

export async function fetchPdfImportStatus() {
  const http = getHttp();
  return await http<PdfImportStatus>("/course-pack/import/pdf/status", {
    method: "get",
  });
}
