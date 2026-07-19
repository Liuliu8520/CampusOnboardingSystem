import { defineStore } from 'pinia'
import { authApi, type LoginPayload } from '@/api/modules'

interface AuthState {
  token: string
  role: 'STUDENT' | 'ADMIN' | ''
  account: string
  displayName: string
}

const STORAGE_KEY = 'campus_onboarding_auth'

function loadState(): AuthState {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return { token: '', role: '', account: '', displayName: '' }
  }
  try {
    return JSON.parse(raw) as AuthState
  } catch {
    return { token: '', role: '', account: '', displayName: '' }
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => loadState(),
  actions: {
    async login(payload: LoginPayload) {
      const result = await authApi.login(payload)
      this.token = result.token
      this.role = result.role
      this.account = result.account
      this.displayName = result.displayName
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
      return result
    },
    logout() {
      this.token = ''
      this.role = ''
      this.account = ''
      this.displayName = ''
      localStorage.removeItem(STORAGE_KEY)
    }
  }
})
