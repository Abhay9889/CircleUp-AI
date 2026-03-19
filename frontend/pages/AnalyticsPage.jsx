import { useEffect, useState } from 'react'
import { analyticsApi } from '../api'
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid
} from 'recharts'
import { BarChart2, Flame, Clock, BookOpen, FileText, Loader2 } from 'lucide-react'
import toast from 'react-hot-toast'

export default function AnalyticsPage() {
  const [summary, setSummary]   = useState(null)
  const [heatmap, setHeatmap]   = useState({})
  const [weekly, setWeekly]     = useState(null)
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const [s, h, w] = await Promise.all([
          analyticsApi.summary(),
          analyticsApi.heatmap(),
          analyticsApi.weekly(),
        ])
        setSummary(s.data)
        setHeatmap(h.data.heatmap || {})
        setWeekly(w.data)
      } catch { toast.error('Failed to load analytics') }
      finally { setLoading(false) }
    }
    load()
  }, [])

  if (loading) return (
    <div className="p-6 flex items-center gap-2 text-[#6c7086]">
      <Loader2 size={18} className="animate-spin" /> Loading analytics...
    </div>
  )

  // Build bar chart data from weekly byDay
  const DAYS  = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY']
  const barData = DAYS.map(d => ({
    day:     d.slice(0,3),
    minutes: Math.round((weekly?.byDay?.[d] || 0) / 60),
  }))

  // Heatmap — last 12 weeks
  const today     = new Date()
  const heatCells = Array.from({ length: 84 }, (_, i) => {
    const d   = new Date(today)
    d.setDate(today.getDate() - (83 - i))
    const key = d.toISOString().split('T')[0]
    const val = heatmap[key] || 0
    return { date: key, minutes: Math.round(val / 60) }
  })

  const heatColor = (min) => {
    if (min === 0)  return 'bg-[#1e1e2e]'
    if (min < 15)   return 'bg-indigo-900/60'
    if (min < 30)   return 'bg-indigo-700/70'
    if (min < 60)   return 'bg-indigo-500/80'
    return 'bg-indigo-400'
  }

  const STATS = [
    { label: 'Study Streak',    value: `${summary?.streak ?? 0} days`,    icon: Flame,    color: 'text-orange-400' },
    { label: 'Weekly Study',    value: `${Math.round((summary?.weeklyStudySec || 0)/60)} min`, icon: Clock, color: 'text-indigo-400' },
    { label: 'Cards Due',       value: summary?.dueFlashcards ?? 0,        icon: BookOpen, color: 'text-green-400'  },
    { label: 'Total Notes',     value: summary?.totalNotes ?? 0,           icon: FileText, color: 'text-purple-400' },
  ]

  return (
    <div className="p-6 space-y-8 animate-fade-in">
      <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
        <BarChart2 size={24} className="text-indigo-400" /> Analytics
      </h1>

      {/* Stat cards */}
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

      {/* Study heatmap */}
      <div className="card space-y-4">
        <h2 className="text-base font-semibold text-[#cdd6f4]">Study Activity — Last 12 Weeks</h2>
        <div className="grid gap-1.5" style={{ gridTemplateColumns: 'repeat(12, 1fr)' }}>
          {Array.from({ length: 12 }, (_, week) => (
            <div key={week} className="flex flex-col gap-1">
              {heatCells.slice(week * 7, week * 7 + 7).map((cell, di) => (
                <div
                  key={di}
                  title={`${cell.date}: ${cell.minutes} min`}
                  className={`w-full aspect-square rounded-sm ${heatColor(cell.minutes)} transition-colors cursor-default`}
                />
              ))}
            </div>
          ))}
        </div>
        {/* Legend */}
        <div className="flex items-center gap-2 text-xs text-[#6c7086]">
          <span>Less</span>
          {['bg-[#1e1e2e]','bg-indigo-900/60','bg-indigo-700/70','bg-indigo-500/80','bg-indigo-400'].map(c => (
            <div key={c} className={`w-3.5 h-3.5 rounded-sm ${c}`} />
          ))}
          <span>More</span>
        </div>
      </div>

      {/* Weekly bar chart */}
      <div className="card space-y-4">
        <h2 className="text-base font-semibold text-[#cdd6f4]">Daily Study Time This Week</h2>
        <div className="h-48">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={barData} barSize={28}>
              <CartesianGrid strokeDasharray="3 3" stroke="#313244" vertical={false} />
              <XAxis dataKey="day" tick={{ fill: '#6c7086', fontSize: 12 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: '#6c7086', fontSize: 12 }} axisLine={false} tickLine={false}
                     unit=" min" width={45} />
              <Tooltip
                contentStyle={{ background: '#1e1e2e', border: '1px solid #313244', borderRadius: 8 }}
                labelStyle={{ color: '#cdd6f4' }}
                itemStyle={{ color: '#a5b4fc' }}
                formatter={(v) => [`${v} min`, 'Study time']}
              />
              <Bar dataKey="minutes" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Sessions by type */}
      {weekly?.byType && Object.keys(weekly.byType).length > 0 && (
        <div className="card space-y-4">
          <h2 className="text-base font-semibold text-[#cdd6f4]">Sessions by Type</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {Object.entries(weekly.byType).map(([type, count]) => (
              <div key={type} className="bg-[#1e1e2e] rounded-xl p-4 text-center">
                <p className="text-2xl font-bold text-indigo-400">{count}</p>
                <p className="text-xs text-[#6c7086] mt-1">{type}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
