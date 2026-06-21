<template>
  <div class="page-content">
<div>
        <div class="container">
          <div class="topbar">
            <h2>社区动态</h2>
            <el-button v-if="!isShelter" type="primary" @click="showPostDialog = true">发布帖子</el-button>
          </div>
          <div v-loading="loading">
            <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />
            <el-card v-for="post in posts" :key="post.id" class="post-card">
            <div class="post-header">
              <router-link v-if="post.userType === 1" :to="'/shelter/' + post.userId" class="post-author">
                {{ post.userName || '匿名' }}
              </router-link>
              <span v-else class="post-author">{{ post.userName || '匿名' }}</span>
              <el-button v-if="post.userId !== myId && !friendIds.has(post.userId)" size="small" text type="primary" @click="addFriend(post)">加好友</el-button>
            </div>
            <div @click="$router.push(`/community/post/${post.id}`)">
              <h3>{{ post.title }}</h3>
              <p class="post-content">{{ post.content?.slice(0, 200) }}...</p>
              <div class="post-meta">
                <span>{{ post.createTime }}</span>
                <span>浏览 {{ post.viewCount }}</span>
                <span>评论 {{ post.commentCount }}</span>
              </div>
            </div>
            <div class="post-actions">
              <el-button
                size="small"
                :type="post.hasLiked ? 'danger' : 'default'"
                @click.stop="toggleLike(post)"
                :loading="likingMap[post.id]"
              >
                {{ post.hasLiked ? '已赞' : '点赞' }} {{ post.likeCount }}
              </el-button>
              <el-button v-if="!isShelter" size="small" type="warning" plain @click.stop="showReport(post)">举报</el-button>
              <el-button v-if="isShelter" size="small" type="danger" plain @click.stop="takeDown(post)">下架</el-button>
            </div>
          </el-card>
          </div>

          <div class="pagination-wrap" v-if="total > pageSize">
            <el-pagination
              :total="total"
              :page-size="pageSize"
              v-model:current-page="pageNum"
              layout="prev, pager, next"
              @current-change="loadPosts"
              background
            />
          </div>

          <el-dialog v-model="showPostDialog" title="发布帖子" width="500px" top="2vh" class="community-post-dialog">
            <el-form :model="newPost">
              <el-form-item label="标题"><el-input v-model="newPost.title" /></el-form-item>
              <el-form-item label="分类">
                <el-select v-model="newPost.category" style="width:100%" popper-class="community-post-popper">
                  <el-option label="领养故事" value="领养故事" />
                  <el-option label="经验分享" value="经验分享" />
                  <el-option label="寻宠启示" value="寻宠启示" />
                </el-select>
              </el-form-item>
              <el-form-item label="内容"><el-input v-model="newPost.content" type="textarea" :rows="5" /></el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="showPostDialog = false">取消</el-button>
              <el-button type="primary" @click="publishPost" :loading="publishing">发布</el-button>
            </template>
          </el-dialog>

          <el-dialog v-model="showReportDialog" title="举报帖子" width="400px">
            <el-form :model="reportForm">
              <el-form-item label="举报原因">
                <el-select v-model="reportForm.reason" style="width:100%" placeholder="选择举报原因" popper-class="community-post-popper">
                  <el-option label="垃圾广告" value="垃圾广告" />
                  <el-option label="色情低俗" value="色情低俗" />
                  <el-option label="虚假信息" value="虚假信息" />
                  <el-option label="人身攻击" value="人身攻击" />
                  <el-option label="侵权内容" value="侵权内容" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="showReportDialog = false">取消</el-button>
              <el-button type="primary" @click="submitReport" :loading="reporting">提交</el-button>
            </template>
          </el-dialog>

          <el-dialog v-model="showTakeDownDialog" title="提示" width="400px">
            <p>确定要下架该帖子吗？</p>
            <template #footer>
              <el-button @click="showTakeDownDialog = false">取消</el-button>
              <el-button type="danger" @click="confirmTakeDown" :loading="takingDown">确定</el-button>
            </template>
          </el-dialog>
        </div>
      </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { communityAPI, reportAPI, friendAPI } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const { isShelter, userId: myId, userType: myType } = storeToRefs(userStore)
const posts = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = 20
const total = ref(0)
const friendIds = ref(new Set())
const showPostDialog = ref(false)
const publishing = ref(false)
const likingMap = ref({})
const newPost = ref({ title: '', category: '领养故事', content: '' })
const showReportDialog = ref(false)
const reporting = ref(false)
const reportForm = ref({ reason: '', postId: null })
const showTakeDownDialog = ref(false)
const takeDownTarget = ref(null)
const takingDown = ref(false)

onMounted(loadPosts)

async function loadPosts() {
  loading.value = true
  try {
    const res = await communityAPI.postList({ pageNum: pageNum.value, pageSize })
    posts.value = res?.data?.records || []
    total.value = res?.data?.total || 0
    const friendRes = await friendAPI.list(myId.value, myType.value)
    if (friendRes?.data) {
      friendIds.value = new Set(friendRes.data.map(f => f.friendId))
    }
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function publishPost() {
  publishing.value = true
  try {
    await communityAPI.createPost(newPost.value)
    showPostDialog.value = false
    newPost.value = { title: '', category: '领养故事', content: '' }
    if (isShelter.value) {
      ElMessage.success('发布成功')
    } else {
      ElMessage.success('发布成功，等待审核')
    }
    loadPosts()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    publishing.value = false
  }
}

async function toggleLike(post) {
  likingMap.value[post.id] = true
  try {
    await communityAPI.likePost(post.id)
    post.hasLiked = !post.hasLiked
    post.likeCount += post.hasLiked ? 1 : -1
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    likingMap.value[post.id] = false
  }
}

function showReport(post) {
  reportForm.value = { reason: '', postId: post.id }
  showReportDialog.value = true
}

async function submitReport() {
  reporting.value = true
  try {
    await reportAPI.create({ reporterId: userStore.userId, targetType: 'post', targetId: reportForm.value.postId, reason: reportForm.value.reason })
    ElMessage.success('举报已提交')
    showReportDialog.value = false
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    reporting.value = false
  }
}

async function addFriend(post) {
  try {
    await friendAPI.request({ userId: myId.value, userType: myType.value, friendId: post.userId, friendUserType: post.userType || 0 })
    friendIds.value.add(post.userId)
    ElMessage.success('好友请求已发送')
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  }
}

function takeDown(post) {
  takeDownTarget.value = post
  showTakeDownDialog.value = true
}

async function confirmTakeDown() {
  takingDown.value = true
  try {
    await communityAPI.takeDownPost(takeDownTarget.value.id)
    ElMessage.success('帖子已下架')
    showTakeDownDialog.value = false
    takeDownTarget.value = null
    loadPosts()
  } catch (e) {
    ElMessage.error(e.message || '下架失败')
  } finally {
    takingDown.value = false
  }
}

function logout() { userStore.logout(); router.push('/login') }
</script>

<style scoped>
.page-content { padding-top: 60px; }
.topbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }
.post-card { margin-bottom:16px; }
.post-card > div { cursor:pointer; }
.post-content { color:#888; margin:8px 0; }
.post-meta { display:flex; gap:20px; color:#aaa; font-size:13px; }
.post-actions { margin-top:8px; display:flex; gap:8px; }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.post-author { font-weight: 500; color: #409eff; font-size: 14px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
<style>
.community-post-dialog .el-dialog__body { max-height:calc(90vh - 120px); overflow-y:auto; }
.community-post-popper { z-index: 9999 !important; }
</style>
