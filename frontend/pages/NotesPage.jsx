import { useEffect, useState, useRef } from 'react'
import { notesApi, aiApi } from '../api'
import { useNoteStore } from '../store/noteStore'
import { Upload, FileText, Trash2, Download, Loader2, Sparkles, Tag } from 'lucide-react'
import toast from 'react-hot-toast'
import clsx from 'clsx'

const STATUS_COLOR = {
  READY:      'bg-green-500/20 text-green-400',
  PENDING:    'bg-yellow-500/20 text-yellow-400',
  PROCESSING: 'bg-blue-500/20 text-blue-400',
  FAILED:     'bg-red-500/20 text-red-400',
}

export default function NotesPage() {
  const { notes, setNotes, addNote, removeNote } = useNoteStore()
  const [loading, setLoading]     = useState(true)
  const [uploading, setUploading] = useState(false)
  const [title, setTitle]         = useState('')
  const [file, setFile]           = useState(null)
  const fileRef                   = useRef()

  useEffect(() => {
    notesApi.list()
      .then(r => setNotes(r.data))
      .catch(() => toast.error('Failed to load notes'))
      .finally(() => setLoading(false))
  }, [])

  const handleUpload = async (e) => {
    e.preventDefault()
    if (!file || !title.trim()) { toast.error('Title and file required'); return }
    const fd = new FormData()
    fd.append('title', title.trim())
    fd.append('file', file)
    setUploading(true)
    try {
      const { data } = await notesApi.upload(fd)
      addNote(data)
      toast.success('Note uploaded!')
      setTitle(''); setFile(null); fileRef.current.value = ''
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed')
    } finally { setUploading(false) }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this note?')) return
    try {
      await notesApi.delete(id)
      removeNote(id)
      toast.success('Note deleted')
    } catch { toast.error('Delete failed') }
  }

  const handleDownload = async (id) => {
    try {
      const { data } = await notesApi.downloadUrl(id)
      window.open(data.url, '_blank')
    } catch { toast.error('Could not get download link') }
  }

  const handleSummarize = async (id) => {
    const toastId = toast.loading('Summarizing...')
    try {
      const { data } = await aiApi.summarize(id)
      toast.dismiss(toastId)
      toast.success('Summary ready!')
      alert(data.summary) // swap with a modal in production
    } catch {
      toast.dismiss(toastId)
      toast.error('Summarize failed')
    }
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold text-[#cdd6f4]">My Notes</h1>

      {/* Upload card */}
      <div className="card">
        <h2 className="text-base font-semibold text-[#cdd6f4] mb-4 flex items-center gap-2">
          <Upload size={18} className="text-indigo-400" /> Upload Note
        </h2>
        <form onSubmit={handleUpload} className="flex flex-col sm:flex-row gap-3">
          <input
            className="input flex-1"
            placeholder="Note title..."
            value={title}
            onChange={e => setTitle(e.target.value)}
          />
          <input
            ref={fileRef}
            type="file"
            accept=".pdf,.docx,.txt,.pptx"
            className="hidden"
            onChange={e => setFile(e.target.files[0])}
          />
          <button
            type="button"
            onClick={() => fileRef.current.click()}
            className="btn-ghost whitespace-nowrap"
          >
            {file ? file.name.slice(0,20)+'…' : 'Choose File'}
          </button>
          <button
            type="submit"
            disabled={uploading}
            className="btn-primary flex items-center gap-2 whitespace-nowrap"
          >
            {uploading && <Loader2 size={15} className="animate-spin" />}
            {uploading ? 'Uploading…' : 'Upload'}
          </button>
        </form>
        <p className="text-xs text-[#6c7086] mt-2">Supported: PDF, DOCX, TXT, PPTX · Max 50 MB</p>
      </div>

      {/* Notes list */}
      {loading ? (
        <div className="flex items-center gap-2 text-[#6c7086]">
          <Loader2 size={18} className="animate-spin" /> Loading notes...
        </div>
      ) : notes.length === 0 ? (
        <div className="card text-center py-16 text-[#6c7086]">
          <FileText size={48} className="mx-auto mb-3 opacity-30" />
          <p>No notes yet. Upload your first note above.</p>
        </div>
      ) : (
        <div className="grid gap-3">
          {notes.map(note => (
            <div key={note.id} className="card flex items-start gap-4 hover:border-indigo-500/30 transition-all">
              <div className="p-2.5 rounded-xl bg-[#1e1e2e] text-indigo-400 shrink-0">
                <FileText size={20} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-semibold text-[#cdd6f4] truncate">{note.title}</p>
                <div className="flex flex-wrap items-center gap-2 mt-1">
                  <span className={clsx('badge text-xs', STATUS_COLOR[note.processingStatus] || 'bg-gray-500/20 text-gray-400')}>
                    {note.processingStatus}
                  </span>
                  {note.fileType && (
                    <span className="text-xs text-[#6c7086] uppercase font-mono">{note.fileType}</span>
                  )}
                  {note.difficultyScore && (
                    <span className="text-xs text-[#6c7086]">Difficulty: {note.difficultyScore.toFixed(1)}</span>
                  )}
                </div>
                {note.tags?.length > 0 && (
                  <div className="flex flex-wrap gap-1 mt-2">
                    {note.tags.slice(0,5).map(tag => (
                      <span key={tag} className="inline-flex items-center gap-1 badge bg-[#1e1e2e] text-[#6c7086]">
                        <Tag size={10} />{tag}
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <div className="flex items-center gap-1 shrink-0">
                <button onClick={() => handleSummarize(note.id)} title="Summarize"
                  className="p-2 rounded-lg text-[#6c7086] hover:text-purple-400 hover:bg-[#1e1e2e] transition-all">
                  <Sparkles size={16} />
                </button>
                <button onClick={() => handleDownload(note.id)} title="Download"
                  className="p-2 rounded-lg text-[#6c7086] hover:text-indigo-400 hover:bg-[#1e1e2e] transition-all">
                  <Download size={16} />
                </button>
                <button onClick={() => handleDelete(note.id)} title="Delete"
                  className="p-2 rounded-lg text-[#6c7086] hover:text-red-400 hover:bg-[#1e1e2e] transition-all">
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
