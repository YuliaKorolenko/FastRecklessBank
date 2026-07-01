import { useState } from 'react'
import { displayToCents } from '../money.js'

export default function CreateAccountForm({ onCreate }) {
  const [name, setName] = useState('')
  const [surname, setSurname] = useState('')
  const [amount, setAmount] = useState('')
  const [localError, setLocalError] = useState(null)

  async function submit(e) {
    e.preventDefault()
    setLocalError(null)
    let cents = 0
    if (amount.trim() !== '') {
      try {
        cents = displayToCents(amount)
      } catch (err) {
        setLocalError(err.message)
        return
      }
    }
    const err = await onCreate(name.trim(), surname.trim(), cents)
    if (err) {
      setLocalError(err)
      return
    }
    setName('')
    setSurname('')
    setAmount('')
  }

  return (
    <form onSubmit={submit} className="form">
      <input placeholder="First name" value={name} onChange={(e) => setName(e.target.value)} required />
      <input placeholder="Surname" value={surname} onChange={(e) => setSurname(e.target.value)} required />
      <input placeholder="Initial deposit (optional)" value={amount} onChange={(e) => setAmount(e.target.value)} />
      <button type="submit">Create</button>
      {localError && <span className="field-error">{localError}</span>}
    </form>
  )
}
