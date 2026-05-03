import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * In dev: `npm run dev` starts Vite on :5173 with HMR.
 *   - Requests to /api/** get proxied to the Grails backend on :8080,
 *     so the SPA can fetch('/api/books') without dealing with CORS.
 * In prod: `npm run build` writes a static bundle to ./dist/.
 *   - The root build.gradle copies dist/ into
 *     backend/src/main/resources/public/ so Spring Boot serves the
 *     SPA from the same origin as the API. Still no CORS.
 */
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
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
})
