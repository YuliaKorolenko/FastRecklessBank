// A short slice of the account UUID
export const shortId = (id) => id.slice(0, 8)

// "First Last · a2eb8f88" — the display label.
export const accountLabel = (a) => `${a.name} ${a.surname} · ${shortId(a.id)}`
