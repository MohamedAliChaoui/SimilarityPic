import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  server: {
    proxy: {
      '^/': {
        target: 'http://localhost:8090', // Spring boot backend address
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist'
  },
  plugins: [vue(), vueDevTools({ launchEditor: 'code', })],
})
