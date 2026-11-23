import { useEffect, useState } from "react"
import { api, errorMessage } from "../api"
import { roleLabel } from "../auth"

type Profile = {
  employeeId?: string
  firstName?: string
  lastName?: string
  email?: string
  phone?: string
  position?: string
  role?: string
  employmentStatus?: string
  hireDate?: string
  department?: { name?: string }
}

export function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null)
  const [error, setError] = useState("")

  useEffect(() => {
    api
      .get("/api/employees/me")
      .then((response) => setProfile(response.data))
      .catch((err) => setError(errorMessage(err)))
  }, [])

  if (error) return <div className="alert alert-danger">{error}</div>
  if (!profile) return <p className="text-muted">Loading profile...</p>

  const rows: [string, string][] = [
    ["Employee ID", profile.employeeId || "—"],
    ["Name", `${profile.firstName || ""} ${profile.lastName || ""}`.trim()],
    ["Email", profile.email || "—"],
    ["Phone", profile.phone || "—"],
    ["Department", profile.department?.name || "—"],
    ["Position", profile.position || "—"],
    ["Role", profile.role ? roleLabel(profile.role) : "—"],
    ["Status", profile.employmentStatus || "—"],
    ["Hire date", profile.hireDate || "—"],
  ]

  return (
    <>
      <h1 className="h3">Profile</h1>
      <div className="card" style={{ maxWidth: 640 }}>
        <ul className="list-group list-group-flush">
          {rows.map(([label, value]) => (
            <li key={label} className="list-group-item d-flex justify-content-between">
              <span className="text-muted">{label}</span>
              <span>{value}</span>
            </li>
          ))}
        </ul>
      </div>
    </>
  )
}
