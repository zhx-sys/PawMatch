<template>
  <div class="page-content">
    <div class="container">
      <div v-if="entry" class="detail-wrap">
        <el-page-header @back="$router.push('/wiki')" content="词条详情" style="margin-bottom:20px" />
        <h1 class="entry-title">{{ entry.title }}</h1>
        <div class="entry-info">
          <span>浏览 {{ entry.viewCount }}</span>
          <span>有帮助 {{ entry.helpfulCount }}</span>
          <span>{{ entry.createTime }}</span>
          <span v-if="categoryName">分类：{{ categoryName }}</span>
        </div>
        <div class="entry-content" v-html="renderedContent"></div>
        <div class="entry-actions">
          <el-button :type="helpful ? 'warning' : 'success'" @click="markHelpful">{{ helpful ? '取消有帮助' : '有帮助' }} ({{ entry.helpfulCount }})</el-button>
          <el-button v-if="canEdit" type="primary" @click="$router.push('/wiki/' + entry.id + '/edit')">编辑</el-button>
        </div>

        <!-- 编辑历史 -->
        <div v-if="revisions.length > 0" class="revisions">
          <h3>编辑历史</h3>
          <el-timeline>
            <el-timeline-item
              v-for="rev in revisions"
              :key="rev.id"
              :timestamp="rev.createTime"
              placement="top"
            >
              <span>{{ rev.userName }}</span>
              <span v-if="rev.summary"> &mdash; {{ rev.summary }}</span>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { wikiAPI } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const entry = ref(null)
const revisions = ref([])
const categoryName = ref('')
const helpful = ref(false)

const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
const canEdit = computed(() => {
  if (!entry.value) return false
  return userInfo.userId === entry.value.authorId || userInfo.userType === 1
})

const renderedContent = computed(() => {
  if (!entry.value?.content) return ''
  return entry.value.content.replace(/\n/g, '<br>')
})

onMounted(async () => {
  await init()
  try {
    const statusRes = await wikiAPI.helpfulStatus(route.params.id)
    helpful.value = statusRes?.data || false
  } catch (e) {}
})

async function init() {
  try {
    const res = await wikiAPI.entryDetail(route.params.id)
    entry.value = res?.data
    if (entry.value?.categoryId) {
      try {
        const catRes = await wikiAPI.categories()
        const cats = catRes?.data || []
        function findName(list, id) {
          for (const c of list) {
            if (c.id === id) return c.name
            if (c.children) {
              const found = findName(c.children, id)
              if (found) return found
            }
          }
          return null
        }
        categoryName.value = findName(cats, entry.value.categoryId) || ''
      } catch(e) {}
    }
    try {
      const revRes = await wikiAPI.revisions(route.params.id)
      revisions.value = revRes?.data || []
    } catch(e) {}
  } catch (e) {
    ElMessage.error('词条不存在')
    router.push('/wiki')
  }
}

async function markHelpful() {
  try {
    const res = await wikiAPI.markHelpful(route.params.id)
    const data = res?.data
    if (entry.value) entry.value.helpfulCount = data?.helpfulCount
    helpful.value = data?.helpful
    ElMessage.success(helpful.value ? '已标记为有帮助' : '已取消有帮助')
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>

<style scoped>
.page-content { padding-top: 60px; min-height: 100vh; background: #f7f4f0; }
.container { max-width: 900px; margin: 0 auto; padding: 20px; }
.detail-wrap { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,.04); }
.entry-title { font-size: 26px; color: #333; margin: 0 0 12px; }
.entry-info { display: flex; gap: 16px; color: #bbb; font-size: 13px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #eee; }
.entry-content { font-size: 15px; line-height: 1.8; color: #444; min-height: 200px; }
.entry-actions { margin-top: 24px; display: flex; gap: 12px; }
.revisions { margin-top: 32px; padding-top: 20px; border-top: 1px solid #eee; }
.revisions h3 { margin: 0 0 16px; font-size: 16px; color: #333; }
</style>