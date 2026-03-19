import { useEffect, useState } from 'react'
import { notesApi, quizApi } from '../api'
import { ClipboardList, Loader2, CheckCircle, XCircle } from 'lucide-react'
import toast from 'react-hot-toast'
import clsx from 'clsx'

export default function QuizPage() {
  const [notes, setNotes]       = useState([])
  const [noteId, setNoteId]     = useState('')
  const [count, setCount]       = useState(10)
  const [qtype, setQtype]       = useState('MCQ')
  const [quiz, setQuiz]         = useState(null)
  const [answers, setAnswers]   = useState({})
  const [result, setResult]     = useState(null)
  const [loading, setLoading]   = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [questions, setQuestions]   = useState([])

  useEffect(() => {
    notesApi.list().then(r => {
      const allNotes = Array.isArray(r.data) ? r.data : []
      setNotes(allNotes)
      if (allNotes.length > 0) setNoteId(String(allNotes[0].id))
    })
  }, [])

  const handleGenerate = async () => {
    if (!noteId) { toast.error('Select a note'); return }
    setLoading(true); setQuiz(null); setResult(null); setAnswers({})
    try {
      const { data } = await quizApi.generate(noteId, { questionCount: count, quizType: qtype })
      setQuiz(data)
      const parsed = typeof data.questions === 'string'
        ? JSON.parse(data.questions) : data.questions
      setQuestions(parsed)
    } catch { toast.error('Quiz generation failed') }
    finally { setLoading(false) }
  }

  const handleSubmit = async () => {
    if (Object.keys(answers).length < questions.length) {
      toast.error('Answer all questions first'); return
    }
    const answersList = questions.map((_, i) => ({
      questionIndex: String(i),
      answer: answers[i] || ''
    }))
    setSubmitting(true)
    try {
      const { data } = await quizApi.submit(quiz.id, answersList)
      setResult(data)
      toast.success(`Score: ${data.score}/${data.total}`)
    } catch { toast.error('Submit failed') }
    finally { setSubmitting(false) }
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
        <ClipboardList size={24} className="text-indigo-400" /> Quiz
      </h1>

      {/* Generator */}
      {!quiz && (
        <div className="card space-y-4">
          <h2 className="text-base font-semibold text-[#cdd6f4]">Generate Quiz</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <select value={noteId} onChange={e => setNoteId(e.target.value)} className="input">
              <option value="">Select note...</option>
              {notes.map(n => <option key={n.id} value={n.id}>{n.title}</option>)}
            </select>
            <select value={qtype} onChange={e => setQtype(e.target.value)} className="input">
              {['MCQ','TRUE_FALSE','SHORT_ANSWER','MIXED'].map(t =>
                <option key={t} value={t}>{t.replace('_',' ')}</option>
              )}
            </select>
            <select value={count} onChange={e => setCount(Number(e.target.value))} className="input">
              {[5,10,15,20].map(n => <option key={n} value={n}>{n} Questions</option>)}
            </select>
          </div>
          <button onClick={handleGenerate} disabled={loading} className="btn-primary flex items-center gap-2">
            {loading && <Loader2 size={15} className="animate-spin" />}
            {loading ? 'Generating…' : 'Generate Quiz'}
          </button>
        </div>
      )}

      {/* Quiz questions */}
      {quiz && !result && (
        <div className="space-y-5">
          <div className="flex items-center justify-between">
            <p className="text-sm text-[#6c7086]">{questions.length} questions</p>
            <button onClick={() => setQuiz(null)} className="btn-ghost text-xs">← New Quiz</button>
          </div>

          {questions.map((q, i) => (
            <div key={i} className="card space-y-3">
              <p className="font-semibold text-[#cdd6f4]">
                <span className="text-indigo-400 font-mono mr-2">Q{i+1}.</span>{q.question}
              </p>
              {q.options?.length > 0 ? (
                <div className="space-y-2">
                  {q.options.map((opt, oi) => {
                    const letter = String.fromCharCode(65 + oi)
                    const selected = answers[i] === letter
                    return (
                      <button
                        key={oi}
                        onClick={() => setAnswers(a => ({ ...a, [i]: letter }))}
                        className={clsx(
                          'w-full text-left px-4 py-3 rounded-xl text-sm border transition-all',
                          selected
                            ? 'border-indigo-500 bg-indigo-500/20 text-indigo-300'
                            : 'border-[#313244] hover:border-indigo-500/50 text-[#a6adc8]'
                        )}
                      >
                        <span className="font-mono font-semibold mr-2">{letter})</span> {opt}
                      </button>
                    )
                  })}
                </div>
              ) : (
                <input
                  className="input"
                  placeholder="Your answer..."
                  value={answers[i] || ''}
                  onChange={e => setAnswers(a => ({ ...a, [i]: e.target.value }))}
                />
              )}
            </div>
          ))}

          <button onClick={handleSubmit} disabled={submitting} className="btn-primary w-full flex items-center justify-center gap-2">
            {submitting && <Loader2 size={15} className="animate-spin" />}
            Submit Quiz
          </button>
        </div>
      )}

      {/* Result */}
      {result && (
        <div className="card space-y-5">
          <div className="text-center">
            <p className="text-4xl font-bold text-indigo-400">{result.score}/{result.total}</p>
            <p className="text-[#6c7086] mt-1">
              {Math.round((result.score/result.total)*100)}% correct
            </p>
          </div>

          {result.feedback?.map((f, i) => (
            <div key={i} className={clsx('p-4 rounded-xl border text-sm',
              f.is_correct ? 'border-green-500/30 bg-green-500/5' : 'border-red-500/30 bg-red-500/5')}>
              <div className="flex items-start gap-2">
                {f.is_correct
                  ? <CheckCircle size={16} className="text-green-400 shrink-0 mt-0.5" />
                  : <XCircle    size={16} className="text-red-400    shrink-0 mt-0.5" />}
                <div>
                  <p className="text-[#cdd6f4] font-medium">{f.question}</p>
                  {!f.is_correct && (
                    <p className="text-red-400 text-xs mt-1">Your answer: {f.your_answer} · Correct: {f.correct}</p>
                  )}
                  {f.explanation && <p className="text-[#6c7086] text-xs mt-1">{f.explanation}</p>}
                </div>
              </div>
            </div>
          ))}

          <button onClick={() => { setQuiz(null); setResult(null) }} className="btn-ghost w-full">
            Take Another Quiz
          </button>
        </div>
      )}
    </div>
  )
}
