import { useMutation } from '@tanstack/react-query'
import type { uuid } from '../../utils/uuid'
import { apiPatch, type CommonError } from '../api-client'

type UseCombineFileProps = {
  filename: string;
  hash: string;
  projectId: uuid;
}

export const useCombineFile = () => {
  return useMutation<void, CommonError[], UseCombineFileProps>({
    mutationFn: async ({ projectId, filename, hash }) => {
      await apiPatch<void, unknown>(`file/${projectId}`, { filename, hash })
    },
    retry: false,
  })
}