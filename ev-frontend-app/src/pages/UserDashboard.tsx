import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
	clearAuthSession,
	getAccountDetails,
	type AccountDetailsResponse,
} from '../services/auth'

function formatCurrency(value: number) {
	if (!Number.isFinite(value)) {
		return '0 RON'
	}

	return new Intl.NumberFormat('ro-RO', {
		style: 'currency',
		currency: 'RON',
		maximumFractionDigits: 2,
	}).format(value)
}

function UserDashboard() {
	const navigate = useNavigate()
	const [accountDetails, setAccountDetails] = useState<AccountDetailsResponse | null>(null)
	const [isLoading, setIsLoading] = useState(true)
	const [errorMessage, setErrorMessage] = useState('')

	const fullName = useMemo(() => {
		if (!accountDetails) {
			return ''
		}

		return `${accountDetails.firstName} ${accountDetails.lastName}`.trim()
	}, [accountDetails])

	useEffect(() => {
		let isCancelled = false

		const loadAccount = async () => {
			setIsLoading(true)
			setErrorMessage('')

			try {
				const response = await getAccountDetails()

				if (isCancelled) {
					return
				}

				setAccountDetails(response)
			} catch (error) {
				if (isCancelled) {
					return
				}

				setErrorMessage(
					error instanceof Error
						? error.message
						: 'Nu s-au putut incarca detaliile contului.',
				)
			} finally {
				if (!isCancelled) {
					setIsLoading(false)
				}
			}
		}

		void loadAccount()

		return () => {
			isCancelled = true
		}
	}, [])

	const handleLogout = () => {
		clearAuthSession()
		navigate('/login')
	}

	return (
		<main className="dashboard-page">
			<section className="dashboard-hero">
				<span className="auth-kicker">User Dashboard</span>
				<h1>Bine ai revenit{fullName ? `, ${fullName}` : ''}</h1>
				<p>Aici vezi datele contului din backend.</p>
				<div className="auth-chip-row">
					<span className="auth-chip">GET /account</span>
					<span className="auth-chip">profil utilizator</span>
				</div>
			</section>

			<section className="dashboard-card">
				<div className="dashboard-header-row">
					<h2>Date cont</h2>
					<button className="dashboard-logout" type="button" onClick={handleLogout}>
						Logout
					</button>
				</div>

				{isLoading ? <p>Se incarca detaliile contului...</p> : null}

				{!isLoading && accountDetails ? (
					<div className="dashboard-grid">
						<article className="dashboard-item">
							<span>Email</span>
							<strong>{accountDetails.email}</strong>
						</article>

						<article className="dashboard-item">
							<span>Nume complet</span>
							<strong>{fullName || '-'}</strong>
						</article>

						<article className="dashboard-item">
							<span>Telefon</span>
							<strong>{accountDetails.phoneNumber || '-'}</strong>
						</article>

						<article className="dashboard-item">
							<span>Sold</span>
							<strong>{formatCurrency(accountDetails.balance)}</strong>
						</article>
					</div>
				) : null}

				{errorMessage ? <p className="auth-message auth-message-error">{errorMessage}</p> : null}

				<p className="auth-footer">
					Ai nevoie de alt cont? <Link to="/login">Mergi la autentificare</Link>
				</p>
			</section>
		</main>
	)
}

export default UserDashboard
