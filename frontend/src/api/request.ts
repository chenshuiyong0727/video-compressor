import axios from "axios";

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

const request = axios.create({
  baseURL: "/api",
  timeout: 30000
});

request.interceptors.response.use((response) => {
  const result = response.data as ApiResult<unknown>;
  if (result && result.code && result.code !== 200) {
    return Promise.reject(new Error(result.message || "请求失败"));
  }
  return response;
});

export async function getData<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const response = await request.get<ApiResult<T>>(url, { params });
  return response.data.data;
}

export async function postData<T>(url: string, data?: unknown): Promise<T> {
  const response = await request.post<ApiResult<T>>(url, data);
  return response.data.data;
}
