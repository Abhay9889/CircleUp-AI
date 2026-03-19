import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import {
  LayoutDashboard, FileText, MessageSquare, BookOpen,
  ClipboardList, GitBranch, BarChart2, Mic, Users,
  Search, Settings, LogOut, ChevronLeft, ChevronRight, Bell
} from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import { authApi } from '../../api'
import toast from 'react-hot-toast'

const NAV_ITEMS = [
  { to: '/',           icon: LayoutDashboard, label: 'Dashboard'   },
  { to: '/notes',      icon: FileText,        label: 'Notes'       },
  { to: '/ask',        icon: MessageSquare,   label: 'Ask AI'      },
  { to: '/flashcards', icon: BookOpen,        label: 'Flashcards'  },
  { to: '/quiz',       icon: ClipboardList,   label: 'Quiz'        },
  { to: '/mindmap',    icon: GitBranch,       label: 'Mind Map'    },
  { to: '/analytics',  icon: BarChart2,       label: 'Analytics'   },
  { to: '/voice',      icon: Mic,             label: 'Voice'       },
  { to: '/groups',     icon: Users,           label: 'Groups'      },
  { to: '/search',     icon: Search,          label: 'Web Search'  },
]

export default function Layout() {
  const [collapsed, setCollapsed] = useState(false)
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try { await authApi.logout() } catch {}
    logout()
    navigate('/login')
    toast.success('Logged out')
  }

  return (
    <div className="flex h-screen overflow-hidden bg-[#0f0f17]">

      {/* ── Sidebar ── */}
      <aside className={`flex flex-col border-r border-[#313244] bg-[#16161f] transition-all duration-300 ${collapsed ? 'w-16' : 'w-56'}`}>

        {/* Logo */}
        <div className="flex h-16 items-center justify-between px-4 border-b border-[#313244]">
          {!collapsed && (
            <span className="text-lg font-bold text-indigo-400 tracking-tight">EduCircle</span>
          )}
          <button
            onClick={() => setCollapsed(!collapsed)}
            className="rounded-lg p-1.5 text-[#6c7086] hover:text-[#cdd6f4] hover:bg-[#1e1e2e] transition-colors ml-auto"
          >
            {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto py-4 space-y-0.5 px-2">
          {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all
                 ${isActive
                   ? 'bg-indigo-500/20 text-indigo-400'
                   : 'text-[#a6adc8] hover:bg-[#1e1e2e] hover:text-[#cdd6f4]'}`
              }
            >
              <Icon size={18} className="shrink-0" />
              {!collapsed && <span>{label}</span>}
            </NavLink>
          ))}
        </nav>

        {/* Bottom */}
        <div className="border-t border-[#313244] p-3 space-y-0.5">
          <NavLink
            to="/profile"
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all
               ${isActive ? 'bg-indigo-500/20 text-indigo-400' : 'text-[#a6adc8] hover:bg-[#1e1e2e]'}`
            }
          >
            <Settings size={18} className="shrink-0" />
            {!collapsed && <span>Profile</span>}
          </NavLink>

          {!collapsed && user && (
            <div className="px-3 py-2">
              <p className="text-sm font-semibold text-[#cdd6f4] truncate">{user.name}</p>
              <p className="text-xs text-[#6c7086] truncate">{user.email}</p>
            </div>
          )}

          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm
                       text-[#a6adc8] hover:bg-red-500/10 hover:text-red-400 transition-all"
          >
            <LogOut size={18} className="shrink-0" />
            {!collapsed && <span>Logout</span>}
          </button>
        </div>
      </aside>

      {/* ── Content ── */}
      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  )
}