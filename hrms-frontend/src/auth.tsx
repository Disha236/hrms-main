import { createContext, useContext, useMemo, useState } from "react"
import type { ReactNode } from "react"
import { api, clearSession, readStoredUser, saveSession } from "./api"
import type { Area, Role, SessionUser } from "./types"

type AuthContextValue = {
  user: SessionUser | null
  login: (email: string, password: string) => Promise<SessionUser>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(() => readStoredUser<SessionUser>())

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      async login(email, password) {
        const { data } = await api.post("/api/auth/login", { email, password })
        const nextUser = data.user as SessionUser
        saveSession(data.token, nextUser)
        setUser(nextUser)
        return nextUser
      },
      logout() {
        clearSession()
        setUser(null)
      },
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error("useAuth must be used inside AuthProvider")
  return context
}

export function areaForRole(role: Role): Area {
  if (role === "EMPLOYEE") return "employee"
  if (role === "MANAGER") return "manager"
  return "admin"
}

export function homePath(role: Role) {
  return `/${areaForRole(role)}/dashboard`
}

export function roleLabel(role: string) {
  if (role === "HR_MANAGER") return "HR Manager"
  if (role === "ADMIN") return "Admin"
  if (role === "MANAGER") return "Manager"
  return "Employee"
}
