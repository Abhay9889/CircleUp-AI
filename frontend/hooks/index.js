import { useState, useEffect, useCallback } from 'react'
import { notesApi, flashcardsApi, analyticsApi } from '../api'
import { useNoteStore } from '../store/noteStore'
import toast from 'react-hot-toast'

// ── useNotes ──────────────────────────────────────────────
export function useNotes() {
  const { notes, setNotes } = useNoteStore()
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    setLoading(true)
    try {
      const { data } = await notesApi.list()
      setNotes(data)
    } catch {
      toast.error('Failed to load notes')
    } finally {
      setLoading(false)
    }
  }, [setNotes])

  useEffect(() => { refresh() }, [refresh])

  return { notes, loading, refresh }
}

// ── useFlashcards ─────────────────────────────────────────
export function useFlashcards(dueOnly = true) {
  const [cards, setCards]   = useState([])
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    setLoading(true)
    try {
      const fn = dueOnly ? flashcardsApi.getDue : flashcardsApi.getAll
      const { data } = await fn()
      setCards(data)
    } catch {
      toast.error('Failed to load flashcards')
    } finally {
      setLoading(false)
    }
  }, [dueOnly])

  useEffect(() => { refresh() }, [refresh])

  return { cards, loading, refresh, setCards }
}

// ── useAnalytics ──────────────────────────────────────────
export function useAnalytics() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    analyticsApi.summary()
      .then(r => setSummary(r.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return { summary, loading }
}

// ── useStudySession ───────────────────────────────────────
export function useStudySession(sessionType) {
  const [sessionId, setSessionId] = useState(null)
  const startedAt = useState(Date.now())[0]

  const start = useCallback(async () => {
    try {
      const { data } = await analyticsApi.startSession({ sessionType })
      setSessionId(data.sessionId)
    } catch {}
  }, [sessionType])

  const end = useCallback(async () => {
    if (!sessionId) return
    const durationSec = Math.round((Date.now() - startedAt) / 1000)
    try {
      await analyticsApi.endSession(sessionId, durationSec)
    } catch {}
  }, [sessionId, startedAt])

  return { start, end }
}
