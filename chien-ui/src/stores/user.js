import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, getUserInfoApi, logoutApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userId = ref(null)
  const username = ref('')
  const permissions = ref([])
  const menus = ref([])

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    token.value = res.data.token
    userId.value = res.data.userId
    username.value = res.data.username
    setToken(res.data.token)
    return res
  }

  async function getInfo() {
    const res = await getUserInfoApi()
    userId.value = res.data.userId
    username.value = res.data.username
    permissions.value = res.data.permissions
    menus.value = res.data.menus
    return res
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // ignore
    }
    token.value = ''
    userId.value = null
    username.value = ''
    permissions.value = []
    menus.value = []
    removeToken()
  }

  function resetToken() {
    token.value = ''
    userId.value = null
    username.value = ''
    permissions.value = []
    menus.value = []
    removeToken()
  }

  return {
    token, userId, username, permissions, menus,
    login, getInfo, logout, resetToken
  }
})
