<template>
  <div class="page-content">
    <div class="container">
      <div class="wiki-layout">
        <!-- 左：分类树 -->
        <aside class="wiki-sidebar">
          <h3>知识分类</h3>
          <el-tree
            :data="categories"
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            :default-expand-all="true"
            @node-click="handleNodeClick"
            highlight-current
          />
        </aside>
        <!-- 右：词条列表 -->
        <main class="wiki-main" v-loading="loadingEntries">
          <div class="topbar">
            <h2>{{ currentCategoryName || '全部词条' }}</h2>
            <div class="topbar-right">
              <el-input v-model="keyword" placeholder="搜索词条..." style="width:220px" clearable @input="search" />
              <el-select v-model="sortBy" style="width:130px;margin-left:8px" @change="loadEntries">
                <el-option label="最新发布" value="newest" />
                <el-option label="最有帮助" value="helpful" />
              </el-select>
              <el-button type="primary" @click="$router.push('/wiki/create')" style="margin-left:8px">创建词条</el-button>
            </div>
          </div>
          <div v-if="entries.length === 0" class="empty">暂无词条</div>
          <el-card v-for="entry in entries" :key="entry.id" class="entry-card" @click="$router.push('/wiki/' + entry.id)">
            <h3>{{ entry.title }}</h3>
            <p class="entry-summary">{{ entry.summary || '暂无简介' }}</p>
            <div class="entry-meta">
              <span>{{ entry.categoryName }}</span>
              <span>{{ entry.authorName }}</span>
              <span>浏览 {{ entry.viewCount }}</span>
              <span>有帮助 {{ entry.helpfulCount }}</span>
              <span>{{ entry.createTime }}</span>
              <el-button
                v-if="isShelter"
                type="danger"
                size="small"
                class="delist-btn"
                @click.stop="showDelist(entry)"
              >下架</el-button>
            </div>
          </el-card>
          <el-pagination
            v-if="total > pageSize"
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadEntries"
            style="margin-top:20px;justify-content:center"
          />
        </main>
      </div>
    </div>
    <!-- 下架确认弹窗 -->
    <Teleport to="body">
      <div v-if="delistVisible" class="del-overlay" @click.self="cancelDelist">
        <div class="del-dialog">
          <p>确定要下架词条「{{ delistTarget?.title }}」吗？下架后其他用户将无法看到该词条。</p>
          <div class="del-actions">
            <button class="del-cancel" @click="cancelDelist">取消</button>
            <button class="del-confirm" :disabled="delistLoading" @click="doDelist">
              {{ delistLoading ? '下架中...' : '确认下架' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { wikiAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const { userInfo, isShelter } = storeToRefs(userStore)

const categories = ref([])
const loadingCategories = ref(false)
const entries = ref([])
const loadingEntries = ref(false)
const keyword = ref('')
const sortBy = ref('newest')
const categoryId = ref(null)
const currentCategoryName = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 下架相关
const delistVisible = ref(false)
const delistTarget = ref(null)
const delistLoading = ref(false)

function showDelist(entry) {
  delistTarget.value = entry
  delistVisible.value = true
}

function cancelDelist() {
  delistVisible.value = false
  delistTarget.value = null
}

async function doDelist() {
  delistLoading.value = true
  try {
    await wikiAPI.delistEntry(delistTarget.value.id)
    ElMessage.success('下架成功')
    delistVisible.value = false
    delistTarget.value = null
    loadEntries()
  } catch (e) {
    ElMessage.error(e?.message || '下架失败')
  } finally {
    delistLoading.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadEntries()
})

async function loadCategories() {
  loadingCategories.value = true
  try {
    const res = await wikiAPI.categories()
    categories.value = res?.data || []
  } catch (e) {} finally {
    loadingCategories.value = false
  }
}

async function loadEntries() {
  loadingEntries.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, sortBy: sortBy.value }
    if (keyword.value) params.keyword = keyword.value
    if (categoryId.value) params.categoryId = categoryId.value
    const res = await wikiAPI.entryList(params)
    entries.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loadingEntries.value = false
  }
}

function handleNodeClick(data) {
  categoryId.value = data.id
  currentCategoryName.value = data.name
  pageNum.value = 1
  loadEntries()
}

let timer
function search() {
  clearTimeout(timer)
  timer = setTimeout(() => {
    pageNum.value = 1
    loadEntries()
  }, 400)
}
</script>

<style scoped>
.page-content { padding-top: 60px; min-height: 100vh; background: #f7f4f0; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.wiki-layout { display: flex; gap: 20px; }
.wiki-sidebar {
  width: 220px; flex-shrink: 0;
  background: #fff; border-radius: 12px; padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  height: fit-content;
}
.wiki-sidebar h3 { margin: 0 0 12px; font-size: 16px; color: #e67e22; }
.wiki-main { flex: 1; }
.topbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 8px; }
.topbar h2 { margin: 0; font-size: 20px; }
.topbar-right { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.entry-card { margin-bottom: 12px; cursor: pointer; transition: box-shadow .2s; }
.entry-card:hover { box-shadow: 0 4px 16px rgba(230,126,34,.12); }
.entry-card h3 { margin: 0 0 6px; color: #333; }
.entry-summary { color: #888; margin: 0 0 8px; font-size: 14px; }
.entry-meta { display: flex; gap: 16px; color: #bbb; font-size: 13px; flex-wrap: wrap; }
.empty { text-align: center; padding: 60px 0; color: #bbb; font-size: 16px; }
.delist-btn { margin-left: auto; }
</style>