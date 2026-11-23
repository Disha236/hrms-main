import { useEffect, useState } from "react"
import { api, errorMessage } from "../api"

type LeaveRow = {
  id: string
  leaveType: string
  startDate: string
  endDate: string
  reason: string
  employee?: { firstName?: string; lastName?: string }
}

export function ApprovalsPage() {
  const [leaves, setLeaves] = useState<LeaveRow[]>([])
  const [error, setError] = useState("")

  function load() {
    api
      .get("/api/approvals")
      .then((response) => setLeaves(response.data.pendingLeaves || []))
      .catch((err) => setError(errorMessage(err)))
  }

  useEffect(() => {
    load()
  }, [])

  async function decide(id: string, action: "approve" | "reject") {
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
      <h1 className="h3">Approvals</h1>
      <p className="text-muted">Pending leave requests.</p>
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="card">
        <ul className="list-group list-group-flush">
          {leaves.length === 0 && <li className="list-group-item text-muted">Nothing waiting</li>}
          {leaves.map((leave) => (
            <li key={leave.id} className="list-group-item d-flex justify-content-between align-items-center gap-3">
              <div>
                <div className="fw-semibold">
                  {leave.employee?.firstName} {leave.employee?.lastName} · {leave.leaveType}
                </div>
                <div className="small text-muted">
                  {leave.startDate} to {leave.endDate}. {leave.reason}
                </div>
              </div>
              <div className="text-nowrap">
                <button className="btn btn-sm btn-success me-2" onClick={() => decide(leave.id, "approve")}>
                  Approve
                </button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => decide(leave.id, "reject")}>
                  Reject
                </button>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </>
  )
}
