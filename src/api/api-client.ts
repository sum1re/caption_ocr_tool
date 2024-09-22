import axios, { AxiosError, type AxiosRequestConfig } from 'axios'
import { enqueueSnackbar } from 'notistack'

export type Pageable = {
  page: number;
  size: number;
}

type CommonResponse<T> = {
  success: boolean;
  errors: CommonError[];
  result?: T
  resultInfo?: CommonPagination
}

export type SuccessResponse<T> = Omit<CommonResponse<T>, 'errors'> & Required<Pick<CommonResponse<T>, 'result'>>

export type ErrorResponse = Omit<CommonResponse<void>, 'resultInfo' | 'result'>

export type CommonError = {
  code: number;
  message: string;
}

export type CommonPagination = {
  count: number;
  page: number;
  perPage: number;
  totalCount: number;
  totalPages: number;
}

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 0,
  responseType: 'json',
  responseEncoding: 'UTF-8',
  withCredentials: false,
  validateStatus: (status) => (status >= 200 && status < 300),
})

// @ts-ignore
apiClient.interceptors.response.use((response) => {
  const { result, resultInfo } = response.data
  return { result, resultInfo, success: true }
}, (error: AxiosError<ErrorResponse>) => {
  const { status, message, response } = error
  if (status && status >= 400 && response && response.data) {
    response.data.errors.forEach((commonError) => {
      enqueueSnackbar({
        message: commonError.message,
        variant: 'error',
      })
    })
  } else {
    enqueueSnackbar({ message, variant: 'error' })
  }
  return Promise.reject(error)
})

export const apiFetch = <T>(url: string, config?: AxiosRequestConfig) =>
  apiClient.get<any, SuccessResponse<T>>(url, config)

export const apiDelete = <T>(url: string, config?: AxiosRequestConfig) =>
  apiClient.delete<any, SuccessResponse<T>>(url, config)

export const apiPut = <T, D>(url: string, data?: D, config?: AxiosRequestConfig) =>
  apiClient.put<any, SuccessResponse<T>>(url, data, config)

export const apiPost = <T, D>(url: string, data?: D, config?: AxiosRequestConfig) =>
  apiClient.post<any, SuccessResponse<T>>(url, data, config)

export const apiPatch = <T, D>(url: string, data?: D, config?: AxiosRequestConfig) =>
  apiClient.patch<any, SuccessResponse<T>>(url, data, config)