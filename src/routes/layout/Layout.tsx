import { AppBar, Box } from '@mui/material';
import React from 'react';
import PWABadge from '../../PWABadge.tsx';
import { LayoutToolbar } from './LayoutToolbar.tsx';

type LayoutProps = Required<Pick<React.PropsWithChildren, 'children'>>

export function Layout({ children }: LayoutProps): React.ReactElement {
  return (
    <Box display="flex" flexDirection="row">
      <Box
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
        <AppBar position="sticky" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
          <LayoutToolbar />
        </AppBar>
        <Box
          component="main"
          sx={{ p: 0, flexGrow: 1, }}
        >
          {children}
        </Box>
      </Box>
      <PWABadge />
    </Box>
  );
}
