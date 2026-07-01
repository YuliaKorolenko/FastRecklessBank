import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The frontend is a standalone React app. In dev (and preview) the Vite server
// proxies /api calls to the Spring Boot backend on :8080, so no CORS setup or
// backend changes are needed.
const proxy = {
  '/api': 'http://localhost:8080',
}

export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy },
  preview: { port: 4173, proxy },
})
