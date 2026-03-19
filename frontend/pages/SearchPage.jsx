import { useState } from 'react'
import { Search, ExternalLink, Loader2, Globe } from 'lucide-react'
import api from '../api/axiosClient'
import toast from 'react-hot-toast'

export default function SearchPage() {
  const [query, setQuery]     = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!query.trim()) return
    setLoading(true)
    try {
      const { data } = await api.get('/search', { params: { q: query.trim(), count: 10 } })
      setResults(data.results || [])
      setSearched(true)
      if (data.error) toast.error(data.error)
    } catch (err) {
      if (err.response?.status === 429) {
        toast.error('Search limit reached. You have 50 searches per hour.')
      } else {
        toast.error('Search failed')
      }
    } finally { setLoading(false) }
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
        <Globe size={24} className="text-indigo-400" /> Web Search
      </h1>

      {/* Search bar */}
      <form onSubmit={handleSearch} className="flex gap-3">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-[#6c7086]" />
          <input
            className="input pl-10"
            placeholder="Search the web to support your studies..."
            value={query}
            onChange={e => setQuery(e.target.value)}
          />
        </div>
        <button type="submit" disabled={loading} className="btn-primary flex items-center gap-2">
          {loading && <Loader2 size={14} className="animate-spin" />}
          Search
        </button>
      </form>

      {/* Results */}
      {searched && results.length === 0 && !loading && (
        <div className="card text-center py-12 text-[#6c7086]">
          <p>No results found for "{query}"</p>
        </div>
      )}

      <div className="space-y-3">
        {results.map((r, i) => (
          <a
            key={i}
            href={r.url}
            target="_blank"
            rel="noreferrer"
            className="card block hover:border-indigo-500/50 transition-all group"
          >
            <div className="flex items-start justify-between gap-3">
              <div className="flex-1 min-w-0">
                <p className="font-semibold text-indigo-400 group-hover:text-indigo-300 transition-colors truncate">
                  {r.title}
                </p>
                <p className="text-xs text-[#6c7086] font-mono truncate mt-0.5">{r.url}</p>
                <p className="text-sm text-[#a6adc8] mt-2 line-clamp-2">{r.description}</p>
              </div>
              <ExternalLink size={14} className="text-[#313244] group-hover:text-indigo-400 transition-colors shrink-0 mt-1" />
            </div>
          </a>
        ))}
      </div>
    </div>
  )
}