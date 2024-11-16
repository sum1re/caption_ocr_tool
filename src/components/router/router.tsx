import { lazily } from 'react-lazily'
import { createBrowserRouter, Navigate, redirect } from 'react-router-dom'
import { isUUID } from '../../utils/uuid'
import { Layout } from '../../views/layout/layout'

const { Welcome } = lazily(() => import('../../views/welcome/welcome'))
const { Project } = lazily(() => import('../../views/project/project'))
const { Settings } = lazily(() => import('../../views/settings/settings'))
const { About } = lazily(() => import('../../views/about/about'))

export const router = createBrowserRouter(
  [
    {
      path: '/',
      element: <Layout />,
      children: [
        {
          path: '/index',
          element: <Navigate to="/" replace />,
        },
        {
          index: true,
          element: <Welcome />,
        },
        {
          path: ':projectId',
          loader: ({ params }) => {
            if (!isUUID(params.projectId)) {
              return redirect('/')
            }
            return null
          },
          children: [
            {
              index: true,
              element: <Project />,
            },
          ],
        },
        {
          path: 'settings',
          element: <Settings />,
        },
        {
          path: 'about',
          element: <About />,
        },
      ],
    },
  ],
  {
    future: {
      v7_fetcherPersist: true,
    },
  },
)