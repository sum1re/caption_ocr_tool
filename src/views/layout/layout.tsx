import { AppBar, Box } from '@mui/material'
import { useAtomValue } from 'jotai'
import { type ReactElement, Suspense } from 'react'
import { Outlet } from 'react-router-dom'
import { Placeholder } from '../../components/placeholder/placeholder'
import { appbarAtom } from '../../provider/atom-provider'
import PwaBadge from '../../pwa-badge'
import { LayoutToolbar } from './layout-toolbar'

type LayoutProps = {}

export function Layout({}: LayoutProps): ReactElement {
  const appbar = useAtomValue(appbarAtom)
  return (
    <Box
      display="flex"
      flexDirection="row"
      sx={[
        {
          display: 'flex',
          flexDirection: 'column',
          flex: 1,
          minHeight: '100vh',
        },
        { overflow: 'auto' },
        { overflow: 'clip' },
      ]}
    >
      {appbar && (
        <AppBar
          position="sticky"
          elevation={0}
          sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
        >
          <LayoutToolbar />
        </AppBar>
      )}
      <Box
        component="main"
        sx={{ p: 0, flexGrow: 1 }}
      >
        <Suspense fallback={<Placeholder />}>
          <Outlet />
        </Suspense>
        <PwaBadge />
      </Box>
    </Box>
  )
}
