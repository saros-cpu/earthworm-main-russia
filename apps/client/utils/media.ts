const YOUTUBE_HOSTS = new Set([
  "youtube.com",
  "www.youtube.com",
  "m.youtube.com",
  "youtube-nocookie.com",
  "www.youtube-nocookie.com",
]);
const SHORT_YOUTUBE_HOSTS = new Set(["youtu.be", "www.youtu.be"]);
const VIDEO_ID_PATTERN = /^[A-Za-z0-9_-]{1,64}$/;
const VIDEO_FILE_EXTENSION_PATTERN = /\.(mp4|avi|flv|wmv|webm)$/i;

export interface YouTubeEmbedOptions {
  autoplay?: boolean;
  enableJsApi?: boolean;
  origin?: string;
}

export function getYouTubeVideoId(source?: string): string | null {
  if (!source) return null;

  let url: URL;
  try {
    url = new URL(source);
  } catch {
    return null;
  }

  if (url.protocol !== "http:" && url.protocol !== "https:") {
    return null;
  }

  const host = url.hostname.toLowerCase();
  let videoId: string | null = null;
  if (YOUTUBE_HOSTS.has(host) && url.pathname === "/watch") {
    videoId = url.searchParams.get("v");
  } else if (YOUTUBE_HOSTS.has(host) && url.pathname.startsWith("/embed/")) {
    videoId = url.pathname.slice("/embed/".length).split("/")[0] || null;
  } else if (SHORT_YOUTUBE_HOSTS.has(host)) {
    videoId = url.pathname.replace(/^\/+/, "").split("/")[0] || null;
  }

  if (!videoId || !VIDEO_ID_PATTERN.test(videoId)) {
    return null;
  }
  return videoId;
}

export function getYouTubeEmbedUrl(
  source?: string,
  options: YouTubeEmbedOptions = {},
): string | null {
  const videoId = getYouTubeVideoId(source);
  if (!videoId) return null;

  const params = new URLSearchParams({
    autoplay: options.autoplay === false ? "0" : "1",
    rel: "0",
    playsinline: "1",
  });
  if (options.enableJsApi) {
    params.set("enablejsapi", "1");
  }
  if (options.origin) {
    params.set("origin", options.origin);
  }
  return `https://www.youtube-nocookie.com/embed/${videoId}?${params.toString()}`;
}

export function getMediaPlaybackUrl(source?: string): string {
  if (!source) return "";

  const youtubeEmbedUrl = getYouTubeEmbedUrl(source);
  if (youtubeEmbedUrl) return youtubeEmbedUrl;

  return getLocalMediaStreamUrl(source);
}

export function getLocalMediaStreamUrl(source?: string, querySuffix = ""): string {
  if (!source || /^https?:\/\//i.test(source)) return "";

  return `/api/backend/media/stream?path=${encodeURIComponent(source)}${querySuffix}`;
}

export function isVideoMediaSource(source?: string): boolean {
  if (!source) return false;
  if (getYouTubeEmbedUrl(source)) return true;
  if (/^https?:\/\//i.test(source)) return false;

  return VIDEO_FILE_EXTENSION_PATTERN.test(source.split(/[?#]/)[0]);
}
