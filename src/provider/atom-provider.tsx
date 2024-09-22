import { createTheme, type PaletteMode, ThemeProvider } from '@mui/material'
import { atom, useAtomValue } from 'jotai'
import { atomWithStorage } from 'jotai/vanilla/utils'
import React, { useMemo } from 'react'
import type { RequiredFields } from '../utils/common'
import type { uuid } from '../utils/uuid'

type AtomProviderProps = RequiredFields<React.PropsWithChildren, 'children'>;

type ThemeMode = {
  mode: PaletteMode | 'system';
}

export const projectIdAtom = atom<uuid | undefined>(undefined)
export const themeAtom = atomWithStorage<ThemeMode>('theme-mode', { mode: 'light' })
export const appbarAtom = atom<boolean>(true)

const toPaletteMode = ({ mode }: ThemeMode): PaletteMode => {
  if (mode === 'system') {
    if (window) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    return 'light'
  } else {
    return mode as PaletteMode
  }
}

export function AtomProvider({ children }: AtomProviderProps): React.ReactElement {
  const themeMode = useAtomValue(themeAtom)
  const theme = useMemo(() => {
    return createTheme({ palette: { mode: toPaletteMode(themeMode) } })
  }, [themeMode])
  return (
    <ThemeProvider theme={theme}>
      {children}
    </ThemeProvider>
  )
}
