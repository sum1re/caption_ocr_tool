import React from 'react';
import { Outlet, Route, Routes } from 'react-router-dom';
import { Layout } from './layout/Layout.tsx';
import { Welcome } from './welcome/Welcome.tsx';

type RouterProps = {}

export function Router({}: RouterProps): React.ReactElement {
  return (
    <Routes>
      <Route element={<Layout><Outlet /></Layout>}>
        <Route index element={<Welcome />}>
        </Route>
      </Route>
    </Routes>
  );
}
