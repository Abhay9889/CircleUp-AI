import { useState, useEffect } from 'react'
import { Settings, Loader2, Check } from 'lucide-react'
import api from '../api/axiosClient'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'

const LANGUAGES = [
  'english','hindi','tamil','telugu','bengali',
  'marathi','french','german','spanish','arabic',
]

export default function ProfilePage() {
  const { user, setUser }   = useAuthStore()
  const [form, setForm]     = useState({ name: '', preferredLanguage: 'english' })
  const [loading, setLoading] = useState(false)
  const [saved, setSaved]   = useState(false)

  useEffect(() => {
    api.get('/user/profile').then(r => {
      setForm({ name: r.data.name, preferredLanguage: r.data.preferredLanguage || 'english' })
    })
  }, [])

  const handleSave = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await api.patch('/user/profile', form)
      setUser({ ...user, name: data.name })
      setSaved(true)
      setTimeout(() => setSaved(false), 2000)
      toast.success('Profile updated!')
    } catch { toast.error('Update failed') }
    finally { setLoading(false) }
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in max-w-lg">
      <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
        <Settings size={24} className="text-indigo-400" /> Profile & Settings
      </h1>

      <div className="card space-y-5">
        <form onSubmit={handleSave} className="space-y-4">

          <div>
            <label className="block text-sm font-medium text-[#a6adc8] mb-1.5">Name</label>
            <input className="input" value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
          </div>

          <div>
            <label className="block text-sm font-medium text-[#a6adc8] mb-1.5">Email</label>
            <input className="input opacity-60 cursor-not-allowed" value={user?.email || ''} disabled />
            <p className="text-xs text-[#6c7086] mt-1">Email cannot be changed</p>
          </div>

          <div>
            <label className="block text-sm font-medium text-[#a6adc8] mb-1.5">
              Preferred Language
            </label>
            <select className="input" value={form.preferredLanguage}
              onChange={e => setForm(f => ({ ...f, preferredLanguage: e.target.value }))}>
              {LANGUAGES.map(l => (
                <option key={l} value={l}>{l.charAt(0).toUpperCase() + l.slice(1)}</option>
              ))}
            </select>
            <p className="text-xs text-[#6c7086] mt-1">
              AI responses will be in this language by default
            </p>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn-primary flex items-center gap-2"
          >
            {loading   && <Loader2 size={14} className="animate-spin" />}
            {saved     && <Check   size={14} />}
            {saved ? 'Saved!' : loading ? 'Saving...' : 'Save Changes'}
          </button>
        </form>
      </div>

      {/* Account stats */}
      <div className="card space-y-3">
        <h2 className="text-sm font-semibold text-[#a6adc8]">Account Info</h2>
        <div className="space-y-2 text-sm">
          {[
            { label: 'Role',          value: user?.role || '-'   },
            { label: 'Study Streak',  value: `${user?.studyStreak ?? 0} days` },
          ].map(({ label, value }) => (
            <div key={label} className="flex justify-between text-[#a6adc8]">
              <span className="text-[#6c7086]">{label}</span>
              <span>{value}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}