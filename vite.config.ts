import { tanstackRouter } from '@tanstack/router-plugin/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import compression from 'vite-plugin-compression2'
import { VitePWA } from 'vite-plugin-pwa'

// https://vitejs.dev/config/
export default defineConfig({
  appType: 'spa',
  server: {
    host: '0.0.0.0',
  },
  plugins: [
    tanstackRouter({ target: 'react', autoCodeSplitting: true }),
    VitePWA({
      registerType: 'prompt',
      injectRegister: false,

      pwaAssets: {
        disabled: false,
        config: true,
      },

      manifest: {
        name: 'Caption OCR Tool',
        short_name: 'cocr',
        description: 'A tool for extracting video caption.',
        theme_color: '#ffffff',
      },

      workbox: {
        globPatterns: ['**/*.{js,css,html,svg,png,ico}'],
        cleanupOutdatedCaches: true,
        clientsClaim: true,
      },

      devOptions: {
        enabled: false,
        navigateFallback: 'index.html',
        suppressWarnings: true,
        type: 'module',
      },
    }),
    react(),
    compression({
      exclude: [/\.svg$/],
    }),
  ],
  build: {
    outDir: 'dist',
    manifest: true,
    target: 'baseline-widely-available',
    cssCodeSplit: true,
    modulePreload: {
      polyfill: true,
    },
    minify: 'oxc',
    cssMinify: 'lightningcss',
    reportCompressedSize: true,

    //build
    sourcemap: false,
    assetsInlineLimit: 4096,
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        minifyInternalExports: true,
      },
    },
  },
})