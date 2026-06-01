<template>
  <div class="pointer-events-none fixed left-6 top-1/2 z-50 -translate-y-1/2">
    <Transition name="combo-fade">
      <div v-if="show" class="flex flex-col items-center gap-2">
        <div
          :key="comboKey"
          class="combo-badge animate-bounce-in"
          :class="badgeClass"
        >
          <div class="text-2xl font-black leading-none">{{ comboCount }}x</div>
          <div class="mt-0.5 text-[10px] font-bold uppercase tracking-wider">
            {{ comboLabel }}
          </div>
        </div>
        <div
          v-if="score > 0 && showScore"
          :key="'score-' + scoreKey"
          class="animate-float-up text-sm font-bold"
          :class="scoreColor"
        >
          +{{ score }}
        </div>
      </div>
    </Transition>
  </div>

  <div class="pointer-events-none fixed right-6 top-1/2 z-50 -translate-y-1/2">
    <Transition name="rating-fade">
      <div v-if="showRating" :key="ratingKey" class="animate-rating-pop text-center">
        <div class="text-6xl font-black" :class="ratingColor">{{ rating }}</div>
        <div class="mt-1 text-xs font-bold uppercase tracking-widest" :class="ratingLabelColor">
          {{ ratingLabel }}
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";

import type { ComboRating } from "~/composables/main/combo";

const props = defineProps<{
  comboCount: number;
  comboLabel: string;
  score: number;
  rating: ComboRating;
  showRating: boolean;
}>();

const comboKey = ref(0);
const scoreKey = ref(0);
const ratingKey = ref(0);
const show = ref(false);
const showScore = ref(false);

watch(() => props.comboCount, () => {
  comboKey.value++;
  show.value = true;
  showScore.value = true;
  setTimeout(() => { showScore.value = false; }, 800);
});

watch(() => props.rating, () => {
  ratingKey.value++;
});

watch(() => props.showRating, (v) => {
  if (!v) show.value = false;
});

const badgeClass = computed(() => {
  if (props.comboCount >= 20) return "bg-gradient-to-br from-purple-600 to-pink-500 text-white shadow-lg shadow-purple-400/40";
  if (props.comboCount >= 15) return "bg-gradient-to-br from-orange-500 to-red-500 text-white shadow-lg shadow-orange-400/40";
  if (props.comboCount >= 10) return "bg-gradient-to-br from-emerald-500 to-teal-500 text-white shadow-lg shadow-emerald-400/40";
  if (props.comboCount >= 7) return "bg-blue-500 text-white shadow-lg shadow-blue-400/30";
  if (props.comboCount >= 5) return "bg-violet-400 text-white shadow-md";
  if (props.comboCount >= 3) return "bg-amber-400 text-white shadow-sm";
  return "bg-slate-300 text-slate-600";
});

const scoreColor = computed(() => {
  if (props.comboCount >= 20) return "text-purple-500";
  if (props.comboCount >= 10) return "text-emerald-500";
  return "text-slate-500";
});

const ratingColor = computed(() => {
  if (props.rating === "SSS") return "text-yellow-400";
  if (props.rating === "SS") return "text-orange-400";
  if (props.rating === "S") return "text-emerald-400";
  if (props.rating === "A") return "text-blue-400";
  if (props.rating === "B") return "text-violet-400";
  return "text-slate-400";
});

const ratingLabelColor = computed(() => {
  if (props.rating === "SSS") return "text-yellow-500";
  if (props.rating === "SS") return "text-orange-500";
  if (props.rating === "S") return "text-emerald-500";
  return "text-slate-400";
});

const ratingLabel = computed(() => {
  if (props.rating === "SSS") return "Perfect";
  if (props.rating === "SS") return "Excellent";
  if (props.rating === "S") return "Great";
  if (props.rating === "A") return "Good";
  if (props.rating === "B") return "Fair";
  return "Keep Going";
});
</script>

<style scoped>
.combo-badge {
  border-radius: 1rem;
  padding: 0.5rem 1rem;
  min-width: 70px;
  text-align: center;
}
.animate-bounce-in {
  animation: bounceIn 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}
.animate-float-up {
  animation: floatUp 0.8s ease-out forwards;
}
.animate-rating-pop {
  animation: ratingPop 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}
.combo-fade-enter-active { transition: all 0.3s ease; }
.combo-fade-leave-active { transition: all 0.2s ease; }
.combo-fade-enter-from,
.combo-fade-leave-to { opacity: 0; transform: translateY(10px); }
.rating-fade-enter-active { transition: all 0.3s ease; }
.rating-fade-leave-active { transition: all 0.2s ease; }
.rating-fade-enter-from { opacity: 0; transform: scale(0.5); }
.rating-fade-leave-to { opacity: 0; }

@keyframes bounceIn {
  0% { transform: scale(0.3); opacity: 0; }
  50% { transform: scale(1.15); }
  70% { transform: scale(0.95); }
  100% { transform: scale(1); opacity: 1; }
}
@keyframes floatUp {
  0% { opacity: 1; transform: translateY(0); }
  100% { opacity: 0; transform: translateY(-30px); }
}
@keyframes ratingPop {
  0% { transform: scale(0) rotate(-10deg); opacity: 0; }
  60% { transform: scale(1.3) rotate(3deg); }
  100% { transform: scale(1) rotate(0deg); opacity: 1; }
}
</style>
