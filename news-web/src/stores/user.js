import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref('')
  const refreshToken = ref('')
  const userId = ref(null)
  const username = ref('')
  const role = ref('')
  const roleName = ref('')
  const avatar = ref('')

  // Getters
  const isLoggedIn = computed(() => !!accessToken.value)
  const isAdmin = computed(() => role.value === 'admin')
  const isAuditor = computed(() => role.value === 'auditor')
  const isEditor = computed(() => role.value === 'editor')
  const isUser = computed(() => role.value === 'user')

  // Actions
  async function login(credentials) {
    const data = await request.post('/auth/login', credentials)
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    userId.value = data.userId
    username.value = data.username
    role.value = data.role
    roleName.value = data.roleName
    return data
  }

  async function register(data) {
    return await request.post('/auth/register', data)
  }

  async function fetchUserInfo() {
    const data = await request.get('/auth/me')
    userId.value = data.userId
    username.value = data.username
    role.value = data.role
    roleName.value = data.roleName
    avatar.value = data.avatar
  }

  function logout() {
    if (refreshToken.value) {
      request.post('/auth/logout', { refreshToken: refreshToken.value }).catch(() => {})
    }
    accessToken.value = ''
    refreshToken.value = ''
    userId.value = null
    username.value = ''
    role.value = ''
    roleName.value = ''
    avatar.value = ''
  }

  return {
    accessToken, refreshToken, userId, username, role, roleName, avatar,
    isLoggedIn, isAdmin, isAuditor, isEditor, isUser,
    login, register, fetchUserInfo, logout
  }
}, {
  persist: {
    key: 'news-user',
    pick: ['accessToken', 'refreshToken', 'userId', 'username', 'role', 'roleName', 'avatar']
  }
})
