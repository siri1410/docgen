import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Vite config: React plugin + dev proxy so `/api` calls hit the Spring Boot backend.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
