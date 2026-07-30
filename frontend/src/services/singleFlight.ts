export function createSingleFlight<T>(operation: () => Promise<T>): () => Promise<T> {
  let activePromise: Promise<T> | null = null;

  return () => {
    if (!activePromise) {
      activePromise = operation().finally(() => {
        activePromise = null;
      });
    }
    return activePromise;
  };
}
