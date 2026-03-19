import { useState, useRef } from 'react'
import { Mic, MicOff, Volume2, Loader2, Send } from 'lucide-react'
import { notesApi, aiApi } from '../api'
import { useEffect } from 'react'
import toast from 'react-hot-toast'

export default function VoicePage() {
  const [recording, setRecording]   = useState(false)
  const [transcript, setTranscript] = useState('')
  const [answer, setAnswer]         = useState('')
  const [notes, setNotes]           = useState([])
  const [noteId, setNoteId]         = useState('')
  const [loading, setLoading]       = useState(false)
  const mediaRef  = useRef(null)
  const chunksRef = useRef([])

  useEffect(() => {
    notesApi.list().then(r => {
      const ready = r.data.filter(n => n.processingStatus === 'READY')
      setNotes(ready)
      if (ready.length) setNoteId(ready[0].id)
    })
  }, [])

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      const mr = new MediaRecorder(stream)
      mediaRef.current = mr
      chunksRef.current = []
      mr.ondataavailable = (e) => chunksRef.current.push(e.data)
      mr.onstop = handleRecordingStop
      mr.start()
      setRecording(true)
      toast.success('Recording started — speak your question')
    } catch {
      toast.error('Microphone access denied')
    }
  }

  const stopRecording = () => {
    mediaRef.current?.stop()
    mediaRef.current?.stream?.getTracks().forEach(t => t.stop())
    setRecording(false)
  }

  const handleRecordingStop = async () => {
    const blob = new Blob(chunksRef.current, { type: 'audio/webm' })
    const fd   = new FormData()
    fd.append('audio', blob, 'voice.webm')
    fd.append('note_id', noteId)

    setLoading(true)
    try {
      // Send to FastAPI /voice/ask endpoint
      const res = await fetch('/ai/voice/ask', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('educircle-auth')
            ? JSON.parse(localStorage.getItem('educircle-auth'))?.state?.token
            : ''}`
        },
        body: fd,
      })
      const data = await res.json()
      setTranscript(data.transcript || '')
      setAnswer(data.answer || '')
    } catch {
      toast.error('Voice processing failed')
    } finally {
      setLoading(false)
    }
  }

  const handleTextAsk = async () => {
    if (!transcript.trim() || !noteId) return
    setLoading(true)
    try {
      const { data } = await aiApi.ask({ noteId: Number(noteId), question: transcript })
      setAnswer(data.answer)
    } catch { toast.error('Failed') }
    finally { setLoading(false) }
  }

  const speak = (text) => {
    if (!text) return
    const utt = new SpeechSynthesisUtterance(text)
    utt.rate = 0.9
    window.speechSynthesis.speak(utt)
    toast.success('Speaking answer...')
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
        <Mic size={24} className="text-indigo-400" /> Voice Study
      </h1>

      {/* Note selector */}
      <div className="card flex flex-wrap gap-3 items-center">
        <span className="text-sm text-[#a6adc8]">Note:</span>
        <select value={noteId} onChange={e => setNoteId(e.target.value)} className="input py-2 w-56">
          <option value="">Select note...</option>
          {notes.map(n => <option key={n.id} value={n.id}>{n.title}</option>)}
        </select>
      </div>

      {/* Record button */}
      <div className="card flex flex-col items-center gap-6 py-12">
        <button
          onClick={recording ? stopRecording : startRecording}
          disabled={loading || !noteId}
          className={`w-24 h-24 rounded-full flex items-center justify-center transition-all
            ${recording
              ? 'bg-red-500/20 border-2 border-red-500 text-red-400 animate-pulse'
              : 'bg-indigo-500/20 border-2 border-indigo-500 text-indigo-400 hover:bg-indigo-500/30'
            } disabled:opacity-40 disabled:cursor-not-allowed`}
        >
          {recording ? <MicOff size={36} /> : <Mic size={36} />}
        </button>
        <p className="text-sm text-[#6c7086]">
          {recording ? 'Recording… click to stop' : 'Click mic and ask a question about your note'}
        </p>
      </div>

      {/* Transcript */}
      {(transcript || loading) && (
        <div className="card space-y-3">
          <p className="text-xs text-[#6c7086] uppercase tracking-widest font-mono">Your Question</p>
          {loading ? (
            <div className="flex items-center gap-2 text-[#6c7086]">
              <Loader2 size={16} className="animate-spin" /> Transcribing...
            </div>
          ) : (
            <div className="flex gap-2">
              <p className="flex-1 text-[#cdd6f4]">{transcript}</p>
              <button onClick={handleTextAsk} className="btn-primary px-3 py-1.5 flex items-center gap-1 text-xs">
                <Send size={13} /> Ask
              </button>
            </div>
          )}
        </div>
      )}

      {/* Answer */}
      {answer && (
        <div className="card space-y-3">
          <div className="flex items-center justify-between">
            <p className="text-xs text-[#6c7086] uppercase tracking-widest font-mono">Answer</p>
            <button
              onClick={() => speak(answer)}
              className="flex items-center gap-1.5 text-xs text-indigo-400 hover:text-indigo-300"
            >
              <Volume2 size={14} /> Read aloud
            </button>
          </div>
          <p className="text-[#cdd6f4] leading-relaxed whitespace-pre-wrap">{answer}</p>
        </div>
      )}

      {/* Manual type fallback */}
      <div className="card">
        <p className="text-sm text-[#6c7086] mb-3">Or type your question:</p>
        <div className="flex gap-2">
          <input
            className="input flex-1"
            placeholder="Type a question..."
            value={transcript}
            onChange={e => setTranscript(e.target.value)}
          />
          <button onClick={handleTextAsk} disabled={loading || !noteId} className="btn-primary px-4">
            {loading ? <Loader2 size={15} className="animate-spin" /> : <Send size={15} />}
          </button>
        </div>
      </div>
    </div>
  )
}
