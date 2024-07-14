import { CssBaseline, GlobalStyles } from '@mui/material';
import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ColorModeProvider } from './provider/ColorModeProvider.tsx';
import { Router } from './routes/Router.tsx';

export function App(): React.ReactElement {
  return (
    <BrowserRouter>
      <ColorModeProvider>
        <CssBaseline />
        <GlobalStyles styles={{ html: { WebkitFontSmoothing: 'auto' } }} />
        <Router />
      </ColorModeProvider>
    </BrowserRouter>
  );
}
