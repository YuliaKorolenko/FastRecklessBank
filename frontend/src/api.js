// Thin wrapper over the bank REST API. Base URL is '' (same origin) — in dev the
// Vite proxy forwards /api to the backend on :8080. On a non-2xx response we throw
// an Error carrying the backend's { code, message } so the UI can show it.
async function request(path, options = {}) {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  const text = await res.text()
  const body = text ? JSON.parse(text) : null
  if (!res.ok) {
    throw new Error(body?.message || `Request failed (${res.status})`)
  }
  return body
}

export const listAccounts = () => request('/api/accounts')

export const getAccount = (id) => request(`/api/accounts/${id}`)

export const createAccount = (name, surname, initialDepositCents) =>
  request('/api/accounts', {
    method: 'POST',
    body: JSON.stringify({ name, surname, initialDepositCents }),
  })

export const deposit = (id, amountCents) =>
  request(`/api/accounts/${id}/deposit`, {
    method: 'POST',
    body: JSON.stringify({ amountCents }),
  })

export const withdraw = (id, amountCents) =>
  request(`/api/accounts/${id}/withdraw`, {
    method: 'POST',
    body: JSON.stringify({ amountCents }),
  })

export const getTransfers = (id) => request(`/api/accounts/${id}/transfers`)

export const transfer = (fromAccountId, toAccountId, amountCents) =>
  request('/api/transfers', {
    method: 'POST',
    body: JSON.stringify({ fromAccountId, toAccountId, amountCents }),
  })
