import { createTheme, type PaletteMode, ThemeProvider } from '@mui/material';
import React, { createContext, useCallback, useEffect, useMemo, useState } from 'react';

type ColorMode = PaletteMode & 'system'

type ColorModeContextType = {
  mode: ColorMode,
  toggleMode: (mode: ColorMode) => void;
}

type ColorModeProviderProps = Required<Pick<React.PropsWithChildren, 'children'>>

export const ColorModeContext = createContext<ColorModeContextType>({} as ColorModeContextType);

export function ColorModeProvider({ children }: ColorModeProviderProps): React.ReactElement {
  const colorModeFromLocalStorage: ColorMode = localStorage.getItem('color-mode') as ColorMode ?? 'light';
  const [mode, setMode] = useState(colorModeFromLocalStorage);
  const toPaletteMode = useCallback((): PaletteMode => {
    if (mode === 'system') {
      return window?.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    } else {
      return mode as PaletteMode;
    }
  }, []);
  const theme = useMemo(() => createTheme({ palette: { mode: toPaletteMode() } }), [mode]);
  useEffect(() => {
    localStorage.setItem('color-mode', mode);
  }, [mode]);
  const toggleMode = (mode: ColorMode) => {
    setMode(mode);
  };
  return (
    <ColorModeContext.Provider value={{ mode, toggleMode }}>
      <ThemeProvider theme={theme}>
        {children}
      </ThemeProvider>
    </ColorModeContext.Provider>
  );
}
