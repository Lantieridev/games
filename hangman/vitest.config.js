import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'jsdom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: ['index.js'],
      thresholds: {
        lines: 100,
        branches: 95,
        functions: 100,
        statements: 100,
      },
    },
  },
});
