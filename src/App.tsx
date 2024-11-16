import { CssBaseline, GlobalStyles } from '@mui/material'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { SnackbarProvider } from 'notistack'
import { type ReactElement, Suspense } from 'react'
import { RouterProvider } from 'react-router-dom'
import { Placeholder } from './components/placeholder/placeholder'
import { router } from './components/router/router.tsx'
import { AtomProvider } from './provider/atom-provider'

const queryClient = new QueryClient()

export function App(): ReactElement {
  return (
    <Suspense fallback={<Placeholder />}>
      <AtomProvider>
        <CssBaseline />
        <GlobalStyles styles={{ html: { WebkitFontSmoothing: 'auto' } }} />
        <SnackbarProvider>
          <QueryClientProvider client={queryClient}>
            <RouterProvider router={router} />
          </QueryClientProvider>
        </SnackbarProvider>
      </AtomProvider>
    </Suspense>
  )
}
