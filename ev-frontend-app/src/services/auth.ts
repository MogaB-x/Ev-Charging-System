const AUTH_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/auth'
const ACCOUNT_BASE_URL = import.meta.env.VITE_ACCOUNT_BASE_URL ?? '/account'
const AUTH_TOKEN_KEY = 'authToken'

export type LoginPayload = {
	email: string
	password: string
}

export type RegisterPayload = {
	email: string
	password: string
	firstName: string
	lastName: string
	phoneNumber: string
}

export type AccountDetailsResponse = {
	email: string
	firstName: string
	lastName: string
	phoneNumber: string
	balance: number
}

type AccountDetailsRawResponse = {
	email: string
	firstName: string
	lastName: string
	phoneNumber: string
	balance: number | string
}

type ApiErrorBody = {
	message?: string
}

function notifyAuthChanged() {
	if (typeof window !== 'undefined') {
		window.dispatchEvent(new Event('auth-changed'))
	}
}

function normalizeToken(rawToken: string) {
	const normalizedToken = rawToken.trim()

	if (normalizedToken.toLowerCase().startsWith('bearer ')) {
		return normalizedToken.slice(7).trim()
	}

	return normalizedToken
}

function saveAuthToken(rawToken: string) {
	const token = normalizeToken(rawToken)
	if (!token) {
		return
	}

	localStorage.setItem(AUTH_TOKEN_KEY, token)
	notifyAuthChanged()
}

function tryReadTokenFromResponseBody(responseBody: unknown) {
	if (!responseBody || typeof responseBody !== 'object') {
		return undefined
	}

	const directToken =
		'accessToken' in responseBody
			? responseBody.accessToken
			: 'token' in responseBody
				? responseBody.token
				: 'jwt' in responseBody
					? responseBody.jwt
					: undefined

	if (typeof directToken === 'string') {
		return directToken
	}

	if ('data' in responseBody && responseBody.data && typeof responseBody.data === 'object') {
		const nestedToken =
			'accessToken' in responseBody.data
				? responseBody.data.accessToken
				: 'token' in responseBody.data
					? responseBody.data.token
					: 'jwt' in responseBody.data
						? responseBody.data.jwt
						: undefined

		if (typeof nestedToken === 'string') {
			return nestedToken
		}
	}

	return undefined
}

function getAuthorizedHeaders() {
	const token = localStorage.getItem(AUTH_TOKEN_KEY)
	const headers: Record<string, string> = {
		Accept: 'application/json',
	}

	if (token) {
		headers.Authorization = `Bearer ${normalizeToken(token)}`
	}

	return headers
}

async function parseResponse(response: Response) {
	const contentType = response.headers.get('content-type') ?? ''
	const responseBody = contentType.includes('application/json')
		? await response.json().catch(() => null)
		: await response.text().catch(() => '')

	if (!response.ok) {
		const errorMessage =
			typeof responseBody === 'string'
				? responseBody.trim()
				: (responseBody as ApiErrorBody | null)?.message

		throw new Error(errorMessage || `Nu s-a putut finaliza cererea. [${response.status}]`)
	}

	return responseBody
}

function mapAccountDetails(response: AccountDetailsRawResponse): AccountDetailsResponse {
	return {
		email: response.email,
		firstName: response.firstName,
		lastName: response.lastName,
		phoneNumber: response.phoneNumber,
		balance: Number(response.balance),
	}
}

export function isAuthenticated() {
	return Boolean(localStorage.getItem(AUTH_TOKEN_KEY))
}

export function clearAuthSession() {
	localStorage.removeItem(AUTH_TOKEN_KEY)
	notifyAuthChanged()
}

export async function loginUser(payload: LoginPayload) {
	try {
		const response = await fetch(`${AUTH_BASE_URL}/login`, {
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json',
				Accept: 'application/json',
			},
			body: JSON.stringify(payload),
		})

		const responseBody = await parseResponse(response)
		const headerToken = response.headers.get('authorization')
		const bodyToken = tryReadTokenFromResponseBody(responseBody)
		const token = headerToken ?? bodyToken

		if (typeof token === 'string' && token.trim()) {
			saveAuthToken(token)
		} else {
			notifyAuthChanged()
		}

		return responseBody
	} catch (error) {
		if (error instanceof TypeError) {
			throw new Error(
				'Nu s-a putut contacta backend-ul. Verifica daca serverul ruleaza si daca proxy-ul Vite este pornit.',
			)
		}

		throw error
	}
}

export async function registerUser(payload: RegisterPayload) {
	try {
		const response = await fetch(`${AUTH_BASE_URL}/register`, {
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json',
				Accept: 'application/json',
			},
			body: JSON.stringify(payload),
		})

		const responseBody = await parseResponse(response)
		const headerToken = response.headers.get('authorization')
		const bodyToken = tryReadTokenFromResponseBody(responseBody)
		const token = headerToken ?? bodyToken

		if (typeof token === 'string' && token.trim()) {
			saveAuthToken(token)
		} else {
			notifyAuthChanged()
		}

		return responseBody
	} catch (error) {
		if (error instanceof TypeError) {
			throw new Error(
				'Nu s-a putut contacta backend-ul. Verifica daca serverul ruleaza si daca proxy-ul Vite este pornit.',
			)
		}

		throw error
	}
}

export async function getAccountDetails() {
	try {
		const response = await fetch(ACCOUNT_BASE_URL, {
			method: 'GET',
			credentials: 'include',
			headers: getAuthorizedHeaders(),
		})

		const responseBody = (await parseResponse(response)) as AccountDetailsRawResponse
		return mapAccountDetails(responseBody)
	} catch (error) {
		if (error instanceof TypeError) {
			throw new Error(
				'Nu s-a putut contacta backend-ul. Verifica daca serverul ruleaza si daca proxy-ul Vite este pornit.',
			)
		}

		throw error
	}
}
