import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { registerUser, type RegisterPayload } from '../services/auth'

const initialForm: RegisterPayload = {
	email: '',
	password: '',
	firstName: '',
	lastName: '',
	phoneNumber: '',
}

function RegisterPage() {
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

		const trimmedForm: RegisterPayload = {
			email: form.email.trim(),
			password: form.password,
			firstName: form.firstName.trim(),
			lastName: form.lastName.trim(),
			phoneNumber: form.phoneNumber.trim(),
		}

		const hasEmptyField = Object.values(trimmedForm).some((value) => !value)
		if (hasEmptyField) {
			setErrorMessage('Completeaza toate campurile inainte sa trimiti formularul.')
			return
		}

		setIsSubmitting(true)

		try {
			await registerUser(trimmedForm)
			setSuccessMessage('Cont creat cu succes.')
			window.setTimeout(() => {
				navigate('/dashboard')
			}, 700)
		} catch (error) {
			setErrorMessage(error instanceof Error ? error.message : 'A aparut o eroare la inregistrare.')
		} finally {
			setIsSubmitting(false)
		}
	}

	return (
		<main className="auth-page">
			<section className="auth-hero">
				<span className="auth-kicker">EV Charging System</span>
				<h1>Register</h1>
				<p>Creeaza un cont nou pentru a accesa dashboard-ul.</p>
				<div className="auth-chip-row">
					<span className="auth-chip">POST /auth/register</span>
					<span className="auth-chip">date utilizator</span>
				</div>
			</section>

			<section className="auth-card">
				<div className="auth-card-header">
					<h2>Inregistrare</h2>
					<p>Completeaza datele pentru contul nou.</p>
				</div>

				<form className="auth-form" onSubmit={handleSubmit}>
					<div className="auth-grid">
						<label>
							<span>Prenume</span>
							<input
								name="firstName"
								type="text"
								placeholder="Prenume"
								value={form.firstName}
								onChange={handleChange}
								autoComplete="given-name"
							/>
						</label>

						<label>
							<span>Nume</span>
							<input
								name="lastName"
								type="text"
								placeholder="Nume"
								value={form.lastName}
								onChange={handleChange}
								autoComplete="family-name"
							/>
						</label>
					</div>

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
							placeholder="********"
							value={form.password}
							onChange={handleChange}
							autoComplete="new-password"
						/>
					</label>

					<label>
						<span>Telefon</span>
						<input
							name="phoneNumber"
							type="tel"
							placeholder="07xxxxxxxx"
							value={form.phoneNumber}
							onChange={handleChange}
							autoComplete="tel"
						/>
					</label>

					{errorMessage ? <p className="auth-message auth-message-error">{errorMessage}</p> : null}
					{successMessage ? <p className="auth-message auth-message-success">{successMessage}</p> : null}

					<button className="auth-submit" type="submit" disabled={isSubmitting}>
						{isSubmitting ? 'Se inregistreaza...' : 'Creeaza cont'}
					</button>
				</form>

				<p className="auth-footer">
					Ai deja cont? <Link to="/login">Mergi la login</Link>
				</p>
			</section>
		</main>
	)
}

export default RegisterPage
