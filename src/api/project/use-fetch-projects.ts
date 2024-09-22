import { useQuery } from '@tanstack/react-query'
import { apiFetch, type CommonError } from '../api-client'
import type { Project } from './index'

export const useFetchProjects = () => {
  return useQuery<Project[], CommonError[]>({
    queryKey: ['project-all'],
    queryFn: async () => {
      return (await apiFetch<Project[]>('project/all')).result
    },
  })
}