import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Java SOAP Microservices
      '/api/soap-java-login': {
        target: 'http://209.145.48.25:8091',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/soap-java-login/, '')
      },
      '/api/soap-java-cuentas': {
        target: 'http://209.145.48.25:8094',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/soap-java-cuentas/, '')
      },
      '/api/soap-java-movimientos': {
        target: 'http://209.145.48.25:8095',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/soap-java-movimientos/, '')
      },
      '/api/soap-java-transferencias': {
        target: 'http://209.145.48.25:8096',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/soap-java-transferencias/, '')
      },

      // .NET REST Microservices
      '/api/rest-dotnet-login': {
        target: 'http://209.145.48.25:8093',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/rest-dotnet-login/, '')
      },
      '/api/rest-dotnet-cuentas': {
        target: 'http://209.145.48.25:8097',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/rest-dotnet-cuentas/, '')
      },
      '/api/rest-dotnet-movimientos': {
        target: 'http://209.145.48.25:8098',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/rest-dotnet-movimientos/, '')
      },
      '/api/rest-dotnet-transferencias': {
        target: 'http://209.145.48.25:8099',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/rest-dotnet-transferencias/, '')
      },

      // Monolitos
      '/api/rest-java': {
        target: 'http://209.145.48.25:8090',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/rest-java/, '')
      },
      '/api/soap-dotnet': {
        target: 'http://209.145.48.25:8092',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/soap-dotnet/, '')
      }
    }
  }
})
