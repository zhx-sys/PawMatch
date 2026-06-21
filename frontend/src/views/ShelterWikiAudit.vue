<template>
  <div class="page-content">
    <div class="container">
      <h2>百科审核</h2>
      <el-table :data="entries" v-loading="loading" style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="review(row.id, true)">通过</el-button>
            <el-button type="danger" size="small" @click="review(row.id, false)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadList"
        style="margin-top:20px;justify-content:center"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { wikiAPI } from '@/api'

const entries = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await wikiAPI.reviewList({ pageNum: pageNum.value, pageSize: pageSize.value })
    entries.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function review(id, approved) {
  try {
    await wikiAPI.reviewEntry(id, approved)
    ElMessage.success(approved ? '已通过' : '已拒绝')
    loadList()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}
</script>

<style scoped>
.page-content { padding-top: 60px; min-height: 100vh; background: #f7f4f0; }
.container { max-width: 1000px; margin: 0 auto; padding: 20px; }
</style>
