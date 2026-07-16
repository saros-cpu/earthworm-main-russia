export const fontFetch = (url: string | URL) => fetch(url, { cache: "force-cache" });
