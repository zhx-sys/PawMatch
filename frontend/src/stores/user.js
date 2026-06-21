import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(JSON.parse(sessionStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!sessionStorage.getItem('token'))
  const isShelter = computed(() => userInfo.value?.userType === 1)
  const userId = computed(() => userInfo.value?.userId)
  const nickname = computed(() => userInfo.value?.nickname)
  const userType = computed(() => userInfo.value?.userType)

  function setUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function logout() {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
    userInfo.value = {}
  }

  return {
    userInfo,
    isLoggedIn,
    isShelter,
    userId,
    nickname,
    userType,
    setUserInfo,
    logout
  }
})
