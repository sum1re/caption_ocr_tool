import type { AxiosProgressEvent } from 'axios'
import { useSetAtom } from 'jotai'
import { useCombineFile } from '../api/file/use-combine-file'
import { useUploadChunk } from '../api/file/use-upload-chunk'
import { useCreateProject } from '../api/project/use-create-project'
import { projectIdAtom } from '../provider/atom-provider'
import type { uuid } from './uuid'
import { xxhash3_64 } from './xxhash'

type UseUploadFileProps = {
  projectId?: uuid;
  file: File;
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void;
}

type UseUploadFileReturn = {
  uploadFile: (props: UseUploadFileProps) => void;
}

export const useUploadFile = (): UseUploadFileReturn => {
  const { mutateAsync: uploadChunk } = useUploadChunk()
  const { mutateAsync: createProject } = useCreateProject()
  const { mutateAsync: combineChunk } = useCombineFile()
  const setProjectId = useSetAtom(projectIdAtom)
  return {
    uploadFile: async ({ file, projectId, onUploadProgress }) => {
      let id = projectId
      if (id === undefined) {
        id = await createProject()
        setProjectId(id)
      }
      const buffer = await file.arrayBuffer()
      const chunkSize = 32 * 1024 * 1024
      const count = Math.ceil(buffer.byteLength / chunkSize)
      for (let index = 0; index < count; index++) {
        const start = index * chunkSize
        const end = start + chunkSize
        const chunk = buffer.slice(start, end)
        await uploadChunk({ projectId: id, index, chunk, onUploadProgress })
      }
      await combineChunk({
        projectId: id,
        filename: file.name,
        hash: await xxhash3_64(buffer),
      })
    },
  } as UseUploadFileReturn
}