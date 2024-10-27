import { BigNumber } from 'bignumber.js'

export type RequiredFields<T, K extends keyof T> = T & Required<Pick<T, K>>;

export const formatTime = (seconds: number, withMillisecond: boolean = true): string => {
  const time = BigNumber(seconds.toString())
  if (time.isZero() || time.isNegative()) {
    return `0:00:00${withMillisecond ? '.00' : ''}`
  }
  const millisecond = time.multipliedBy(BigNumber('100')).modulo(BigNumber('100'))
  const second = time.modulo(BigNumber('60'))
  const minute = time.dividedToIntegerBy(BigNumber('60')).modulo(BigNumber('60'))
  const hour = time.dividedToIntegerBy(BigNumber('3600'))
  const formattedHours = hour.toFixed().padStart(1, '0')
  const formattedMinutes = minute.toFixed().padStart(2, '0')
  const formattedSeconds = second.toFixed(0, BigNumber.ROUND_FLOOR).padStart(2, '0')
  const formattedMilliseconds = millisecond.toFixed(0, BigNumber.ROUND_HALF_EVEN).padStart(2, '0')
  return `${formattedHours}:${formattedMinutes}:${formattedSeconds}${withMillisecond ? `.${formattedMilliseconds}` : ''}`
}

export const formatTimeline = (seconds: number): string => {
  const time = BigNumber(seconds.toString())
  if (time.lte(BigNumber('0'))) {
    return '00:00'
  }
  const second = time.modulo(BigNumber('60'))
  const minute = time.dividedToIntegerBy(BigNumber('60')).modulo(BigNumber('60'))
  const result = `${minute.toFixed().padStart(2, '0')}:${second.toFixed(0, BigNumber.ROUND_FLOOR).padStart(2, '0')}`
  if (time.lt(BigNumber('3600'))) {
    return result
  }
  return `${time.dividedToIntegerBy(BigNumber('3600'))}:${result}`
}