import { useState } from 'react'
import { displayToCents } from '../money.js'
import { accountLabel } from '../format.js'

export default function TransferForm({ accounts, onTransfer }) {
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [amount, setAmount] = useState('')
  const [localError, setLocalError] = useState(null)

  async function submit(e) {
    e.preventDefault()
    setLocalError(null)
    if (!from || !to) {
      setLocalError('Choose both accounts')
      return
    }
    if (from === to) {
      setLocalError('Cannot transfer to the same account')
      return
    }
    let cents
    try {
      cents = displayToCents(amount)
    } catch (err) {
      setLocalError(err.message)
      return
    }
    const err = await onTransfer(from, to, cents)
    if (err) {
      setLocalError(err)
      return
    }
    setAmount('')
  }

  return (
    <form onSubmit={submit} className="form">
      <select value={from} onChange={(e) => setFrom(e.target.value)}>
        <option value="">From…</option>
        {accounts.map((a) => (
          <option key={a.id} value={a.id}>{accountLabel(a)}</option>
        ))}
      </select>
      <select value={to} onChange={(e) => setTo(e.target.value)}>
        <option value="">To…</option>
        {accounts.map((a) => (
          <option key={a.id} value={a.id}>{accountLabel(a)}</option>
        ))}
      </select>
      <input placeholder="Amount" value={amount} onChange={(e) => setAmount(e.target.value)} />
      <button type="submit">Transfer</button>
      {localError && <span className="field-error">{localError}</span>}
    </form>
  )
}
