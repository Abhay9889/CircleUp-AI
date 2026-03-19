import { useState, useEffect, useRef } from 'react'
import { notesApi, aiApi } from '../api'
import { MessageSquare, Globe, Send, Loader2, FileText, Bot, User } from 'lucide-react'
import toast from 'react-hot-toast'

export default function AskPage() {
  const [notes, setNotes]         = useState([])
  const [noteId, setNoteId]       = useState('')
  const [question, setQuestion]   = useState('')
  const [webSearch, setWebSearch] = useState(false)
  const [language, setLanguage]   = useState('english')
  const [loading, setLoading]     = useState(false)
  const [messages, setMessages]   = useState([])
  const bottomRef = useRef(null)

  useEffect(() => {
    notesApi.list()
      .then(r => {
        // FIX 1: show ALL notes, not just READY ones
        // (notes stay PENDING until FastAPI processes them)
        const allNotes = Array.isArray(r.data) ? r.data : (r.data?.content || r.data?.notes || [])
        setNotes(allNotes)
        if (allNotes.length > 0) setNoteId(String(allNotes[0].id))
      })
      .catch(() => toast.error('Failed to load notes'))
  }, [])

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  const handleAsk = async (e) => {
    e.preventDefault()
    if (!question.trim()) { toast.error('Type a question first'); return }
    if (!noteId)          { toast.error('Select a note first');   return }

    const userMsg = { role: 'user', text: question }
    setMessages(m => [...m, userMsg])
    const q = question.trim()
    setQuestion('')
    setLoading(true)

    try {
      const payload   = { noteId: Number(noteId), question: q, language }
      const fn        = webSearch ? aiApi.askWithSearch : aiApi.ask
      const { data }  = await fn(payload)

      setMessages(m => [...m, {
        role:       'assistant',
        text:       data.answer || data.response || 'No answer returned.',
        sources:    data.sources    || [],
        webSources: data.webSources || [],
      }])
    } catch (err) {
      toast.error('Failed to get answer — is FastAPI running?')
      setMessages(m => m.slice(0, -1))
    } finally {
      setLoading(false)
    }
  }

  const LANGUAGES = ['english','hindi','tamil','telugu','french','german','spanish','arabic']

  // FIX 2: send button enabled whenever there's text (note optional for web-search mode)
  const canSend = !loading && question.trim().length > 0

  return (
    <div className="flex flex-col h-full p-6 space-y-4 animate-fade-in">
      <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
        <MessageSquare size={24} className="text-indigo-400" /> Ask AI
      </h1>

      {/* Controls */}
      <div className="card flex flex-wrap gap-3 items-center">
        <div className="flex items-center gap-2">
          <FileText size={16} className="text-[#6c7086]" />
          <select
            value={noteId}
            onChange={e => setNoteId(e.target.value)}
            className="input py-2 w-52"
          >
            <option value="">Select note...</option>
            {notes.map(n => (
              <option key={n.id} value={String(n.id)}>
                {n.title}
                {n.processingStatus !== 'READY' ? ` (${n.processingStatus})` : ''}
              </option>
            ))}
          </select>
        </div>

        <select
          value={language}
          onChange={e => setLanguage(e.target.value)}
          className="input py-2 w-36"
        >
          {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
        </select>

        <label className="flex items-center gap-2 cursor-pointer select-none">
          <div
            onClick={() => setWebSearch(w => !w)}
            className={`relative w-10 h-5 rounded-full transition-colors ${webSearch ? 'bg-indigo-500' : 'bg-[#313244]'}`}
          >
            <div className={`absolute top-0.5 w-4 h-4 rounded-full bg-white transition-transform ${webSearch ? 'translate-x-5' : 'translate-x-0.5'}`} />
          </div>
          <Globe size={14} className={webSearch ? 'text-indigo-400' : 'text-[#6c7086]'} />
          <span className="text-sm text-[#a6adc8]">Web Search</span>
        </label>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto space-y-4 min-h-0">
        {messages.length === 0 && (
          <div className="text-center py-16 text-[#6c7086]">
            <Bot size={48} className="mx-auto mb-3 opacity-30" />
            <p>Select a note and ask anything about it</p>
            {notes.length === 0 && (
              <p className="text-xs mt-2 text-yellow-500/70">
                No notes found — upload a note from the Notes page first
              </p>
            )}
          </div>
        )}
        {messages.map((msg, i) => (
          <div key={i} className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            {msg.role === 'assistant' && (
              <div className="w-8 h-8 rounded-full bg-indigo-500/20 flex items-center justify-center shrink-0 mt-1">
                <Bot size={16} className="text-indigo-400" />
              </div>
            )}
            <div className={`max-w-2xl rounded-2xl px-4 py-3 text-sm leading-relaxed
              ${msg.role === 'user'
                ? 'bg-indigo-500/20 text-[#cdd6f4] rounded-tr-sm'
                : 'bg-[#1e1e2e] border border-[#313244] text-[#cdd6f4] rounded-tl-sm'}`}
            >
              <p className="whitespace-pre-wrap">{msg.text}</p>
              {msg.sources?.length > 0 && (
                <div className="mt-2 pt-2 border-t border-[#313244]">
                  <p className="text-xs text-[#6c7086] mb-1">From your notes:</p>
                  {msg.sources.slice(0,2).map((s, si) => (
                    <p key={si} className="text-xs text-[#6c7086] italic">"{s}"</p>
                  ))}
                </div>
              )}
              {msg.webSources?.length > 0 && (
                <div className="mt-2 pt-2 border-t border-[#313244]">
                  <p className="text-xs text-[#6c7086] mb-1 flex items-center gap-1">
                    <Globe size={10} /> Web sources:
                  </p>
                  {msg.webSources.slice(0,3).map((s, si) => (
                    <a key={si} href={s.url} target="_blank" rel="noreferrer"
                       className="block text-xs text-indigo-400 hover:underline truncate">
                      {s.title}
                    </a>
                  ))}
                </div>
              )}
            </div>
            {msg.role === 'user' && (
              <div className="w-8 h-8 rounded-full bg-[#313244] flex items-center justify-center shrink-0 mt-1">
                <User size={16} className="text-[#a6adc8]" />
              </div>
            )}
          </div>
        ))}
        {loading && (
          <div className="flex gap-3">
            <div className="w-8 h-8 rounded-full bg-indigo-500/20 flex items-center justify-center">
              <Bot size={16} className="text-indigo-400" />
            </div>
            <div className="bg-[#1e1e2e] border border-[#313244] rounded-2xl rounded-tl-sm px-4 py-3">
              <Loader2 size={16} className="animate-spin text-indigo-400" />
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <form onSubmit={handleAsk} className="flex gap-3">
        <input
          className="input flex-1"
          placeholder={notes.length === 0 ? "Upload a note first..." : "Ask a question about your note..."}
          value={question}
          onChange={e => setQuestion(e.target.value)}
          onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) handleAsk(e) }}
          disabled={loading}
        />
        {/* FIX 2: only disabled when loading or no text typed */}
        <button type="submit" disabled={!canSend} className="btn-primary px-4">
          {loading ? <Loader2 size={16} className="animate-spin" /> : <Send size={16} />}
        </button>
      </form>
    </div>
  )
}