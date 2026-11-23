import { FormEvent, useEffect, useState } from "react"
import { api, errorMessage } from "../api"
import { useAuth } from "../auth"

type LeaveRow = {
  id: string
  leaveType: string
  startDate: string
  endDate: string
  totalDays: number
  status: string
  reason: string
  employee?: { firstName?: string; lastName?: string }
}

export function LeavesPage() {
  const { user } = useAuth()
  const canReview = user?.role !== "EMPLOYEE"
  const [rows, setRows] = useState<LeaveRow[]>([])
  const [error, setError] = useState("")

  function load() {
    api
      .get("/api/leaves", { params: { page: 1, limit: 50 } })
      .then((response) => setRows(response.data.requests || []))
      .catch((err) => setError(errorMessage(err)))
  }

  useEffect(() => {
    load()
  }, [])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    setError("")
    try {
      await api.post("/api/leaves", {
        leaveType: data.get("leaveType"),
        startDate: data.get("startDate"),
        endDate: data.get("endDate"),
        reason: data.get("reason"),
      })
      form.reset()
      load()
    } catch (err) {
      setError(errorMessage(err, "Could not submit leave"))
    }
  }

  async function decide(id: string, action: "approve" | "reject") {
    setError("")
    try {
      if (action === "approve") await api.post(`/api/leaves/${id}/approve`)
      else await api.post(`/api/leaves/${id}/reject`, { rejectedReason: "Rejected" })
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <>
      <h1 className="h3">Leave</h1>
      {error && <div className="alert alert-danger">{error}</div>}
      <form className="card card-body mb-3" onSubmit={submit}>
        <div className="row g-3">
          <div className="col-md-3">
            <label className="form-label" htmlFor="leaveType">Type</label>
            <select className="form-select" id="leaveType" name="leaveType" defaultValue="ANNUAL">
              <option value="ANNUAL">Annual</option>
              <option value="SICK">Sick</option>
              <option value="CASUAL">Casual</option>
              <option value="UNPAID">Unpaid</option>
            </select>
          </div>
          <div className="col-md-2">
            <label className="form-label" htmlFor="startDate">From</label>
            <input className="form-control" id="startDate" name="startDate" type="date" required />
          </div>
          <div className="col-md-2">
            <label className="form-label" htmlFor="endDate">To</label>
            <input className="form-control" id="endDate" name="endDate" type="date" required />
          </div>
          <div className="col-md-3">
            <label className="form-label" htmlFor="reason">Reason</label>
            <input className="form-control" id="reason" name="reason" required />
          </div>
          <div className="col-md-2 d-flex align-items-end">
            <button className="btn btn-primary w-100">Request</button>
          </div>
        </div>
      </form>
      <div className="card">
        <div className="table-responsive">
          <table className="table mb-0">
            <thead>
              <tr>
                {canReview && <th>Employee</th>}
                <th>Type</th>
                <th>Dates</th>
                <th>Days</th>
                <th>Status</th>
                <th>Reason</th>
                {canReview && <th />}
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 && (
                <tr>
                  <td colSpan={canReview ? 7 : 5} className="text-muted">No leave requests</td>
                </tr>
              )}
              {rows.map((row) => (
                <tr key={row.id}>
                  {canReview && <td>{row.employee?.firstName} {row.employee?.lastName}</td>}
                  <td>{row.leaveType}</td>
                  <td>{row.startDate} to {row.endDate}</td>
                  <td>{row.totalDays}</td>
                  <td>{row.status}</td>
                  <td>{row.reason}</td>
                  {canReview && (
                    <td className="text-nowrap">
                      {row.status === "PENDING" && (
                        <>
                          <button className="btn btn-sm btn-success me-2" onClick={() => decide(row.id, "approve")}>
                            Approve
                          </button>
                          <button className="btn btn-sm btn-outline-danger" onClick={() => decide(row.id, "reject")}>
                            Reject
                          </button>
                        </>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  )
}
