import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 8153,
    proxy: {
      '/api': {
        target: 'http://localhost:8183',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist'
  }
})
