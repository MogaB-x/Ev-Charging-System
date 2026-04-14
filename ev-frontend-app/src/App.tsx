import { useEffect, useState } from 'react'
import './App.css'
import { Routes, Route, Link, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import UserDashboard from './pages/UserDashboard'
import HelloPage from './pages/HelloPage'
import { isAuthenticated as isUserAuthenticated } from './services/auth'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(isUserAuthenticated())

  useEffect(() => {
    const syncAuthState = () => {
      setIsAuthenticated(isUserAuthenticated())
    }

    window.addEventListener('auth-changed', syncAuthState)
    window.addEventListener('storage', syncAuthState)

    return () => {
      window.removeEventListener('auth-changed', syncAuthState)
      window.removeEventListener('storage', syncAuthState)
    }
  }, [])

  return (
    <div className="app-shell">
      <nav className="top-nav">
        <Link to="/">Home</Link>
        {isAuthenticated ? (
          <Link to="/dashboard">Dashboard</Link>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </nav>

      <Routes>
        <Route path="/" element={<HelloPage isAuthenticated={isAuthenticated} />} />
        <Route
          path="/login"
          element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />}
        />
        <Route
          path="/register"
          element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <RegisterPage />}
        />
        <Route
          path="/dashboard"
          element={isAuthenticated ? <UserDashboard /> : <Navigate to="/login" replace />}
        />
      </Routes>
    </div>
  )
}

export default App
