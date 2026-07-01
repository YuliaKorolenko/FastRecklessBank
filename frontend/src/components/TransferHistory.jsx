import { useEffect, useState } from 'react'
import * as api from '../api.js'
import { centsToDisplay } from '../money.js'

export default function TransferHistory({ accountId, accounts, reloadKey }) {
  const [transfers, setTransfers] = useState([])
  const [error, setError] = useState(null)

  useEffect(() => {
    let active = true
    api
      .getTransfers(accountId)
      .then((data) => {
        if (active) {
          setTransfers(data)
          setError(null)
        }
      })
      .catch((e) => {
        if (active) setError(e.message)
      })
    return () => {
      active = false
    }
  }, [accountId, reloadKey])

  const nameFor = (id) => {
    const a = accounts?.find((x) => x.id === id)
    return a ? `${a.name} ${a.surname}` : `${id.slice(0, 8)}…`
  }

  return (
    <div className="history">
      <h3>Last outgoing transfers</h3>
      {error && <p className="field-error">{error}</p>}
      {transfers.length === 0 ? (
        <p className="muted">No outgoing transfers yet.</p>
      ) : (
        <ul>
          {transfers.map((t, i) => (
            <li key={i}>→ {nameFor(t.toAccountId)} &nbsp; ${centsToDisplay(t.amountCents)}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
