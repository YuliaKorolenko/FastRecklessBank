import { useState } from 'react'
import { displayToCents } from '../money.js'

export default function AccountActions({ onDeposit, onWithdraw }) {
  const [amount, setAmount] = useState('')
  const [localError, setLocalError] = useState(null)

  async function act(fn) {
    setLocalError(null)
    let cents
    try {
      cents = displayToCents(amount)
    } catch (err) {
      setLocalError(err.message)
      return
    }
    const err = await fn(cents)
    if (err) {
      setLocalError(err)
      return
    }
    setAmount('')
  }

  return (
    <div className="actions">
      <input placeholder="Amount" value={amount} onChange={(e) => setAmount(e.target.value)} />
      <button onClick={() => act(onDeposit)}>Deposit</button>
      <button onClick={() => act(onWithdraw)}>Withdraw</button>
      {localError && <span className="field-error">{localError}</span>}
    </div>
  )
}
