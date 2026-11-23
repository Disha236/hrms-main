import { useEffect, useState } from "react"
import { api, errorMessage } from "../api"
import { useAuth } from "../auth"

type CompanyStats = {
  totalEmployees: number
  presentToday: number
  onLeaveToday: number
  pendingLeaves: number
}

type PersonalStats = {
  employeeName: string
  monthLabel: string
  pendingLeaves: number
  stats: { onTime: number; late: number; absentLeaves: number; todayStatus: string | null }
}

export function DashboardPage() {
  const { user } = useAuth()
  const personal = user?.role === "EMPLOYEE"
  const [company, setCompany] = useState<CompanyStats | null>(null)
  const [mine, setMine] = useState<PersonalStats | null>(null)
  const [error, setError] = useState("")

  useEffect(() => {
    const request = personal ? api.get("/api/dashboard/employee") : api.get("/api/dashboard/stats")
    request
      .then((response) => {
        if (personal) setMine(response.data)
        else setCompany(response.data)
      })
      .catch((err) => setError(errorMessage(err)))
  }, [personal])

  if (error) return <div className="alert alert-danger">{error}</div>

  if (personal) {
    return (
      <>
        <h1 className="h3">Hello, {mine?.employeeName || user?.name}</h1>
        <p className="text-muted">{mine?.monthLabel}</p>
        <div className="row g-3">
          <Stat label="On time" value={mine?.stats.onTime} />
          <Stat label="Late" value={mine?.stats.late} />
          <Stat label="Absent or leave" value={mine?.stats.absentLeaves} />
          <Stat label="Pending leave" value={mine?.pendingLeaves} />
        </div>
        <div className="card mt-4">
          <div className="card-body">
            Today: <strong>{mine?.stats.todayStatus || "No punch yet"}</strong>
          </div>
        </div>
      </>
    )
  }

  return (
    <>
      <h1 className="h3">Dashboard</h1>
      <p className="text-muted">Today across the company.</p>
      <div className="row g-3">
        <Stat label="Active employees" value={company?.totalEmployees} />
        <Stat label="Present today" value={company?.presentToday} />
        <Stat label="On leave today" value={company?.onLeaveToday} />
        <Stat label="Pending leave" value={company?.pendingLeaves} />
      </div>
    </>
  )
}

function Stat({ label, value }: { label: string; value?: number }) {
  return (
    <div className="col-md-3">
      <div className="card h-100">
        <div className="card-body">
          <div className="text-muted small">{label}</div>
          <div className="display-6">{value ?? "—"}</div>
        </div>
      </div>
    </div>
  )
}
