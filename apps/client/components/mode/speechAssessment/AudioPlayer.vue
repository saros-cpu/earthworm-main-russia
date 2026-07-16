<template>
  <div
    class="w-full max-w-md rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900"
  >
    <div class="mb-2 flex items-center justify-between">
      <span class="text-xs font-semibold text-slate-500 dark:text-slate-400">
        {{ isPlaying ? "播放中" : "录音回放" }}
      </span>
      <span class="text-xs text-slate-400"
        >{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span
      >
    </div>
    <canvas
      ref="canvasRef"
      class="mb-3 h-16 w-full rounded bg-slate-100 dark:bg-slate-800"
    ></canvas>
    <div class="flex items-center justify-center gap-3">
      <button
        @click="togglePlay"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-500 text-white transition hover:bg-emerald-600 active:scale-95"
      >
        <UIcon
          :name="isPlaying ? 'i-ph-pause-fill' : 'i-ph-play-fill'"
          class="h-5 w-5"
        />
      </button>
      <input
        type="range"
        min="0"
        :max="duration"
        step="0.01"
        :value="currentTime"
        @input="seek"
        class="h-1.5 w-32 cursor-pointer appearance-none rounded-full bg-slate-200 accent-emerald-500 dark:bg-slate-700"
      />
      <button
        @click="restart"
        class="text-xs text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
      >
        <UIcon
          name="i-ph-arrow-counter-clockwise"
          class="h-4 w-4"
        />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";

const props = defineProps<{ src: string }>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
const isPlaying = ref(false);
const currentTime = ref(0);
const duration = ref(0);

let audioCtx: AudioContext | null = null;
let analyser: AnalyserNode | null = null;
let source: MediaElementAudioSourceNode | null = null;
let audioEl: HTMLAudioElement | null = null;
let rafId: number | null = null;

function formatTime(t: number) {
  const m = Math.floor(t / 60);
  const s = Math.floor(t % 60);
  return `${m}:${s.toString().padStart(2, "0")}`;
}

function initAudio() {
  if (audioEl) return;
  audioEl = new Audio(props.src);
  audioEl.preload = "auto";
  audioEl.onloadedmetadata = () => {
    duration.value = audioEl!.duration;
  };
  audioEl.ontimeupdate = () => {
    currentTime.value = audioEl!.currentTime;
  };
  audioEl.onended = () => {
    isPlaying.value = false;
  };
  audioCtx = new AudioContext();
  source = audioCtx.createMediaElementSource(audioEl);
  analyser = audioCtx.createAnalyser();
  analyser.fftSize = 128;
  source.connect(analyser);
  analyser.connect(audioCtx.destination);
  drawWaveform();
}

function drawWaveform() {
  if (!analyser || !canvasRef.value) return;
  const canvas = canvasRef.value;
  const ctx = canvas.getContext("2d")!;
  const bufferLength = analyser.frequencyBinCount;
  const dataArray = new Uint8Array(bufferLength);

  function draw() {
    rafId = requestAnimationFrame(draw);
    analyser!.getByteFrequencyData(dataArray);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const barWidth = (canvas.width / bufferLength) * 2.5;
    let x = 0;
    for (let i = 0; i < bufferLength; i++) {
      const barHeight = (dataArray[i] / 255) * canvas.height;
      ctx.fillStyle = isPlaying.value ? "#10b981" : "#94a3b8";
      ctx.fillRect(x, canvas.height - barHeight, barWidth - 1, barHeight);
      x += barWidth;
    }
  }
  draw();
}

function togglePlay() {
  if (!audioEl) initAudio();
  if (!audioEl) return;
  if (isPlaying.value) {
    audioEl.pause();
    isPlaying.value = false;
  } else {
    if (audioCtx?.state === "suspended") audioCtx.resume();
    audioEl.play().catch(() => {});
    isPlaying.value = true;
  }
}

function restart() {
  if (!audioEl) return;
  audioEl.currentTime = 0;
  currentTime.value = 0;
}

function seek(e: Event) {
  const val = parseFloat((e.target as HTMLInputElement).value);
  if (audioEl) {
    audioEl.currentTime = val;
    currentTime.value = val;
  }
}

onBeforeUnmount(() => {
  if (rafId) cancelAnimationFrame(rafId);
  if (audioEl) {
    audioEl.pause();
    audioEl.src = "";
  }
  if (audioCtx) audioCtx.close();
});
</script>
