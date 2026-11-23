import axios from "axios"

const TOKEN_KEY = "hrms_token"
const USER_KEY = "hrms_user"

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "",
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function saveSession(token: string, user: unknown) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function readStoredUser<T>(): T | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function errorMessage(error: unknown, fallback = "Request failed") {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data
    if (typeof data === "string" && data.trim()) return data
    if (data && typeof data === "object") {
      const record = data as Record<string, unknown>
      if (typeof record.message === "string") return record.message
      if (typeof record.error === "string") return record.error
    }
    if (error.response?.status === 401) return "Email or password is incorrect"
    if (error.response?.status === 403) return "You do not have access to this action"
  }
  return fallback
}
