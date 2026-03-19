import { useEffect, useState } from 'react'
import { flashcardsApi, notesApi } from '../api'
import { BookOpen, RefreshCw, ChevronLeft, ChevronRight, Loader2, Zap } from 'lucide-react'
import toast from 'react-hot-toast'
import clsx from 'clsx'

const QUALITY_BUTTONS = [
  { q: 0, label: 'Blackout',  color: 'bg-red-500/20    text-red-400    hover:bg-red-500/30'    },
  { q: 2, label: 'Hard',      color: 'bg-orange-500/20 text-orange-400 hover:bg-orange-500/30' },
  { q: 3, label: 'Good',      color: 'bg-yellow-500/20 text-yellow-400 hover:bg-yellow-500/30' },
  { q: 5, label: 'Perfect',   color: 'bg-green-500/20  text-green-400  hover:bg-green-500/30'  },
]

export default function FlashcardsPage() {
  const [cards, setCards]         = useState([])
  const [idx, setIdx]             = useState(0)
  const [flipped, setFlipped]     = useState(false)
  const [loading, setLoading]     = useState(true)
  const [reviewing, setReviewing] = useState(false)
  const [notes, setNotes]         = useState([])
  const [generating, setGenerating] = useState(false)
  const [genNoteId, setGenNoteId]   = useState('')
  const [dueOnly, setDueOnly]     = useState(true)

  const loadCards = async () => {
    setLoading(true)
    try {
      const fn = dueOnly ? flashcardsApi.getDue : flashcardsApi.getAll
      const { data } = await fn()
      setCards(data)
      setIdx(0); setFlipped(false)
    } catch { toast.error('Failed to load flashcards') }
    finally { setLoading(false) }
  }

  useEffect(() => { loadCards() }, [dueOnly])
  useEffect(() => {
    notesApi.list().then(r => {
      const allNotes = Array.isArray(r.data) ? r.data : []
      setNotes(allNotes)
      if (allNotes.length > 0) setNoteId(String(allNotes[0].id))
    })
  }, [])

  const current = cards[idx]

  const handleReview = async (quality) => {
    if (!current) return
    setReviewing(true)
    try {
      await flashcardsApi.review(current.id, quality)
      toast.success(quality >= 3 ? '✅ Correct!' : '🔁 Will repeat soon')
      const next = cards.filter((_, i) => i !== idx)
      setCards(next)
      setIdx(Math.min(idx, next.length - 1))
      setFlipped(false)
    } catch { toast.error('Review failed') }
    finally { setReviewing(false) }
  }

  const handleGenerate = async () => {
    if (!genNoteId) return
    setGenerating(true)
    try {
      await flashcardsApi.generate(genNoteId, 10)
      toast.success('Flashcards generated!')
      loadCards()
    } catch { toast.error('Generation failed') }
    finally { setGenerating(false) }
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
          <BookOpen size={24} className="text-indigo-400" /> Flashcards
        </h1>
        <div className="flex items-center gap-3">
          <label className="flex items-center gap-2 cursor-pointer text-sm text-[#a6adc8]">
            <input type="checkbox" checked={dueOnly} onChange={e => setDueOnly(e.target.checked)}
              className="accent-indigo-500" />
            Due only
          </label>
          <button onClick={loadCards} className="btn-ghost flex items-center gap-2">
            <RefreshCw size={14} /> Refresh
          </button>
        </div>
      </div>

      {/* Generate section */}
      <div className="card flex flex-wrap gap-3 items-center">
        <Zap size={16} className="text-yellow-400 shrink-0" />
        <span className="text-sm text-[#a6adc8]">Generate from note:</span>
        <select value={genNoteId} onChange={e => setGenNoteId(e.target.value)}
          className="input py-2 w-52">
          {notes.map(n => <option key={n.id} value={n.id}>{n.title}</option>)}
        </select>
        <button onClick={handleGenerate} disabled={generating} className="btn-primary flex items-center gap-2">
          {generating && <Loader2 size={14} className="animate-spin" />}
          Generate 10 Cards
        </button>
      </div>

      {loading ? (
        <div className="flex items-center gap-2 text-[#6c7086]">
          <Loader2 size={18} className="animate-spin" /> Loading...
        </div>
      ) : cards.length === 0 ? (
        <div className="card text-center py-20 text-[#6c7086]">
          <BookOpen size={48} className="mx-auto mb-3 opacity-30" />
          <p>{dueOnly ? 'No cards due today! Great job 🎉' : 'No flashcards yet. Generate some above.'}</p>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Progress */}
          <div className="flex items-center gap-3 text-sm text-[#6c7086]">
            <span>{idx + 1} / {cards.length}</span>
            <div className="flex-1 h-1.5 bg-[#1e1e2e] rounded-full overflow-hidden">
              <div
                className="h-full bg-indigo-500 rounded-full transition-all"
                style={{ width: `${((idx + 1) / cards.length) * 100}%` }}
              />
            </div>
          </div>

          {/* Flip card */}
          <div
            className={clsx('flip-card w-full max-w-xl mx-auto cursor-pointer', { flipped })}
            style={{ height: 260 }}
            onClick={() => setFlipped(f => !f)}
          >
            <div className="flip-card-inner">
              {/* Front */}
              <div className="flip-card-front bg-[#1e1e2e] border border-[#313244] flex flex-col items-center justify-center p-8 text-center">
                <p className="text-xs text-[#6c7086] mb-4 font-mono uppercase tracking-widest">Question</p>
                <p className="text-lg font-semibold text-[#cdd6f4]">{current?.question}</p>
                <p className="text-xs text-[#6c7086] mt-6">Click to reveal answer</p>
              </div>
              {/* Back */}
              <div className="flip-card-back bg-indigo-500/10 border border-indigo-500/30 flex flex-col items-center justify-center p-8 text-center">
                <p className="text-xs text-indigo-400 mb-4 font-mono uppercase tracking-widest">Answer</p>
                <p className="text-lg font-semibold text-[#cdd6f4]">{current?.answer}</p>
              </div>
            </div>
          </div>

          {/* Quality buttons (only show after flip) */}
          {flipped && (
            <div className="flex flex-wrap justify-center gap-3 animate-slide-up">
              <p className="w-full text-center text-sm text-[#6c7086] mb-1">How well did you know this?</p>
              {QUALITY_BUTTONS.map(({ q, label, color }) => (
                <button
                  key={q}
                  disabled={reviewing}
                  onClick={() => handleReview(q)}
                  className={`px-5 py-2.5 rounded-xl text-sm font-semibold transition-all ${color}`}
                >
                  {label}
                </button>
              ))}
            </div>
          )}

          {/* Navigation */}
          <div className="flex justify-center gap-3">
            <button
              onClick={() => { setIdx(i => Math.max(0, i - 1)); setFlipped(false) }}
              disabled={idx === 0}
              className="btn-ghost flex items-center gap-1"
            >
              <ChevronLeft size={16} /> Prev
            </button>
            <button
              onClick={() => { setIdx(i => Math.min(cards.length - 1, i + 1)); setFlipped(false) }}
              disabled={idx === cards.length - 1}
              className="btn-ghost flex items-center gap-1"
            >
              Next <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
