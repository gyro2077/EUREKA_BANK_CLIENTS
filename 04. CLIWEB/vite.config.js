import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/soap-java': {
        target: 'http://209.145.48.25:8091',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/soap-java/, '')
      },
      '/api/rest-java': {
        target: 'http://209.145.48.25:8090',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/rest-java/, '')
      },
      '/api/soap-dotnet': {
        target: 'http://209.145.48.25:8092',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/soap-dotnet/, '')
      },
      '/api/rest-dotnet': {
        target: 'http://209.145.48.25:8093',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/rest-dotnet/, '')
      }
    }
  }
})
