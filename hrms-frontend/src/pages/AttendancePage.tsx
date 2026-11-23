import { useEffect, useState } from "react"
import { api, errorMessage } from "../api"
import { useAuth } from "../auth"

type RecordRow = {
  id: string
  date: string
  status: string
  checkIn?: string
  checkOut?: string
  employee?: { firstName?: string; lastName?: string; employeeId?: string }
}

export function AttendancePage() {
  const { user } = useAuth()
  const own = user?.role === "EMPLOYEE"
  const [rows, setRows] = useState<RecordRow[]>([])
  const [error, setError] = useState("")
  const [notice, setNotice] = useState("")
  const [busy, setBusy] = useState(false)

  function load() {
    const request = own
      ? api.get("/api/attendance/calendar")
      : api.get("/api/attendance", { params: { page: 1, limit: 30 } })
    request
      .then((response) => setRows(response.data.records || []))
      .catch((err) => setError(errorMessage(err)))
  }

  useEffect(() => {
    load()
  }, [own])

  async function punch(type: "CHECK_IN" | "CHECK_OUT") {
    setBusy(true)
    setError("")
    try {
      await api.post("/api/attendance/punch", { type })
      setNotice(type === "CHECK_IN" ? "Checked in" : "Checked out")
      load()
    } catch (err) {
      setError(errorMessage(err, "Could not mark attendance"))
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1 className="h3 mb-0">{own ? "My attendance" : "Attendance records"}</h1>
        <div className="d-flex gap-2">
          <button className="btn btn-primary" disabled={busy} onClick={() => punch("CHECK_IN")}>Check in</button>
          <button className="btn btn-outline-primary" disabled={busy} onClick={() => punch("CHECK_OUT")}>Check out</button>
        </div>
      </div>
      {error && <div className="alert alert-danger">{error}</div>}
      {notice && <div className="alert alert-success">{notice}</div>}
      <div className="card">
        <div className="table-responsive">
          <table className="table mb-0">
            <thead>
              <tr>
                {!own && <th>Employee</th>}
                <th>Date</th>
                <th>Status</th>
                <th>Check in</th>
                <th>Check out</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 && (
                <tr>
                  <td colSpan={own ? 4 : 5} className="text-muted">No records</td>
                </tr>
              )}
              {rows.map((row) => (
                <tr key={row.id}>
                  {!own && (
                    <td>
                      {row.employee?.firstName} {row.employee?.lastName}
                      <div className="small text-muted">{row.employee?.employeeId}</div>
                    </td>
                  )}
                  <td>{formatWhen(row.date)}</td>
                  <td>{row.status}</td>
                  <td>{formatWhen(row.checkIn)}</td>
                  <td>{formatWhen(row.checkOut)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  )
}

function formatWhen(value?: string) {
  if (!value) return "—"
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return value.includes("T") ? date.toLocaleString() : date.toLocaleDateString()
}
