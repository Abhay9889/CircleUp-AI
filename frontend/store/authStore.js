import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export const useAuthStore = create(
  persist(
    (set, get) => ({
      token:        null,
      refreshToken: null,
      user:         null,

      setTokens: (token, refreshToken) => set({ token, refreshToken }),

      setUser: (user) => set({ user }),

      login: (token, refreshToken, user) =>
        set({ token, refreshToken, user }),

      logout: () => {
        set({ token: null, refreshToken: null, user: null })
      },

      isLoggedIn: () => !!get().token,
    }),
    {
      name: 'educircle-auth',   // key in localStorage
      partialize: (s) => ({
        token:        s.token,
        refreshToken: s.refreshToken,
        user:         s.user,
      }),
    }
  )
)
