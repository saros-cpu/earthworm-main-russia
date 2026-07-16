export default defineNuxtPlugin(() => {
  if (import.meta.client) return;

  Object.defineProperty(globalThis, "localStorage", {
    value: {
      getItem: () => null,
      setItem: () => {},
      removeItem: () => {},
      clear: () => {},
      key: () => null,
      length: 0,
    } satisfies Storage,
    writable: false,
    configurable: true,
  });
});
