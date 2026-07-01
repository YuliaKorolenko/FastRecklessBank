import { centsToDisplay } from '../money.js'

export default function AccountsTable({ accounts, selectedId, onSelect }) {
  if (accounts.length === 0) {
    return <p className="muted">No accounts yet — open one below.</p>
  }
  return (
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th className="amount">Balance</th>
        </tr>
      </thead>
      <tbody>
        {accounts.map((a) => (
          <tr
            key={a.id}
            className={a.id === selectedId ? 'selected' : ''}
            onClick={() => onSelect(a.id)}
          >
            <td>{a.name} {a.surname}</td>
            <td className="amount">${centsToDisplay(a.balanceCents)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
