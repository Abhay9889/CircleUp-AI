import { create } from 'zustand'

// ── Notes store ───────────────────────────────────────────
export const useNoteStore = create((set) => ({
  notes:         [],
  selectedNote:  null,
  loading:       false,

  setNotes:       (notes)  => set({ notes }),
  setSelectedNote:(note)   => set({ selectedNote: note }),
  setLoading:     (v)      => set({ loading: v }),

  addNote: (note) =>
    set((s) => ({ notes: [note, ...s.notes] })),

  removeNote: (id) =>
    set((s) => ({ notes: s.notes.filter((n) => n.id !== id) })),

  updateNote: (id, data) =>
    set((s) => ({
      notes: s.notes.map((n) => (n.id === id ? { ...n, ...data } : n)),
    })),
}))

// ── Settings store ────────────────────────────────────────
export const useSettingsStore = create((set) => ({
  language:          'english',
  darkMode:          true,
  webSearchEnabled:  false,

  setLanguage:         (language)         => set({ language }),
  toggleWebSearch:     ()                 => set((s) => ({ webSearchEnabled: !s.webSearchEnabled })),
}))
