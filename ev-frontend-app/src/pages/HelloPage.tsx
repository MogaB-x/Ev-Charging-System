import { Link } from 'react-router-dom'

type HelloPageProps = {
	isAuthenticated: boolean
}

function HelloPage({ isAuthenticated }: HelloPageProps) {
	return (
		<main className="hello-page">
			<section className="hello-card">
				<span className="auth-kicker">EV Charging System</span>
				<h1>Bine ai venit!</h1>
				<p>Aceasta este pagina de start. Te poti autentifica sau intra in dashboard.</p>

				{isAuthenticated ? (
					<div className="hello-ribbon hello-ribbon-success">Esti autentificat.</div>
				) : (
					<div className="hello-ribbon">Nu esti autentificat.</div>
				)}

				<div className="hello-actions">
					{isAuthenticated ? (
						<Link className="auth-submit auth-submit-link" to="/dashboard">
							Deschide dashboard
						</Link>
					) : (
						<>
							<Link className="auth-submit auth-submit-link" to="/login">
								Login
							</Link>
							<Link className="hello-secondary" to="/register">
								Register
							</Link>
						</>
					)}
				</div>
			</section>
		</main>
	)
}

export default HelloPage
