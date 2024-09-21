/// <reference types="vite/client" />
/// <reference types="vite-plugin-pwa/react" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_MEDIA_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}