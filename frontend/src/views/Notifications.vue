<template>
  <div class="page-content">
    <div class="notification-page">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>通知中心</span>
            <el-button type="text" @click="markAllRead" :disabled="notifications.length === 0">全部已读</el-button>
          </div>
        </template>
        <div v-if="notifications.length === 0" class="empty">暂无通知</div>
        <div v-else class="notification-list">
          <div
            v-for="item in notifications"
            :key="item.id"
            :class="['notification-item', { unread: !item.isRead }]"
            @click="handleClick(item)"
          >
            <div class="item-header">
              <span class="item-type">{{ typeLabel(item.type) }}</span>
              <span class="item-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <div class="item-title">{{ item.title }}</div>
            <div class="item-content">{{ item.content }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { notificationAPI } from '@/api'

const router = useRouter()

const notifications = ref([])

fetchList()

async function fetchList() {
  try {
    const user = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    const res = await notificationAPI.list(user.userId, user.userType)
    notifications.value = res.data || []
  } catch { /* ignore */ }
}

function typeLabel(type) {
  const map = { ADOPTION: '领养', FOSTER: '寄养', COMMUNITY: '社区', SYSTEM: '系统' }
  return map[type] || type
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function handleClick(item) {
  if (!item.isRead) {
    await notificationAPI.markRead(item.id)
    item.isRead = true
  }
  if (item.relatedId) {
    if (item.type === 'COMMUNITY') {
      router.push(`/community/post/${item.relatedId}`)
    } else if (item.type === 'ADOPTION') {
      router.push(`/pet/${item.relatedId}`)
    }
  }
}

async function markAllRead() {
  const user = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  await notificationAPI.markAllRead(user.userId, user.userType)
  notifications.value.forEach(n => (n.isRead = true))
}
</script>

<style scoped>
.notification-page { max-width: 800px; margin: 0 auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.empty { text-align: center; color: #999; padding: 40px; }
.notification-item { padding: 12px 0; border-bottom: 1px solid #eee; cursor: pointer; }
.notification-item.unread { background: #f0f9ff; margin: 0 -20px; padding: 12px 20px; }
.notification-item:last-child { border-bottom: none; }
.item-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.item-type { color: #409eff; font-size: 12px; }
.item-time { color: #999; font-size: 12px; }
.item-title { font-weight: bold; margin-bottom: 4px; }
.item-content { color: #666; font-size: 13px; }
</style>
