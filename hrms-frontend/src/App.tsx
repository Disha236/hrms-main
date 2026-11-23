import { Navigate, Route, Routes } from "react-router-dom"
import { ProtectedLayout } from "./layout"
import { AnnouncementsPage } from "./pages/AnnouncementsPage"
import { ApprovalsPage } from "./pages/ApprovalsPage"
import { AttendancePage } from "./pages/AttendancePage"
import { DashboardPage } from "./pages/DashboardPage"
import { DepartmentsPage } from "./pages/DepartmentsPage"
import { EmployeesPage } from "./pages/EmployeesPage"
import { LeavesPage } from "./pages/LeavesPage"
import { ProfilePage } from "./pages/ProfilePage"
import { HomePage, LoginPage } from "./pages/PublicPages"

function areaRoutes(includePeople: boolean, includeApprovals: boolean) {
  return [
    <Route key="dashboard" path="dashboard" element={<DashboardPage />} />,
    includePeople ? <Route key="employees" path="employees" element={<EmployeesPage />} /> : null,
    includePeople ? <Route key="departments" path="departments" element={<DepartmentsPage />} /> : null,
    <Route key="attendance" path="attendance" element={<AttendancePage />} />,
    <Route key="leaves" path="leaves" element={<LeavesPage />} />,
    includeApprovals ? <Route key="approvals" path="approvals" element={<ApprovalsPage />} /> : null,
    <Route key="announcements" path="announcements" element={<AnnouncementsPage />} />,
    <Route key="profile" path="profile" element={<ProfilePage />} />,
  ]
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/admin" element={<ProtectedLayout area="admin" />}>
        <Route index element={<Navigate to="dashboard" replace />} />
        {areaRoutes(true, true)}
      </Route>
      <Route path="/manager" element={<ProtectedLayout area="manager" />}>
        <Route index element={<Navigate to="dashboard" replace />} />
        {areaRoutes(false, true)}
      </Route>
      <Route path="/employee" element={<ProtectedLayout area="employee" />}>
        <Route index element={<Navigate to="dashboard" replace />} />
        {areaRoutes(false, false)}
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
