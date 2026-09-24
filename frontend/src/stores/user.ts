import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getMe, login as loginRequest, register as registerRequest } from '@/api/study'
import type { AuthResponse, LoginPayload, RegisterPayload, UserProfile } from '@/types/study'

function storedUser() {
  const value = localStorage.getItem('study_compass_user')
  return value ? JSON.parse(value) as UserProfile : null
}

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(storedUser())
  const loading = ref(false)
  const authenticated = computed(() => Boolean(localStorage.getItem('study_compass_token') && profile.value))
  const displayName = computed(() => profile.value?.nickname || '学习者')
  const avatarText = computed(() => displayName.value.slice(0, 1))

  function acceptAuth(result: AuthResponse) {
    localStorage.setItem('study_compass_token', result.token)
    setProfile(result.user)
  }

  async function login(payload: LoginPayload) { acceptAuth(await loginRequest(payload)) }
  async function register(payload: RegisterPayload) { acceptAuth(await registerRequest(payload)) }

  async function load() {
    if (loading.value || !localStorage.getItem('study_compass_token')) return
    loading.value = true
    try { setProfile(await getMe()) } finally { loading.value = false }
  }

  function setProfile(value: UserProfile) {
    profile.value = value
    localStorage.setItem('study_compass_user', JSON.stringify(value))
  }

  function logout() {
    localStorage.removeItem('study_compass_token')
    localStorage.removeItem('study_compass_user')
    profile.value = null
  }

  return { profile, loading, authenticated, displayName, avatarText, load, login, register, logout, setProfile }
})
