import { useQuery } from '@tanstack/react-query'
import { apiFetch, type CommonError } from '../api-client'

export type AppInfo = {
  name: string;
  appLicense: string;
  version: string;
  buildTimestamp: string;
}

export const useFetchInfo = () => {
  return useQuery<AppInfo, CommonError[]>({
    queryKey: ['app-info'],
    queryFn: async () => {
      return (await apiFetch<AppInfo>('app/info')).result
    },
  })
}