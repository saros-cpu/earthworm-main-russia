import { flushPromises } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";

import * as toolApi from "~/api/tool";
import { useSetup } from "~/tests/helper/component";
import { resetSentenceLoading, useDailySentence, useSummary } from "../summary";

vi.mock("~/api/tool");

describe("summary", () => {
  describe("summary sentence", () => {
    const dummyRes = {
      ru: "ru",
      zh: "zh",
    };
    beforeEach(() => {
      vi.mocked(toolApi.fetchDailySentence).mockResolvedValue(dummyRes);
      return () => {
        resetSentenceLoading();
        vi.resetAllMocks();
      };
    });

    it("should load the daily sentence", async () => {
      const { wrapper } = useSetup(() => {
        const { zhSentence, ruSentence } = useDailySentence();
        return {
          zhSentence,
          ruSentence,
        };
      });

      await flushPromises();

      const { zhSentence, ruSentence } = wrapper.vm;

      expect(toolApi.fetchDailySentence).toBeCalled();
      expect(zhSentence).toBe(dummyRes.zh);
      expect(ruSentence).toBe(dummyRes.ru);
    });

    it("should only load sentence once", async () => {
      useSetup(() => {
        useDailySentence();
      });

      await flushPromises();

      useSetup(() => {
        useDailySentence();
      });

      await flushPromises();

      expect(toolApi.fetchDailySentence).toBeCalledTimes(1);
    });
  });

  describe("summary modal control", () => {
    it("should show summary modal", () => {
      const { showModal, showSummary } = useSummary();
      showSummary();
      expect(showModal.value).toBeTruthy();
    });

    it("should hide summary modal", () => {
      const { showModal, hideSummary } = useSummary();
      hideSummary();
      expect(showModal.value).toBeFalsy();
    });

    it("should return a same value in different hook", () => {
      const { showSummary } = useSummary();
      showSummary();
      const { showModal: anotherShowModal } = useSummary();
      expect(anotherShowModal.value).toBeTruthy();
    });
  });
});
