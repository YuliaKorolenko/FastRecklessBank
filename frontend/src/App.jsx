import { useCallback, useEffect, useMemo, useState } from 'react'
import * as api from './api.js'
import AccountsTable from './components/AccountsTable.jsx'
import CreateAccountForm from './components/CreateAccountForm.jsx'
import AccountActions from './components/AccountActions.jsx'
import TransferForm from './components/TransferForm.jsx'
import TransferHistory from './components/TransferHistory.jsx'

export default function App() {
  const [accounts, setAccounts] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [loadError, setLoadError] = useState(null) // only for failing to load the account list
  const [version, setVersion] = useState(0) // bumped after each mutation to refresh history

  const refresh = useCallback(async () => {
    try {
      const data = await api.listAccounts()
      setAccounts(data)
      setLoadError(null)
    } catch (e) {
      setLoadError(e.message)
    }
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  // Run an API action and return its error message (or null on success). The
  // calling form shows the message inline, so client-side validation and
  // server errors appear in the same place.
  const run = useCallback(
    async (action) => {
      try {
        await action()
        await refresh()
        setVersion((v) => v + 1)
        return null
      } catch (e) {
        return e.message
      }
    },
    [refresh],
  )

  const selected = useMemo(
    () => accounts.find((a) => a.id === selectedId) || null,
    [accounts, selectedId],
  )

  return (
    <div className="app">
      <header>
        <h1>Fast &amp; Reckless Bank</h1>
      </header>

      {loadError && <div className="banner error">{loadError}</div>}

      <main>
        <section className="panel">
          <h2>Accounts</h2>
          <AccountsTable accounts={accounts} selectedId={selectedId} onSelect={setSelectedId} />
        </section>

        <section className="panel">
          <h2>Open an account</h2>
          <CreateAccountForm onCreate={(name, surname, cents) => run(() => api.createAccount(name, surname, cents))} />
        </section>

        <section className="panel">
          <h2>Transfer money</h2>
          <TransferForm accounts={accounts} onTransfer={(from, to, cents) => run(() => api.transfer(from, to, cents))} />
        </section>

        {selected && (
          <section className="panel">
            <h2>{selected.name} {selected.surname}</h2>
            <AccountActions
              onDeposit={(cents) => run(() => api.deposit(selected.id, cents))}
              onWithdraw={(cents) => run(() => api.withdraw(selected.id, cents))}
            />
            <TransferHistory accountId={selected.id} accounts={accounts} reloadKey={version} />
          </section>
        )}
      </main>
    </div>
  )
}
