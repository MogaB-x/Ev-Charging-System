import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { loginUser, type LoginPayload } from '../services/auth'

const initialForm: LoginPayload = {
	email: '',
	password: '',
}

function LoginPage() {
	const navigate = useNavigate()
	const [form, setForm] = useState(initialForm)
	const [errorMessage, setErrorMessage] = useState('')
	const [successMessage, setSuccessMessage] = useState('')
	const [isSubmitting, setIsSubmitting] = useState(false)

	const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
		const { name, value } = event.target

		setForm((currentForm) => ({
			...currentForm,
			[name]: value,
		}))
	}

	const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault()
		setErrorMessage('')
		setSuccessMessage('')

		const trimmedForm = {
			email: form.email.trim(),
			password: form.password,
		}

		if (!trimmedForm.email || !trimmedForm.password) {
			setErrorMessage('Completeaza emailul si parola inainte sa trimiti formularul.')
			return
		}

		setIsSubmitting(true)

		try {
			await loginUser(trimmedForm)
			setSuccessMessage('Autentificare reusita.')
			window.setTimeout(() => {
				navigate('/dashboard')
			}, 700)
		} catch (error) {
			setErrorMessage(
				error instanceof Error ? error.message : 'A aparut o eroare la autentificare.',
			)
		} finally {
			setIsSubmitting(false)
		}
	}

	return (
		<main className="auth-page">
			<section className="auth-hero">
				<span className="auth-kicker">EV Charging System</span>
				<h1>Login</h1>
				<p>Introdu datele de autentificare pentru a accesa dashboard-ul.</p>
				<div className="auth-chip-row">
					<span className="auth-chip">POST /auth/login</span>
					<span className="auth-chip">email + parola</span>
				</div>
			</section>

			<section className="auth-card">
				<div className="auth-card-header">
					<h2>Autentificare</h2>
					<p>Introdu credentialele contului tau.</p>
				</div>

				<form className="auth-form" onSubmit={handleSubmit}>
					<label>
						<span>Email</span>
						<input
							name="email"
							type="email"
							placeholder="test@test.com"
							value={form.email}
							onChange={handleChange}
							autoComplete="email"
						/>
					</label>

					<label>
						<span>Parola</span>
						<input
							name="password"
							type="password"
							placeholder="test251"
							value={form.password}
							onChange={handleChange}
							autoComplete="current-password"
						/>
					</label>

					{errorMessage ? <p className="auth-message auth-message-error">{errorMessage}</p> : null}
					{successMessage ? (
						<p className="auth-message auth-message-success">{successMessage}</p>
					) : null}

					<button className="auth-submit" type="submit" disabled={isSubmitting}>
						{isSubmitting ? 'Se autentifica...' : 'Intra in cont'}
					</button>
				</form>

				<p className="auth-footer">
					Nu ai cont? <Link to="/register">Creeaza unul aici</Link>
				</p>
			</section>
		</main>
	)
}

export default LoginPage
