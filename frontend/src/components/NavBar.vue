<template>
  <el-header class="navbar">
    <div class="logo" @click="$router.push('/home')">PawMatch</div>
    <el-menu mode="horizontal" :default-active="activeMenu" router class="nav-menu">
      <el-menu-item index="/home">首页</el-menu-item>
      <template v-if="isShelter">
        <el-menu-item index="/shelter/pets">宠物管理</el-menu-item>
        <el-menu-item index="/shelter/adoptions">领养审核</el-menu-item>
        <el-menu-item index="/shelter/foster">寄养管理</el-menu-item>
        <el-menu-item index="/shelter/posts">帖子审核</el-menu-item>
        <el-menu-item index="/shelter/reports">举报管理</el-menu-item>
        <el-menu-item index="/shelter/wiki-audit">百科审核</el-menu-item>
      </template>
      <template v-else>
        <el-menu-item index="/pets">宠物列表</el-menu-item>
        <el-menu-item index="/foster">寄养服务</el-menu-item>
        <el-menu-item index="/game">领养乐园</el-menu-item>
      </template>
      <el-menu-item index="/community">社区</el-menu-item>
      <el-menu-item index="/wiki">百科</el-menu-item>
    </el-menu>
    <div class="user-area">
      <el-badge :value="unreadNotif" :hidden="!unreadNotif" :max="99" class="nav-badge">
        <span class="nav-icon bell" :class="{ 'has-unread': unreadNotif }" @click="$router.push('/notifications')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
        </span>
      </el-badge>
      <div class="nav-icon message" @click.stop="console.log('clicked'); $router.push('/messages')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
      </div>
      <div v-if="!isShelter" class="nav-icon star" @click="$router.push('/favorites')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
        </svg>
      </div>
      <el-dropdown trigger="click" @command="handleCommand">
        <el-avatar :size="34" class="avatar">
          {{ nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="followups" v-if="!isShelter">领养回访</el-dropdown-item>
            <el-dropdown-item command="followups" v-if="isShelter">回访记录</el-dropdown-item>
            <el-dropdown-item command="growth">成长激励</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { notificationAPI, messageAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isShelter, nickname, userId, userType } = storeToRefs(userStore)
const unreadNotif = ref(0)
let pollTimer = null
const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/shelter')) return p
  if (p.startsWith('/community')) return '/community'
  if (p.startsWith('/wiki')) return '/wiki'
  if (p.startsWith('/pet/')) return '/pets'
  return p
})

onMounted(() => {
  fetchUnread()
  pollTimer = setInterval(fetchUnread, 30000)
})
onBeforeUnmount(() => { clearInterval(pollTimer) })

async function fetchUnread() {
  try {
    if (!userId.value) return
    const [notifRes, msgRes] = await Promise.allSettled([
      notificationAPI.unreadCount(userId.value, userType.value),
      messageAPI.unreadCount(userId.value, userType.value)
    ])
    let count = 0
    if (notifRes.status === 'fulfilled' && notifRes.value.data) count += notifRes.value.data.count || 0
    if (msgRes.status === 'fulfilled' && msgRes.value.data) count += msgRes.value.data.count || 0
    unreadNotif.value = count
  } catch {
    ElMessage.error('加载失败，请稍后重试')
  }
}

function handleCommand(cmd) {
  if (cmd === 'profile') router.push('/my')
  else if (cmd === 'growth') router.push('/profile')
  else if (cmd === 'logout') { userStore.logout(); router.push('/login') }
  else if (cmd === 'messages') router.push('/messages')
  else if (cmd === 'followups') router.push('/followups')
}

</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 1px 12px rgba(230, 126, 34, 0.08);
  padding: 0 40px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.logo {
  font-size: 22px;
  font-weight: 800;
  color: #e67e22;
  margin-right: 40px;
  cursor: pointer;
  user-select: none;
  letter-spacing: -0.5px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo::before {
  content: '🐾';
  font-size: 20px;
}
.nav-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;
}
.user-area {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-badge { margin-right: 2px; }
.nav-icon {
  width: 38px; height: 38px;
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  color: #b8a590;
  transition: all .25s cubic-bezier(.4,0,.2,1);
  position: relative;
}
.nav-icon svg { width: 20px; height: 20px; }
.nav-icon:hover {
  color: #e67e22;
  background: rgba(230,126,34,.08);
  transform: scale(1.1);
}
.nav-icon.bell.has-unread {
  color: #e67e22;
  animation: bell-ring .8s ease-in-out;
}
@keyframes bell-ring {
  0%, 100% { transform: rotate(0); }
  10%, 30% { transform: rotate(12deg); }
  20%, 40% { transform: rotate(-12deg); }
  50% { transform: rotate(6deg); }
  60% { transform: rotate(-6deg); }
  70% { transform: rotate(0); }
}
.nav-icon.message:hover { color: #2e86de; background: rgba(46,134,222,.08); }
.nav-icon.star:hover { color: #f39c12; background: rgba(243,156,18,.08); }
.avatar {
  background: linear-gradient(135deg, #e67e22, #f0a04b);
  color: #fff;
  cursor: pointer;
  font-weight: 700;
  margin-left: 4px;
  transition: transform .25s ease, box-shadow .25s ease;
}
.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(230, 126, 34, 0.3);
}
</style>
