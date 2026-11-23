import { FormEvent, useEffect, useState } from "react"
import { api, errorMessage } from "../api"

type Department = {
  id: string
  name: string
  description?: string
  _count?: { employees?: number }
}

export function DepartmentsPage() {
  const [rows, setRows] = useState<Department[]>([])
  const [error, setError] = useState("")

  function load() {
    api
      .get("/api/departments")
      .then((response) => setRows(response.data.departments || []))
      .catch((err) => setError(errorMessage(err)))
  }

  useEffect(() => {
    load()
  }, [])

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setError("")
    try {
      await api.post("/api/departments", {
        name: form.get("name"),
        description: form.get("description"),
      })
      event.currentTarget.reset()
      load()
    } catch (err) {
      setError(errorMessage(err, "Could not create department"))
    }
  }

  return (
    <>
      <h1 className="h3">Departments</h1>
      {error && <div className="alert alert-danger">{error}</div>}
      <form className="card card-body mb-3" onSubmit={create}>
        <div className="row g-3">
          <div className="col-md-4">
            <label className="form-label" htmlFor="name">Name</label>
            <input className="form-control" id="name" name="name" required />
          </div>
          <div className="col-md-6">
            <label className="form-label" htmlFor="description">Description</label>
            <input className="form-control" id="description" name="description" />
          </div>
          <div className="col-md-2 d-flex align-items-end">
            <button className="btn btn-primary w-100">Add</button>
          </div>
        </div>
      </form>
      <div className="card">
        <ul className="list-group list-group-flush">
          {rows.map((row) => (
            <li key={row.id} className="list-group-item d-flex justify-content-between">
              <div>
                <div className="fw-semibold">{row.name}</div>
                <div className="small text-muted">{row.description || "No description"}</div>
              </div>
              <span className="badge text-bg-secondary align-self-center">
                {row._count?.employees ?? 0} people
              </span>
            </li>
          ))}
        </ul>
      </div>
    </>
  )
}
