import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout         from './components/layout/Layout'
import LoginPage      from './pages/LoginPage'
import RegisterPage   from './pages/RegisterPage'
import DashboardPage  from './pages/DashboardPage'
import NotesPage      from './pages/NotesPage'
import AskPage        from './pages/AskPage'
import FlashcardsPage from './pages/FlashcardsPage'
import QuizPage       from './pages/QuizPage'
import MindMapPage    from './pages/MindMapPage'
import AnalyticsPage  from './pages/AnalyticsPage'
import VoicePage      from './pages/VoicePage'
import StudyGroupPage from './pages/StudyGroupPage'
import SearchPage     from './pages/SearchPage'
import ProfilePage    from './pages/ProfilePage'

function Protected({ children }) {
  const token = useAuthStore((s) => s.token)
  return token ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route path="/" element={<Protected><Layout /></Protected>}>
        <Route index                element={<DashboardPage />} />
        <Route path="notes"         element={<NotesPage />} />
        <Route path="ask"           element={<AskPage />} />
        <Route path="flashcards"    element={<FlashcardsPage />} />
        <Route path="quiz"          element={<QuizPage />} />
        <Route path="mindmap"       element={<MindMapPage />} />
        <Route path="analytics"     element={<AnalyticsPage />} />
        <Route path="voice"         element={<VoicePage />} />
        <Route path="groups"        element={<StudyGroupPage />} />
        <Route path="search"        element={<SearchPage />} />
        <Route path="profile"       element={<ProfilePage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}