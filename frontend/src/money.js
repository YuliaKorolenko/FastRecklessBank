// The API stores money as integer cents; the UI shows/accepts a decimal amount.

export function centsToDisplay(cents) {
  return (cents / 100).toFixed(2)
}

// Parse a decimal string like "12.34" into integer cents. Throws on invalid input.
export function displayToCents(value) {
  const trimmed = String(value).trim()
  if (!/^\d+(\.\d{1,2})?$/.test(trimmed)) {
    throw new Error('Enter a valid amount, e.g. 12.34')
  }
  return Math.round(parseFloat(trimmed) * 100)
}
