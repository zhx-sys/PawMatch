<template>
  <div class="page-content">
    <div class="chat-page">
      <div class="chat-sidebar">
        <!-- 救助站侧：只要联系人列表 -->
        <template v-if="isShelter">
          <div class="sidebar-header">联系我的用户</div>
          <div class="contact-list">
            <div
              v-for="conv in shelterConversations"
              :key="'s_' + conv.otherUserId"
              :class="['contact-item', {
                active: currentChat?.type === 'shelter' && currentChat?.otherUserId === conv.otherUserId
              }]"
              @click="openShelterChat(conv)"
            >
              <div class="contact-avatar">{{ conv.nickname ? conv.nickname.charAt(0) : 'U' }}</div>
              <div class="contact-info">
                <div class="contact-name">
                  {{ conv.nickname || '用户' + conv.otherUserId }}
                  <span v-if="conv.unread" class="unread-dot"></span>
                </div>
                <div class="contact-preview">{{ conv.lastContent }}</div>
              </div>
              <div class="contact-time">{{ formatTime(conv.lastTime) }}</div>
            </div>
            <div v-if="shelterConversations.length === 0" class="empty">还没有用户联系你</div>
          </div>
        </template>

        <!-- 普通用户侧：好友 + 救助站 -->
        <template v-else>
          <!-- 好友区域 -->
          <div class="sidebar-header">
            <span>好友</span>
            <el-button size="small" type="primary" @click="showAddDialog = true">添加</el-button>
          </div>
          <div v-if="pendingRequests.length > 0" class="pending-section">
            <div class="section-title">新的好友请求 ({{ pendingRequests.length }})</div>
            <div v-for="req in pendingRequests" :key="req.id" class="pending-item">
              <span class="pending-name">{{ req.nickname || '用户' + req.friendId }}</span>
              <div class="pending-actions">
                <el-button size="mini" type="success" @click="acceptRequest(req)">接受</el-button>
                <el-button size="mini" type="info" @click="rejectRequest(req)">拒绝</el-button>
              </div>
            </div>
          </div>
          <div class="contact-list friend-list">
            <div
              v-for="contact in contacts"
              :key="contact.friendId"
              :class="['contact-item', {
                active: currentChat?.type === 'friend' && currentChat?.friendId === contact.friendId
              }]"
              @click="openFriendChat(contact)"
            >
              <div class="contact-avatar">{{ (contact.nickname || 'U').charAt(0) }}</div>
              <div class="contact-info">
                <div class="contact-name">{{ contact.nickname || '用户' + contact.friendId }}</div>
              </div>
              <el-button
                class="delete-friend-btn"
                type="danger"
                size="mini"
                icon="el-icon-delete"
                circle
                @click.stop="confirmDeleteFriend(contact)"
                title="删除好友"
              ></el-button>
            </div>
            <div v-if="contacts.length === 0 && pendingRequests.length === 0" class="empty tips">
              暂无好友，点击「添加」搜索并添加好友
            </div>
          </div>

          <!-- 救助站对话 -->
          <div class="sidebar-divider">联系救助站</div>
          <div class="contact-list shelter-list">
            <div
              v-for="conv in shelterConversations"
              :key="'s_' + conv.otherUserId"
              :class="['contact-item', {
                active: currentChat?.type === 'shelter' && currentChat?.otherUserId === conv.otherUserId
              }]"
              @click="openShelterChat(conv)"
            >
              <div class="contact-avatar shelter-avatar">
                {{ conv.nickname ? conv.nickname.charAt(0) : 'S' }}
              </div>
              <div class="contact-info">
                <div class="contact-name">
                  {{ conv.nickname || '救助站' + conv.otherUserId }}
                  <span v-if="conv.unread" class="unread-dot"></span>
                </div>
                <div class="contact-preview">{{ conv.lastContent }}</div>
              </div>
              <div class="contact-time">{{ formatTime(conv.lastTime) }}</div>
            </div>
            <div v-if="shelterConversations.length === 0" class="empty tips">
              去宠物详情页点击「联系救助站」开始对话</div>
          </div>
        </template>
      </div>

      <div class="chat-main">
        <template v-if="currentChat">
          <div class="chat-header">{{ currentChat.displayName }}</div>
          <div class="chat-messages" ref="msgBox">
            <div v-if="messages.length === 0" class="empty-msg">暂无消息，发送第一条消息吧</div>
            <div
              v-for="msg in messages"
              :key="msg.id"
              :class="['msg-item', { self: msg.fromUserId === myId && msg.fromUserType === myType }]"
            >
              <div class="msg-content">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input v-model="inputText" placeholder="输入消息..." @keyup.enter="sendMsg">
              <template #append>
                <el-button @click="sendMsg">发送</el-button>
              </template>
            </el-input>
          </div>
        </template>
        <div v-else class="no-chat">选择一个对话开始聊天</div>
      </div>
    </div>

    <!-- 添加好友对话框 -->
    <el-dialog v-model="showAddDialog" title="添加好友" width="400px" top="2vh" class="msg-dialog">
      <el-input v-model="searchKeyword" placeholder="输入用户昵称搜索" @keyup.enter="searchUsers" />
      <div style="margin-top:12px; max-height:240px; overflow-y:auto;">
        <div v-if="searchResults.length === 0 && searched" class="empty">未找到用户</div>
        <div v-for="user in searchResults" :key="user.id" class="search-item">
          <span>{{ user.nickname }}</span>
          <el-button size="small" type="primary" :disabled="user.added" @click="addFriend(user)">
            {{ user.added ? '已发送' : '加好友' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { messageAPI, friendAPI, shelterProfileAPI } from '@/api'
import wsManager from '@/utils/websocket'
import { useUserStore } from '@/stores/user'

console.log('[Messages] setup starting')

const route = useRoute()
const router = useRouter()

const userStore = useUserStore()
const { isShelter, userId: myId, userType: myType } = storeToRefs(userStore)

console.log('[Messages] isShelter.value =', isShelter.value, 'myId.value =', myId.value)

// 好友相关
const contacts = ref([])
const pendingRequests = ref([])
const showAddDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searched = ref(false)
// 救助站对话
const shelterConversations = ref([])
// 当前聊天
const currentChat = ref(null)
const messages = ref([])
const inputText = ref('')
const msgBox = ref(null)

// WebSocket 消息回调：处理各种类型的实时推送
function handleWsMessage(data) {
  if (!data || !data.type) return

  switch (data.type) {
    case 'NEW_MESSAGE': {
      const msg = data.data
      // 如果当前聊天窗口正与该消息相关，直接追加到消息列表
      if (currentChat.value) {
        const isRelevant =
          (currentChat.value.type === 'friend' &&
            ((msg.fromUserId === myId.value && msg.toUserId === currentChat.value.friendId) ||
             (msg.toUserId === myId.value && msg.fromUserId === currentChat.value.friendId))) ||
          (currentChat.value.type === 'shelter' &&
            ((msg.fromUserId === myId.value && msg.toUserId === currentChat.value.otherUserId) ||
             (msg.toUserId === myId.value && msg.fromUserId === currentChat.value.otherUserId)))
        if (isRelevant) {
          // 避免重复消息（去重检查）
          const exists = messages.value.some(m => m.id === msg.id)
          if (!exists) {
            messages.value.push(msg)
            nextTick(() => {
              const box = msgBox.value
              if (box) box.scrollTop = box.scrollHeight
            })
            // 标记已读
            if (msg.toUserId === myId.value && msg.toUserType === myType.value) {
              messageAPI.markRead(myId.value, myType.value, msg.fromUserId).catch(() => {})
            }
          }
        }
      }
      // 刷新对话列表以更新最新消息预览
      loadShelterConversations()
      break
    }
    case 'NEW_FRIEND_REQUEST':
      // 收到好友请求，刷新待处理列表
      loadPendingRequests()
      break
    case 'FRIEND_REQUEST_ACCEPTED':
      // 好友请求被接受，刷新好友列表
      loadFriends()
      break
    default:
      // 其他事件类型由相应页面处理
      break
  }
}

// ------- init -------
if (isShelter.value) {
  loadShelterConversations()
} else {
  loadFriends()
  loadPendingRequests()
  loadShelterConversations()
}

onMounted(() => {
  // 建立 WebSocket 连接，注册消息回调
  wsManager.connect()
  wsManager.onMessage(handleWsMessage)
})

onBeforeUnmount(() => {
  // 组件销毁时移除回调并断开 WebSocket
  wsManager.offMessage(handleWsMessage)
  // 注意：不在此处直接 disconnect()，因为其他页面可能仍在使用 WebSocket
})

// === 好友 ===
async function loadFriends() {
  try {
    const res = await friendAPI.list(myId.value, myType.value)
    contacts.value = res.data || []
  } catch { /* ignore */ }
}

async function loadPendingRequests() {
  try {
    const res = await friendAPI.pending(myId.value, myType.value)
    pendingRequests.value = res.data || []
  } catch {
    ElMessage.error('加载失败，请稍后重试')
  }
}

async function acceptRequest(req) {
  await friendAPI.accept(req.id, myId.value)
  pendingRequests.value = pendingRequests.value.filter(r => r.id !== req.id)
  loadFriends()
}

async function rejectRequest(req) {
  await friendAPI.reject(req.id, myId.value)
  pendingRequests.value = pendingRequests.value.filter(r => r.id !== req.id)
}

async function confirmDeleteFriend(contact) {
  try {
    await ElMessageBox.confirm(
      `确定要删除好友「${contact.nickname || '用户' + contact.friendId}」吗？`,
      '删除好友',
      { type: 'warning' }
    )
    await friendAPI.delete(myId.value, contact.friendId)
    contacts.value = contacts.value.filter(c => c.friendId !== contact.friendId)
    if (currentChat.value?.type === 'friend' && currentChat.value?.friendId === contact.friendId) {
      currentChat.value = null
      messages.value = []
    }
    ElMessage.success('已删除好友')
  } catch {
    // 用户取消
  }
}

async function searchUsers() {
  if (!searchKeyword.value.trim()) return
  try {
    const res = await friendAPI.search(myId.value, searchKeyword.value)
    searchResults.value = res.data || []
  } catch {
    ElMessage.error('加载失败，请稍后重试')
    searchResults.value = []
  } finally {
    searched.value = true
  }
}

async function addFriend(user) {
  await friendAPI.request({
    userId: myId.value,
    userType: myType.value,
    friendId: user.id,
    friendUserType: user.userType || 0
  })
  user.added = true
}

function openFriendChat(contact) {
  console.log('openFriendChat called', contact);
  currentChat.value = {
    type: 'friend',
    friendId: contact.friendId,
    friendUserType: contact.friendUserType,
    displayName: contact.nickname || '用户' + contact.friendId
  }
  fetchMessages()
}

// === 救助站对话 ===
async function loadShelterConversations() {
  try {
    const res = await messageAPI.conversations(myId.value, myType.value)
    let list = res.data || []
    if (!isShelter.value) {
      list = list.filter(c => c.otherUserType === 1)
    }
    shelterConversations.value = list
    // 路由参数跳转（从宠物详情页）
    const { shelterId } = route.query
    if (shelterId) {
      const existing = shelterConversations.value.find(
        c => String(c.otherUserId) === String(shelterId)
      )
      if (existing) {
        openShelterChat(existing)
      } else {
        const newConv = {
          otherUserId: Number(shelterId),
          otherUserType: 1,
          lastContent: '',
          lastTime: '',
          unread: false,
          nickname: ''
        }
        try {
          const profileRes = await shelterProfileAPI.profile(shelterId)
          newConv.nickname = profileRes?.data?.shelterInfo?.nickname || ''
        } catch {
          ElMessage.error('加载失败，请稍后重试')
        }
        shelterConversations.value.unshift(newConv)
        openShelterChat(newConv)
      }
      router.replace({ query: {} })
    }
  } catch {
    ElMessage.error('加载失败，请稍后重试')
  }
}

function openShelterChat(conv) {
  console.log('openShelterChat called', conv);
  currentChat.value = {
    type: 'shelter',
    otherUserId: conv.otherUserId,
    otherUserType: conv.otherUserType,
    displayName: conv.nickname || (conv.otherUserType === 1 ? '救助站' + conv.otherUserId : '用户' + conv.otherUserId)
  }
  fetchMessages()
  if (conv.unread) {
    messageAPI.markRead(myId.value, myType.value, conv.otherUserId).catch(() => {})
    conv.unread = false
  }
}

// === 消息 ===
async function fetchMessages() {
  if (!currentChat.value) return
  try {
    let res
    if (currentChat.value.type === 'friend') {
      res = await messageAPI.conversation(
        myId.value, myType.value,
        currentChat.value.friendId, currentChat.value.friendUserType
      )
    } else {
      const otherId = currentChat.value.otherUserId
      const otherType = currentChat.value.otherUserType
      res = await messageAPI.conversation(myId.value, myType.value, otherId, otherType)
    }
    messages.value = res.data || []
    nextTick(() => {
      const box = msgBox.value
      if (box) box.scrollTop = box.scrollHeight
    })
  } catch { /* ignore */ }
}

async function sendMsg() {
  if (!inputText.value.trim() || !currentChat.value) return
  const payload = {
    fromUserId: myId.value,
    fromUserType: myType.value,
    content: inputText.value.trim()
  }
  if (currentChat.value.type === 'friend') {
    payload.toUserId = currentChat.value.friendId
    payload.toUserType = currentChat.value.friendUserType
  } else {
    payload.toUserId = currentChat.value.otherUserId
    payload.toUserType = currentChat.value.otherUserType
  }
  try {
    await messageAPI.send(payload)
    inputText.value = ''
    fetchMessages()
    loadShelterConversations()
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return t.replace('T', ' ').substring(11, 16)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  return isToday ? d.toTimeString().substring(0, 5) : `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<style scoped>
.chat-page { display: flex; height: calc(100vh - 80px); max-width: 1000px; margin: 0 auto; }
.chat-sidebar { width: 300px; border-right: 1px solid #eee; background: #fafafa; display: flex; flex-direction: column; overflow-y: auto; }
.sidebar-header { padding: 12px 16px; font-weight: 600; font-size: 15px; border-bottom: 1px solid #eee; color: #333; display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; }
.sidebar-divider { padding: 8px 16px; font-size: 12px; color: #999; font-weight: 500; border-top: 1px solid #eee; border-bottom: 1px solid #eee; background: #f5f5f5; flex-shrink: 0; }

/* 好友请求 */
.pending-section { border-bottom: 1px solid #eee; padding: 8px 0; flex-shrink: 0; }
.section-title { font-size: 12px; color: #999; padding: 4px 16px; }
.pending-item { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.pending-name { font-size: 14px; }
.pending-actions { display: flex; gap: 4px; }

/* 联系人列表 */
.contact-list { overflow-y: auto; }
.friend-list { flex-shrink: 1; min-height: 0; }
.shelter-list { flex: 1; }

.contact-item { display: flex; align-items: center; padding: 10px 16px; cursor: pointer; transition: background .15s; }
.contact-item:hover, .contact-item.active { background: #e8f4ff; }
.contact-item .delete-friend-btn { opacity: 0; transition: opacity .15s; }
.contact-item:hover .delete-friend-btn { opacity: 1; }
.contact-avatar { width: 36px; height: 36px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px; font-weight: 600; flex-shrink: 0; }
.shelter-avatar { background: #e67e22; }
.contact-info { flex: 1; min-width: 0; }
.contact-name { font-weight: 500; font-size: 14px; display: flex; align-items: center; gap: 4px; }
.unread-dot { width: 8px; height: 8px; border-radius: 50%; background: #e67e22; flex-shrink: 0; }
.contact-preview { font-size: 12px; color: #999; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.contact-time { font-size: 11px; color: #bbb; margin-left: 6px; flex-shrink: 0; }

/* 聊天区域 */
.chat-main { flex: 1; display: flex; flex-direction: column; }
.chat-header { padding: 14px 16px; border-bottom: 1px solid #eee; font-weight: 600; font-size: 15px; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; }
.empty-msg { text-align: center; color: #ccc; padding: 60px 0; }
.msg-item { margin-bottom: 10px; max-width: 70%; }
.msg-item.self { margin-left: auto; text-align: right; }
.msg-content { display: inline-block; padding: 8px 14px; border-radius: 12px; background: #f0f0f0; font-size: 14px; word-break: break-word; }
.msg-item.self .msg-content { background: #409eff; color: #fff; }
.msg-time { font-size: 11px; color: #ccc; margin-top: 3px; }
.chat-input { padding: 12px; border-top: 1px solid #eee; }
.no-chat, .empty { text-align: center; color: #999; padding: 60px 20px; flex: 1; display: flex; align-items: center; justify-content: center; font-size: 14px; line-height: 1.6; }
.tips { padding: 30px 16px; }

/* 添加好友弹窗 */
.search-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
</style>
<style>
.msg-dialog .el-dialog__body { max-height:calc(90vh - 120px); overflow-y:auto; }
</style>
