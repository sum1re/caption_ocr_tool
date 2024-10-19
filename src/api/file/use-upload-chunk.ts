import { useMutation } from '@tanstack/react-query'
import type { AxiosProgressEvent } from 'axios'
import type { uuid } from '../../utils/uuid'
import { xxhash3_64 } from '../../utils/xxhash'
import { apiPost } from '../api-client'

type UploadChunkProps = {
  projectId: uuid;
  index: number;
  chunk: ArrayBuffer;
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void
}

export const useUploadChunk = () => {
  return useMutation<void, unknown, UploadChunkProps>({
    mutationFn: async ({ projectId, index, chunk, onUploadProgress }) => {
      const { result } = await apiPost<string, ArrayBuffer>(`/file/${projectId}/${index}`, chunk, { onUploadProgress })
      const hash = await xxhash3_64(chunk)
      if (result !== hash) {
        throw Error('checksum error')
      }
    },
  })
}
