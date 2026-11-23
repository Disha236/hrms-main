import { Navigate, NavLink, Outlet } from "react-router-dom"
import { homePath, roleLabel, useAuth } from "./auth"
import type { Area, Role } from "./types"

const NAV: Record<Area, { to: string; label: string; icon: string }[]> = {
  admin: [
    { to: "dashboard", label: "Dashboard", icon: "bi-speedometer2" },
    { to: "employees", label: "Employees", icon: "bi-people" },
    { to: "departments", label: "Departments", icon: "bi-diagram-3" },
    { to: "attendance", label: "Attendance", icon: "bi-calendar-check" },
    { to: "leaves", label: "Leaves", icon: "bi-calendar2-week" },
    { to: "approvals", label: "Approvals", icon: "bi-check2-square" },
    { to: "announcements", label: "Announcements", icon: "bi-megaphone" },
    { to: "profile", label: "Profile", icon: "bi-person" },
  ],
  manager: [
    { to: "dashboard", label: "Dashboard", icon: "bi-speedometer2" },
    { to: "attendance", label: "Attendance", icon: "bi-calendar-check" },
    { to: "leaves", label: "Leaves", icon: "bi-calendar2-week" },
    { to: "approvals", label: "Approvals", icon: "bi-check2-square" },
    { to: "announcements", label: "Announcements", icon: "bi-megaphone" },
    { to: "profile", label: "Profile", icon: "bi-person" },
  ],
  employee: [
    { to: "dashboard", label: "Dashboard", icon: "bi-speedometer2" },
    { to: "attendance", label: "Attendance", icon: "bi-calendar-check" },
    { to: "leaves", label: "Leaves", icon: "bi-calendar2-week" },
    { to: "announcements", label: "Announcements", icon: "bi-megaphone" },
    { to: "profile", label: "Profile", icon: "bi-person" },
  ],
}

const ALLOWED: Record<Area, Role[]> = {
  admin: ["ADMIN", "HR_MANAGER"],
  manager: ["MANAGER"],
  employee: ["EMPLOYEE"],
}

export function ProtectedLayout({ area }: { area: Area }) {
  const { user, logout } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (!ALLOWED[area].includes(user.role)) return <Navigate to={homePath(user.role)} replace />

  return (
    <div className="d-flex min-vh-100">
      <aside className="bg-dark text-white p-3" style={{ width: 240 }}>
        <div className="fw-semibold mb-1">HRMS</div>
        <div className="small text-white-50 mb-4">{roleLabel(user.role)}</div>
        <nav className="nav nav-pills flex-column gap-1">
          {NAV[area].map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `nav-link d-flex align-items-center gap-2 ${isActive ? "active" : "text-white"}`
              }
            >
              <i className={`bi ${item.icon}`} />
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="flex-grow-1 bg-light">
        <header className="bg-white border-bottom px-4 py-3 d-flex justify-content-between align-items-center">
          <div>
            <div className="fw-semibold">{user.name}</div>
            <div className="small text-muted">{user.email}</div>
          </div>
          <button className="btn btn-outline-secondary btn-sm" onClick={logout}>
            Sign out
          </button>
        </header>
        <main className="p-4">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
