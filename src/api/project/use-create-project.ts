import { useMutation } from '@tanstack/react-query'
import type { uuid } from '../../utils/uuid'
import { apiPost } from '../api-client'

export const useCreateProject = () => {
  return useMutation<uuid>({
    mutationFn: async () => {
      return (await apiPost<uuid, unknown>('project')).result
    },
  })
}