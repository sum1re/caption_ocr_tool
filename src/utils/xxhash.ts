import { createXXHash3 } from 'hash-wasm'
import type { IDataType } from 'hash-wasm/dist/lib/util'

type DataType = IDataType | ArrayBuffer

const xxhash3 = async (data: IDataType): Promise<string> => {
  const hasher = await createXXHash3()
  hasher.init()
  hasher.update(data)
  return hasher.digest()
}

export const xxhash3_64 = async (data: DataType): Promise<string> => {
  return data instanceof ArrayBuffer ? await xxhash3(new Uint8Array(data)) : await xxhash3_64(data)
}
