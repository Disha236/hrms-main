export type Role = "ADMIN" | "HR_MANAGER" | "MANAGER" | "EMPLOYEE"

export type SessionUser = {
  id: string
  email: string
  name: string
  role: Role
  departmentId?: string | null
}

export type Area = "admin" | "manager" | "employee"
