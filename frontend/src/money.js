// The API stores money as integer cents. Balances can exceed JavaScript's safe
// integer range (2^53), so cents are carried as strings and all math here uses
// BigInt to stay exact.

// Format a cent amount (string | number | bigint) as a decimal string, e.g. "1234.05".
export function centsToDisplay(cents) {
  let c = BigInt(cents)
  const negative = c < 0n
  if (negative) c = -c
  const whole = c / 100n
  const frac = (c % 100n).toString().padStart(2, '0')
  return `${negative ? '-' : ''}${whole}.${frac}`
}

// Parse a decimal string like "12.34" into a cents string. Throws on invalid input.
export function displayToCents(value) {
  const trimmed = String(value).trim()
  if (!/^\d+(\.\d{1,2})?$/.test(trimmed)) {
    throw new Error('Enter a valid amount, e.g. 12.34')
  }
  const [whole, frac = ''] = trimmed.split('.')
  const cents = BigInt(whole) * 100n + BigInt(frac.padEnd(2, '0'))
  return cents.toString()
}
