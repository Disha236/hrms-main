import { FormEvent, useEffect, useState } from "react"
import { api, errorMessage } from "../api"

type EmployeeRow = {
  id: string
  employeeId: string
  firstName: string
  lastName: string
  email: string
  position: string
  role: string
  employmentStatus: string
  department?: { id?: string; name?: string } | null
}

type Department = {
  id: string
  name: string
}

export function EmployeesPage() {
  const [rows, setRows] = useState<EmployeeRow[]>([])
  const [departments, setDepartments] = useState<Department[]>([])
  const [search, setSearch] = useState("")
  const [error, setError] = useState("")
  const [notice, setNotice] = useState("")
  const [open, setOpen] = useState(false)

  function load(query = search) {
    api
      .get("/api/employees", { params: { page: 1, limit: 50, search: query || undefined } })
      .then((response) => setRows(response.data.employees || []))
      .catch((err) => setError(errorMessage(err)))
  }

  useEffect(() => {
    load("")
    api
      .get("/api/departments")
      .then((response) => setDepartments(response.data.departments || []))
      .catch((err) => setError(errorMessage(err)))
  }, [])

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    const email = String(data.get("email") || "").trim()
    const departmentId = String(data.get("departmentId") || "")
    setError("")
    try {
      await api.post("/api/employees", {
        employeeId: String(data.get("employeeId") || "").trim(),
        firstName: String(data.get("firstName") || "").trim(),
        lastName: String(data.get("lastName") || "").trim(),
        email,
        password: String(data.get("password") || ""),
        position: String(data.get("position") || "").trim(),
        role: data.get("role"),
        departmentId: departmentId || undefined,
      })
      form.reset()
      setNotice(`${email} can sign in with the password you just set.`)
      setOpen(false)
      load()
    } catch (err) {
      setError(errorMessage(err, "Could not create employee"))
    }
  }

  async function assignDepartment(id: string, departmentId: string) {
    setError("")
    try {
      await api.put(`/api/employees/${id}`, { departmentId: departmentId || "none" })
      setNotice("Department updated")
      load()
    } catch (err) {
      setError(errorMessage(err, "Could not update department"))
    }
  }

  async function resetPassword(event: FormEvent<HTMLFormElement>, id: string) {
    event.preventDefault()
    const form = event.currentTarget
    const password = String(new FormData(form).get("password") || "")
    setError("")
    try {
      await api.put(`/api/employees/${id}`, { password })
      form.reset()
      setNotice("Password updated. Sign in with the employee email and this password.")
    } catch (err) {
      setError(errorMessage(err, "Could not update password"))
    }
  }

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1 className="h3 mb-0">Employees</h1>
        <button className="btn btn-primary" onClick={() => setOpen((value) => !value)}>
          {open ? "Close" : "Add employee"}
        </button>
      </div>
      {error && <div className="alert alert-danger">{error}</div>}
      {notice && <div className="alert alert-success">{notice}</div>}
      {open && (
        <form className="card card-body mb-3" autoComplete="off" onSubmit={create}>
          <div className="row g-3">
            <Field name="employeeId" label="Employee ID" />
            <Field name="firstName" label="First name" />
            <Field name="lastName" label="Last name" />
            <Field name="email" label="Email" type="email" />
            <Field name="password" label="Login password" type="password" autoComplete="new-password" />
            <Field name="position" label="Position" />
            <div className="col-md-4">
              <label className="form-label" htmlFor="departmentId">Department</label>
              <select className="form-select" id="departmentId" name="departmentId" defaultValue="" required>
                <option value="" disabled>Select department</option>
                {departments.map((department) => (
                  <option key={department.id} value={department.id}>{department.name}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label" htmlFor="role">Role</label>
              <select className="form-select" id="role" name="role" defaultValue="EMPLOYEE">
                <option value="EMPLOYEE">Employee</option>
                <option value="MANAGER">Manager</option>
                <option value="HR_MANAGER">HR Manager</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>
          <div className="mt-3">
            <button className="btn btn-primary">Save</button>
          </div>
        </form>
      )}
      <form
        className="d-flex gap-2 mb-3"
        onSubmit={(event) => {
          event.preventDefault()
          load()
        }}
      >
        <input
          className="form-control"
          placeholder="Search name or email"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
        <button className="btn btn-outline-secondary">Search</button>
      </form>
      <div className="card">
        <div className="table-responsive">
          <table className="table align-middle mb-0">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Position</th>
                <th>Role</th>
                <th>Password</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 && (
                <tr>
                  <td colSpan={7} className="text-muted">No employees</td>
                </tr>
              )}
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.employeeId}</td>
                  <td>{row.firstName} {row.lastName}</td>
                  <td>{row.email}</td>
                  <td style={{ minWidth: 180 }}>
                    <select
                      className="form-select form-select-sm"
                      value={row.department?.id || ""}
                      onChange={(event) => assignDepartment(row.id, event.target.value)}
                    >
                      <option value="">No department</option>
                      {departments.map((department) => (
                        <option key={department.id} value={department.id}>{department.name}</option>
                      ))}
                    </select>
                  </td>
                  <td>{row.position}</td>
                  <td>{row.role}</td>
                  <td>
                    <form className="d-flex gap-2" autoComplete="off" onSubmit={(event) => resetPassword(event, row.id)}>
                      <input
                        className="form-control form-control-sm"
                        name="password"
                        type="password"
                        autoComplete="new-password"
                        placeholder="New password"
                        required
                      />
                      <button className="btn btn-sm btn-outline-secondary">Set</button>
                    </form>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  )
}

function Field({
  name,
  label,
  type = "text",
  autoComplete,
}: {
  name: string
  label: string
  type?: string
  autoComplete?: string
}) {
  return (
    <div className="col-md-4">
      <label className="form-label" htmlFor={name}>{label}</label>
      <input className="form-control" id={name} name={name} type={type} autoComplete={autoComplete} required />
    </div>
  )
}
