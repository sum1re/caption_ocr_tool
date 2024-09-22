import { isEmpty } from 'moderndash'

export type uuid = string & { __uuid: void };

const UUID_REGEX = /^[0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12}$/

export function parse(uuidString: string): uuid | undefined {
  if (UUID_REGEX.test(uuidString)) {
    return uuidString as uuid
  } else {
    return undefined
  }
}

export const isUUID = (value: uuid | string | undefined): boolean => {
  if (isEmpty(value)) return false
  return UUID_REGEX.test(value as string)
}