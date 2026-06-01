import { describe, expect, it } from "vitest";

import { getLocalMediaStreamUrl, getMediaPlaybackUrl, getYouTubeEmbedUrl, isVideoMediaSource } from "../media";

describe("getYouTubeEmbedUrl", () => {
  it("normalizes supported YouTube addresses to a trusted embed URL", () => {
    expect(getYouTubeEmbedUrl("https://www.youtube.com/watch?v=abc_123-xy"))
      .toBe("https://www.youtube.com/embed/abc_123-xy?autoplay=1");
    expect(getYouTubeEmbedUrl("https://youtu.be/abc_123-xy"))
      .toBe("https://www.youtube.com/embed/abc_123-xy?autoplay=1");
    expect(getYouTubeEmbedUrl("https://youtube.com/embed/abc_123-xy"))
      .toBe("https://www.youtube.com/embed/abc_123-xy?autoplay=1");
  });

  it("rejects untrusted or script-like values that mention YouTube", () => {
    expect(getYouTubeEmbedUrl("https://example.test/youtube.com/watch?v=abc_123-xy")).toBeNull();
    expect(getYouTubeEmbedUrl("javascript:alert(1)//youtube.com/watch?v=abc_123-xy")).toBeNull();
    expect(getYouTubeEmbedUrl("https://youtube.com.evil.test/watch?v=abc_123-xy")).toBeNull();
  });
});

describe("getMediaPlaybackUrl", () => {
  it("serves local media and trusted YouTube sources only", () => {
    expect(getMediaPlaybackUrl("courses/lesson-1.avi"))
      .toBe("/api/backend/media/stream?path=courses%2Flesson-1.avi");
    expect(getMediaPlaybackUrl("https://youtu.be/abc_123-xy"))
      .toBe("https://www.youtube.com/embed/abc_123-xy?autoplay=1");
    expect(getMediaPlaybackUrl("https://cdn.example.test/lesson.mp4")).toBe("");
    expect(getMediaPlaybackUrl("https://example.test/youtube.com/watch?v=abc_123-xy")).toBe("");
  });
});

describe("getLocalMediaStreamUrl", () => {
  it("allows locally proxied media but rejects every remote media source", () => {
    expect(getLocalMediaStreamUrl("courses/audio-1.mp3", "&v=mp4"))
      .toBe("/api/backend/media/stream?path=courses%2Faudio-1.mp3&v=mp4");
    expect(getLocalMediaStreamUrl("https://youtu.be/abc_123-xy")).toBe("");
    expect(getLocalMediaStreamUrl("https://cdn.example.test/audio-1.mp3")).toBe("");
  });
});

describe("isVideoMediaSource", () => {
  it("classifies supported local and trusted YouTube video sources without accepting remote impostors", () => {
    expect(isVideoMediaSource("courses/lesson-1.avi")).toBe(true);
    expect(isVideoMediaSource("https://youtu.be/abc_123-xy")).toBe(true);
    expect(isVideoMediaSource("courses/dialogue.mp3")).toBe(false);
    expect(isVideoMediaSource("https://cdn.example.test/lesson.mp4")).toBe(false);
  });
});
