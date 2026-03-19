import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { analyticsApi, flashcardsApi, notesApi } from '../api'
import { useAuthStore } from '../store/authStore'
import { Flame, BookOpen, FileText, Clock, MessageSquare, ArrowRight, Loader2 } from 'lucide-react'

export default function DashboardPage() {
  const { user }      = useAuthStore()
  const [summary, setSummary]   = useState(null)
  const [dueCount, setDueCount] = useState(0)
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const [sumRes, dueRes] = await Promise.all([
          analyticsApi.summary(),
          flashcardsApi.getDueCount(),
        ])
        setSummary(sumRes.data)
        setDueCount(dueRes.data.dueCount)
      } catch {}
      finally { setLoading(false) }
    }
    load()
  }, [])

  const STATS = summary ? [
    { label: 'Study Streak',     value: `${summary.streak} days`,    icon: Flame,       color: 'text-orange-400' },
    { label: 'Cards Due Today',  value: dueCount,                     icon: BookOpen,    color: 'text-indigo-400' },
    { label: 'Total Notes',      value: summary.totalNotes,           icon: FileText,    color: 'text-green-400'  },
    { label: 'Study This Week',  value: `${Math.round((summary.weeklyStudySec||0)/60)} min`, icon: Clock, color: 'text-purple-400' },
  ] : []

  const QUICK_LINKS = [
    { to: '/ask',        label: 'Ask AI',          icon: MessageSquare, desc: 'Ask questions about your notes'   },
    { to: '/flashcards', label: 'Review Cards',     icon: BookOpen,      desc: `${dueCount} cards due today`      },
    { to: '/notes',      label: 'Upload Notes',     icon: FileText,      desc: 'Add new study material'           },
  ]

  return (
    <div className="p-6 space-y-8 animate-fade-in">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-[#cdd6f4]">
          Good {getGreeting()}, {user?.name?.split(' ')[0]} 👋
        </h1>
        <p className="text-sm text-[#6c7086] mt-1">Here's your study overview</p>
      </div>

      {/* Stats */}
      {loading ? (
        <div className="flex items-center gap-2 text-[#6c7086]">
          <Loader2 size={18} className="animate-spin" />
          <span className="text-sm">Loading stats...</span>
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {STATS.map(({ label, value, icon: Icon, color }) => (
            <div key={label} className="card flex items-center gap-4">
              <div className={`p-2.5 rounded-xl bg-[#1e1e2e] ${color}`}>
                <Icon size={20} />
              </div>
              <div>
                <p className="text-xl font-bold text-[#cdd6f4]">{value}</p>
                <p className="text-xs text-[#6c7086]">{label}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Quick links */}
      <div>
        <h2 className="text-base font-semibold text-[#a6adc8] mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {QUICK_LINKS.map(({ to, label, icon: Icon, desc }) => (
            <Link
              key={to}
              to={to}
              className="card group hover:border-indigo-500/50 transition-all flex items-center gap-4"
            >
              <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 group-hover:bg-indigo-500/20 transition-colors">
                <Icon size={22} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-semibold text-[#cdd6f4]">{label}</p>
                <p className="text-xs text-[#6c7086] truncate">{desc}</p>
              </div>
              <ArrowRight size={16} className="text-[#313244] group-hover:text-indigo-400 transition-colors shrink-0" />
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}

function getGreeting() {
  const h = new Date().getHours()
  if (h < 12) return 'morning'
  if (h < 17) return 'afternoon'
  return 'evening'
}
