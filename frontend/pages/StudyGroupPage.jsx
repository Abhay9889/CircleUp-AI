import { useEffect, useState } from 'react'
import api from '../api/axiosClient'
import { Users, Plus, LogIn, Share2, Loader2, Copy, Check } from 'lucide-react'
import toast from 'react-hot-toast'
import clsx from 'clsx'

export default function StudyGroupPage() {
  const [groups, setGroups]       = useState([])
  const [loading, setLoading]     = useState(true)
  const [newName, setNewName]     = useState('')
  const [inviteCode, setInviteCode] = useState('')
  const [copied, setCopied]       = useState(null)
  const [activeGroup, setActiveGroup] = useState(null)
  const [groupNotes, setGroupNotes]   = useState([])
  const [groupMembers, setGroupMembers] = useState([])

  const loadGroups = async () => {
    try {
      const { data } = await api.get('/groups')
      setGroups(data)
    } catch { toast.error('Failed to load groups') }
    finally { setLoading(false) }
  }

  useEffect(() => { loadGroups() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!newName.trim()) return
    try {
      const { data } = await api.post('/groups', { name: newName.trim() })
      setGroups(g => [data, ...g])
      setNewName('')
      toast.success(`Group "${data.name}" created! Code: ${data.inviteCode}`)
    } catch { toast.error('Failed to create group') }
  }

  const handleJoin = async (e) => {
    e.preventDefault()
    if (!inviteCode.trim()) return
    try {
      const { data } = await api.post('/groups/join', { inviteCode: inviteCode.trim() })
      setGroups(g => [data, ...g.filter(x => x.id !== data.id)])
      setInviteCode('')
      toast.success(`Joined "${data.name}"!`)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid invite code')
    }
  }

  const copyCode = (code, id) => {
    navigator.clipboard.writeText(code)
    setCopied(id)
    setTimeout(() => setCopied(null), 2000)
    toast.success('Invite code copied!')
  }

  const openGroup = async (group) => {
    setActiveGroup(group)
    const [notesRes, membersRes] = await Promise.all([
      api.get(`/groups/${group.id}/notes`),
      api.get(`/groups/${group.id}/members`),
    ])
    setGroupNotes(notesRes.data)
    setGroupMembers(membersRes.data)
  }

  const handleLeave = async (groupId) => {
    if (!confirm('Leave this group?')) return
    try {
      await api.delete(`/groups/${groupId}/leave`)
      setGroups(g => g.filter(x => x.id !== groupId))
      if (activeGroup?.id === groupId) setActiveGroup(null)
      toast.success('Left group')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not leave group')
    }
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
        <Users size={24} className="text-indigo-400" /> Study Groups
      </h1>

      {/* Create + Join */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="card space-y-3">
          <h2 className="text-sm font-semibold text-[#a6adc8] flex items-center gap-2">
            <Plus size={15} /> Create Group
          </h2>
          <form onSubmit={handleCreate} className="flex gap-2">
            <input className="input flex-1" placeholder="Group name..."
              value={newName} onChange={e => setNewName(e.target.value)} />
            <button type="submit" className="btn-primary">Create</button>
          </form>
        </div>
        <div className="card space-y-3">
          <h2 className="text-sm font-semibold text-[#a6adc8] flex items-center gap-2">
            <LogIn size={15} /> Join via Code
          </h2>
          <form onSubmit={handleJoin} className="flex gap-2">
            <input className="input flex-1 font-mono uppercase" placeholder="INVITE CODE..."
              value={inviteCode} onChange={e => setInviteCode(e.target.value.toUpperCase())} />
            <button type="submit" className="btn-primary">Join</button>
          </form>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Groups list */}
        <div className="space-y-3">
          <h2 className="text-sm font-semibold text-[#6c7086] uppercase tracking-widest">My Groups</h2>
          {loading ? (
            <div className="flex items-center gap-2 text-[#6c7086]">
              <Loader2 size={16} className="animate-spin" /> Loading...
            </div>
          ) : groups.length === 0 ? (
            <div className="card text-center py-12 text-[#6c7086]">
              <Users size={40} className="mx-auto mb-2 opacity-30" />
              <p className="text-sm">No groups yet. Create or join one above.</p>
            </div>
          ) : groups.map(g => (
            <div
              key={g.id}
              onClick={() => openGroup(g)}
              className={clsx(
                'card cursor-pointer transition-all hover:border-indigo-500/50',
                activeGroup?.id === g.id && 'border-indigo-500'
              )}
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-semibold text-[#cdd6f4]">{g.name}</p>
                  <p className="text-xs text-[#6c7086] mt-1">
                    {g.memberCount} members · {g.noteCount} notes
                    {g.isOwner && <span className="ml-2 badge bg-indigo-500/20 text-indigo-400">Owner</span>}
                  </p>
                </div>
                <div className="flex items-center gap-1 shrink-0">
                  <button
                    onClick={(e) => { e.stopPropagation(); copyCode(g.inviteCode, g.id) }}
                    className="flex items-center gap-1 text-xs px-2 py-1 rounded-lg
                               bg-[#1e1e2e] text-[#6c7086] hover:text-indigo-400 transition-colors font-mono"
                  >
                    {copied === g.id ? <Check size={12} /> : <Copy size={12} />}
                    {g.inviteCode}
                  </button>
                  {!g.isOwner && (
                    <button
                      onClick={(e) => { e.stopPropagation(); handleLeave(g.id) }}
                      className="text-xs px-2 py-1 rounded-lg text-[#6c7086] hover:text-red-400 transition-colors"
                    >
                      Leave
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Group detail */}
        {activeGroup && (
          <div className="space-y-3">
            <h2 className="text-sm font-semibold text-[#6c7086] uppercase tracking-widest">
              {activeGroup.name}
            </h2>

            <div className="card space-y-2">
              <p className="text-xs text-[#6c7086] uppercase tracking-widest mb-2">Members</p>
              {groupMembers.map(m => (
                <div key={m.id} className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-full bg-indigo-500/20 flex items-center justify-center text-xs text-indigo-400 font-semibold">
                    {m.name[0]}
                  </div>
                  <div>
                    <p className="text-sm text-[#cdd6f4]">{m.name}</p>
                    <p className="text-xs text-[#6c7086]">{m.role}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="card space-y-2">
              <p className="text-xs text-[#6c7086] uppercase tracking-widest mb-2">
                Shared Notes ({groupNotes.length})
              </p>
              {groupNotes.length === 0 ? (
                <p className="text-sm text-[#6c7086]">No shared notes yet</p>
              ) : groupNotes.map(n => (
                <div key={n.id} className="flex items-center gap-2 text-sm text-[#cdd6f4]">
                  <Share2 size={13} className="text-indigo-400 shrink-0" />
                  <span className="truncate">{n.title}</span>
                  <span className="text-xs text-[#6c7086] ml-auto uppercase font-mono">{n.type}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}