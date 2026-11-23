import { FormEvent, useState } from "react"
import { Link, Navigate, useNavigate } from "react-router-dom"
import { errorMessage } from "../api"
import { homePath, useAuth } from "../auth"

export function HomePage() {
  const { user } = useAuth()
  if (user) return <Navigate to={homePath(user.role)} replace />

  return (
    <div className="min-vh-100 d-flex align-items-center bg-light">
      <div className="container">
        <div className="row align-items-center g-5">
          <div className="col-lg-7">
            <p className="text-primary fw-semibold mb-2">Human Resource Management</p>
            <h1 className="display-5 fw-semibold">Employees, attendance, and leave in one place.</h1>
            <p className="lead text-muted">
              Sign in with your work email. Admins and HR manage people and departments. Managers review requests.
              Employees see their own attendance and leave.
            </p>
            <Link className="btn btn-primary btn-lg" to="/login">
              Sign in
            </Link>
          </div>
          <div className="col-lg-5">
            <div className="card shadow-sm">
              <div className="card-body">
                <h2 className="h5">What you can do</h2>
                <ul className="mb-0">
                  <li>Admin and HR: employees, departments, attendance, approvals</li>
                  <li>Managers: team attendance and leave approvals</li>
                  <li>Employees: personal dashboard, attendance, leave requests</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState("admin@company.com")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)

  if (user) return <Navigate to={homePath(user.role)} replace />

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError("")
    try {
      const nextUser = await login(email, password)
      navigate(homePath(nextUser.role))
    } catch (err) {
      setError(errorMessage(err, "Could not sign in"))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="min-vh-100 d-flex align-items-center bg-light">
      <div className="container" style={{ maxWidth: 440 }}>
        <div className="card shadow-sm">
          <div className="card-body p-4">
            <h1 className="h4 mb-1">Sign in</h1>
            <p className="text-muted">Use the account created for this environment.</p>
            <form onSubmit={onSubmit} className="d-grid gap-3">
              {error && <div className="alert alert-danger py-2 mb-0">{error}</div>}
              <div>
                <label className="form-label" htmlFor="email">Email</label>
                <input
                  id="email"
                  type="email"
                  className="form-control"
                  autoComplete="username"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                />
              </div>
              <div>
                <label className="form-label" htmlFor="password">Password</label>
                <input
                  id="password"
                  type="password"
                  className="form-control"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                />
              </div>
              <button className="btn btn-primary" disabled={busy}>
                {busy ? "Signing in..." : "Sign in"}
              </button>
            </form>
            <p className="small text-muted mt-3 mb-0">
              Admin: admin@company.com / Admin@123
            </p>
            <Link className="small" to="/">Back</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
