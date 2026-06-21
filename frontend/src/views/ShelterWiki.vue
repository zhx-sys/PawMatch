<template>
  <div class="page-content">
    <div class="container">
      <div class="topbar">
        <h2>我的百科</h2>
      </div>
      <el-table :data="entries" border stripe style="width:100%">
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="viewCount" label="浏览量" width="80" />
        <el-table-column prop="helpfulCount" label="有帮助" width="80" />
        <el-table-column label="摘要" min-width="200">
          <template #default="{ row }">{{ row.summary?.slice(0, 80) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="delist(row)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadEntries"
        style="margin-top:20px; justify-content:center"
      />
      <p v-if="!entries.length" style="text-align:center; color:#999; margin-top:40px">暂无已发布词条</p>
    </div>

    <!-- 下架确认弹窗 -->
    <Teleport to="body">
      <div v-if="delistVisible" class="del-overlay" @click.self="delistVisible = false">
        <div class="del-dialog">
          <h3>确认下架</h3>
          <p>确定下架词条「{{ delistTarget?.title }}」？下架后该词条将不再公开显示。</p>
          <div class="del-actions">
            <el-button @click="delistVisible = false">取消</el-button>
            <el-button type="danger" @click="doDelist" :loading="delistLoading">确认下架</el-button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { wikiAPI } from '../api'

const entries = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 下架弹窗
const delistVisible = ref(false)
const delistTarget = ref(null)
const delistLoading = ref(false)

onMounted(loadEntries)

async function loadEntries() {
  try {
    const res = await wikiAPI.myEntries({ pageNum: pageNum.value, pageSize: pageSize.value })
    const data = res?.data
    entries.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {}
}

function delist(row) {
  delistTarget.value = row
  delistVisible.value = true
}

async function doDelist() {
  delistLoading.value = true
  try {
    await wikiAPI.delistEntry(delistTarget.value.id)
    ElMessage.success('词条已下架')
    delistVisible.value = false
    loadEntries()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    delistLoading.value = false
  }
}
</script>

<style scoped>
.page-content { padding-top: 60px; }
.topbar { margin-bottom: 20px; }
</style>