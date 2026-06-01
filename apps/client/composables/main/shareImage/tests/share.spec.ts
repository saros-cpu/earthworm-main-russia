import { flushPromises } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { fontFetch } from "~/utils/fontLoader";
import { clearFontCache, convertSVGtoImg } from "../helper";
import { ShareImageTemplate, useGenerateShareImage } from "../share";
import { mockCanvasPrototypes } from "./helper";

vi.mock("~/api/course");
vi.mock("../../summary", async () => {
  const { ref } = await import("vue");
  return {
    useDailySentence: () => ({
      zhSentence: ref("重复是学习之母。"),
      ruSentence: ref("Повторение - мать учения."),
    }),
  };
});
vi.mock("satori", () => {
  return {
    default: vi.fn().mockResolvedValue("<svg></svg>"),
  };
});

vi.mock("~/utils/fontLoader", () => {
  return {
    fontFetch: vi.fn().mockResolvedValue({ arrayBuffer: () => new ArrayBuffer(8) }),
  };
});

vi.mock("../helper", async (importOriginal) => {
  return {
    ...((await importOriginal()) as any),
    convertSVGtoImg: vi
      .fn()
      .mockResolvedValue("default image url")
      .mockResolvedValueOnce("first image url"),
  };
});

const dummyUserName = "dummyUserName";
const dummyDateStr = "2024/03/12";

describe("Share Image", () => {
  beforeEach(() => {
    mockCanvasPrototypes();
    return () => {
      clearFontCache();
      vi.clearAllMocks();
    };
  });
  it("should generate an image", async () => {
    const { generateImage, shareImageSrc } = useGenerateShareImage();
    await generateImage(
      "零基础",
      "1",
      ShareImageTemplate.TPL_1,
      0,
      dummyUserName,
      dummyDateStr,
      0,
      "",
    );
    expect(shareImageSrc.value).toBe("first image url");
  });

  it("should copy the image", async () => {
    const { generateImage, copyShareImage } = useGenerateShareImage();
    const dummyIndex = 0;
    await generateImage(
      "零基础",
      "1",
      ShareImageTemplate.TPL_1,
      dummyIndex,
      dummyUserName,
      dummyDateStr,
      0,
      "",
    );
    vi.spyOn(navigator.clipboard, "write");
    copyShareImage(dummyIndex);
    await flushPromises();
    expect(navigator.clipboard.write).toBeCalled();
  });

  it("should load fonts when generating image", async () => {
    const { generateImage } = useGenerateShareImage();
    await generateImage("零基础", "1", ShareImageTemplate.TPL_1, 0, dummyUserName, dummyDateStr, 0, "");
    expect(fontFetch).toBeCalledTimes(2);
  });
});
