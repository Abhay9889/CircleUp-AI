import api from './axiosClient'

// ── Auth ──────────────────────────────────────────────────
export const authApi = {
  register: (data)        => api.post('/auth/register', data),
  login:    (data)        => api.post('/auth/login', data),
  logout:   ()            => api.post('/auth/logout'),
  me:       ()            => api.get('/auth/me'),
  refresh:  (refreshToken)=> api.post('/auth/refresh', { refreshToken }),
}

// ── Notes ─────────────────────────────────────────────────
export const notesApi = {
  upload: (formData) =>
    api.post('/notes', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  list:        ()       => api.get('/notes'),
  get:         (id)     => api.get(`/notes/${id}`),
  delete:      (id)     => api.delete(`/notes/${id}`),
  downloadUrl: (id)     => api.get(`/notes/${id}/download-url`),
}

// ── Flashcards ────────────────────────────────────────────
export const flashcardsApi = {
  getAll:    ()              => api.get('/flashcards'),
  getDue:    ()              => api.get('/flashcards/due'),
  getDueCount: ()            => api.get('/flashcards/due-count'),
  getByNote: (noteId)        => api.get(`/flashcards/note/${noteId}`),
  generate:  (noteId, count) => api.post(`/flashcards/generate/${noteId}?count=${count}`),
  review:    (id, quality)   => api.post(`/flashcards/${id}/review`, { quality }),
  delete:    (id)            => api.delete(`/flashcards/${id}`),
}

// ── Quiz ──────────────────────────────────────────────────
export const quizApi = {
  generate: (noteId, data)   => api.post(`/quizzes/generate/${noteId}`, data),
  list:     ()               => api.get('/quizzes'),
  get:      (id)             => api.get(`/quizzes/${id}`),
  submit:   (id, answers)    => api.post(`/quizzes/${id}/submit`, { answers }),
  delete:   (id)             => api.delete(`/quizzes/${id}`),
}

// ── AI / RAG ──────────────────────────────────────────────
export const aiApi = {
  ask:          (data)   => api.post('/ai/ask', data),
  askWithSearch:(data)   => api.post('/ai/ask-with-search', data),
  summarize:    (noteId) => api.post(`/ai/summarize/${noteId}`),
  difficulty:   (noteId) => api.get(`/ai/difficulty/${noteId}`),
  mindmap:      (noteId) => api.post(`/ai/mindmap/${noteId}`),
  translate:    (data)   => api.post('/ai/translate', data),
}

// ── Analytics ─────────────────────────────────────────────
export const analyticsApi = {
  summary:      ()             => api.get('/analytics/summary'),
  heatmap:      ()             => api.get('/analytics/heatmap'),
  weekly:       ()             => api.get('/analytics/weekly'),
  startSession: (data)         => api.post('/analytics/session/start', data),
  endSession:   (id, duration) => api.post(`/analytics/session/${id}/end`, { durationSec: duration }),
}

// ── Notifications ─────────────────────────────────────────
export const notifApi = {
  getAll:       () => api.get('/notifications'),
  getUnread:    () => api.get('/notifications/unread'),
  unreadCount:  () => api.get('/notifications/unread-count'),
  markAllRead:  () => api.post('/notifications/mark-all-read'),
}
