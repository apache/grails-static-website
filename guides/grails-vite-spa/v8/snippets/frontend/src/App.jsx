import { useEffect, useState } from 'react'

/**
 * Single-screen SPA: lists books from the Grails backend's /api/books
 * and lets the user add a new one.
 *
 * fetch('/api/books') resolves through the Vite proxy in dev and
 * directly (same-origin) from the bundled SPA in prod.
 */
export default function App() {
  const [books, setBooks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetch('/api/books')
      .then((r) => (r.ok ? r.json() : Promise.reject(r.statusText)))
      .then((data) => setBooks(data.items || []))
      .catch((e) => setError(String(e)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="container">
      <h1>Library</h1>
      {loading && <p>Loading...</p>}
      {error && <p className="error">Error: {error}</p>}
      {!loading && !error && (
        <ul>
          {books.map((b) => (
            <li key={b.id}>
              <strong>{b.title}</strong> by {b.author} ({b.isbn})
            </li>
          ))}
          {books.length === 0 && <li>No books yet.</li>}
        </ul>
      )}
    </main>
  )
}
