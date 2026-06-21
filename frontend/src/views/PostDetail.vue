<template>
  <div class="detail-page">
    <div class="topbar">
      <el-button text @click="$router.back()">← 返回</el-button>
    </div>
    <div v-if="post" class="detail-body">
      <div class="post-header">
        <h2>{{ post.title }}</h2>
        <div class="post-meta">
          <span>{{ post.userName || '匿名用户' }}</span>
          <span>{{ post.createTime }}</span>
          <span>浏览 {{ post.viewCount }}</span>
          <span>评论 {{ post.commentCount }}</span>
        </div>
      </div>
      <div class="post-content">{{ post.content }}</div>
      <div class="post-actions">
        <el-button
          :type="post.hasLiked ? 'danger' : 'default'"
          :icon="post.hasLiked ? 'StarFilled' : 'Star'"
          @click="toggleLike"
          :loading="liking"
        >
          {{ post.hasLiked ? '已赞' : '点赞' }} {{ post.likeCount }}
        </el-button>
      </div>
      <el-divider />
      <h4>评论 ({{ post.commentCount }})</h4>
      <div class="comment-list">
        <div v-if="post.comments?.length">
          <div v-for="c in post.comments" :key="c.id" class="comment-item">
            <strong>{{ c.userName || '匿名用户' }}</strong>
            <span class="comment-time">{{ c.createTime }}</span>
            <p class="comment-text">{{ c.content }}</p>
            <div v-if="c.replies?.length" class="reply-list">
              <div v-for="r in c.replies" :key="r.id" class="reply-item">
                <strong>{{ r.userName || '匿名用户' }}</strong>
                <span class="comment-time">{{ r.createTime }}</span>
                <p class="comment-text">{{ r.content }}</p>
              </div>
            </div>
          </div>
        </div>
        <p v-else style="color:#999">暂无评论，快来抢沙发</p>
      </div>
      <div class="comment-input">
        <el-input v-model="commentText" placeholder="写评论..." :rows="2" type="textarea" />
        <el-button type="primary" @click="submitComment" :loading="sending" style="margin-top:8px">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { communityAPI } from '../api'

const route = useRoute()
const post = ref(null)
const commentText = ref('')
const sending = ref(false)
const liking = ref(false)

onMounted(loadPost)

async function loadPost() {
  try {
    const res = await communityAPI.postDetail(route.params.id)
    post.value = res?.data
  } catch (e) {
    ElMessage.error('帖子不存在或已被删除')
  }
}

async function toggleLike() {
  liking.value = true
  try {
    await communityAPI.likePost(post.value.id)
    loadPost()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    liking.value = false
  }
}

async function submitComment() {
  if (!commentText.value.trim()) return
  sending.value = true
  try {
    await communityAPI.createComment({ postId: post.value.id, content: commentText.value })
    ElMessage.success('评论成功')
    commentText.value = ''
    loadPost()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.detail-page { max-width:800px; margin:0 auto; padding:20px; }
.topbar { margin-bottom:20px; }
.detail-body { background:#fff; padding:24px; border-radius:8px; }
.post-header { margin-bottom:16px; }
.post-header h2 { margin:0 0 8px; }
.post-meta { display:flex; gap:20px; color:#999; font-size:13px; }
.post-content { line-height:1.8; color:#333; margin:16px 0; white-space:pre-wrap; }
.post-actions { margin:16px 0; }
.comment-list { margin:16px 0; }
.comment-item { padding:12px 0; border-bottom:1px solid #f0f0f0; }
.comment-time { color:#bbb; font-size:12px; margin-left:8px; }
.comment-text { margin:4px 0 0; color:#555; }
.reply-list { margin-left:24px; padding-left:12px; border-left:2px solid #eee; }
.reply-item { padding:8px 0; }
.comment-input { margin-top:16px; }
</style>
