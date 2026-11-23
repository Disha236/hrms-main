import { FormEvent, useEffect, useState } from "react"
import { api, errorMessage } from "../api"
import { useAuth } from "../auth"

type Announcement = {
  id: string
  title: string
  content: string
  priority?: string
  author?: { firstName?: string; lastName?: string }
}

export function AnnouncementsPage() {
  const { user } = useAuth()
  const canPost = user?.role === "ADMIN" || user?.role === "HR_MANAGER"
  const [rows, setRows] = useState<Announcement[]>([])
  const [error, setError] = useState("")

  function load() {
    api
      .get("/api/announcements")
      .then((response) => setRows(response.data.announcements || []))
      .catch((err) => setError(errorMessage(err)))
  }

  useEffect(() => {
    load()
  }, [])

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    setError("")
    try {
      await api.post("/api/announcements", {
        title: data.get("title"),
        content: data.get("content"),
        priority: data.get("priority"),
      })
      form.reset()
      load()
    } catch (err) {
      setError(errorMessage(err, "Could not publish announcement"))
    }
  }

  return (
    <>
      <h1 className="h3">Announcements</h1>
      {error && <div className="alert alert-danger">{error}</div>}
      {canPost && (
        <form className="card card-body mb-3" onSubmit={create}>
          <div className="mb-3">
            <label className="form-label" htmlFor="title">Title</label>
            <input className="form-control" id="title" name="title" required />
          </div>
          <div className="mb-3">
            <label className="form-label" htmlFor="content">Message</label>
            <textarea className="form-control" id="content" name="content" rows={3} required />
          </div>
          <div className="mb-3" style={{ maxWidth: 240 }}>
            <label className="form-label" htmlFor="priority">Priority</label>
            <select className="form-select" id="priority" name="priority" defaultValue="NORMAL">
              <option value="LOW">Low</option>
              <option value="NORMAL">Normal</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
          <button className="btn btn-primary">Publish</button>
        </form>
      )}
      <div className="d-grid gap-3">
        {rows.length === 0 && <div className="text-muted">No announcements</div>}
        {rows.map((row) => (
          <article key={row.id} className="card">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <h2 className="h5">{row.title}</h2>
                <span className="badge text-bg-secondary">{row.priority || "NORMAL"}</span>
              </div>
              <p className="mb-1">{row.content}</p>
              <div className="small text-muted">
                {row.author?.firstName} {row.author?.lastName}
              </div>
            </div>
          </article>
        ))}
      </div>
    </>
  )
}
