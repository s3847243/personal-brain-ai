import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';

const axiosInstance = axios.create({
  baseURL: '/api',
  
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

export const refreshClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: (value: unknown) => void; reject: (error: unknown) => void }> = [];
let refreshAttempts = 0;

const processQueue = (error: AxiosError | null) => {
  failedQueue.forEach(({ resolve, reject }) => (error ? reject(error) : resolve(null)));
  failedQueue = [];
};

const authPathsNoRefresh = ['/auth/login', '/auth/logout', '/auth/refresh'];

const shouldAttemptRefresh = (error: AxiosError) => {
  const status = error.response?.status;
  const url = (error.config?.url || '').toString();
  if (authPathsNoRefresh.some(p => url.includes(p))) return false;
  return status === 401 || status === 403;
};

axiosInstance.interceptors.response.use(
  (res: AxiosResponse) => res,
  async (error: AxiosError) => {
    const original = error.config as (AxiosRequestConfig & { _retry?: boolean }) | undefined;

    if (!original || !shouldAttemptRefresh(error) || original._retry) {
      return Promise.reject(error);
    }

    if (refreshAttempts >= 1) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then(() => axiosInstance({ ...original }))
        .catch((err) => Promise.reject(err));
    }

    original._retry = true;
    isRefreshing = true;
    refreshAttempts += 1;

    try {
      const res = await refreshClient.post('/auth/refresh');
      if (res.status < 200 || res.status >= 300) throw new Error('Refresh failed');
      processQueue(null);
      return axiosInstance({ ...original });
    } catch (e) {
      processQueue(e as AxiosError);
      return Promise.reject(e);
    } finally {
      isRefreshing = false;
    }
  }
);

export default axiosInstance;