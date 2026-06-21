<template>
  <div class="page-content">
    <div class="report-page">
      <el-card>
        <template #header><span>举报管理</span></template>
        <div v-if="reports.length === 0" class="empty">暂无待处理举报</div>
        <el-table v-else :data="reports" stripe>
          <el-table-column label="举报人" prop="reporterName" width="100" />
          <el-table-column label="类型" width="80">
            <template #default="{ row }">{{ row.targetType === 'POST' ? '帖子' : '评论' }}</template>
          </el-table-column>
          <el-table-column label="被举报内容" min-width="250">
            <template #default="{ row }">
              <div v-if="row.targetTitle" class="target-title">{{ row.targetTitle }}</div>
              <div class="target-content">{{ row.targetContent || '(内容为空)' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="举报原因" prop="reason" width="120" show-overflow-tooltip />
          <el-table-column label="时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="review(row.id, 1)">通过</el-button>
              <el-button type="info" size="small" @click="review(row.id, 2)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import { reportAPI } from '@/api'

export default {
  name: 'ReportManagement',
  data() {
    return { reports: [] }
  },
  created() {
    this.fetchList()
  },
  methods: {
    async fetchList() {
      const res = await reportAPI.pending()
      this.reports = res.data || []
    },
    async review(id, status) {
      await reportAPI.review(id, status)
      this.reports = this.reports.filter(r => r.id !== id)
    },
    formatTime(t) {
      if (!t) return ''
      return t.replace('T', ' ').substring(0, 16)
    }
  }
}
</script>

<style scoped>
.report-page { max-width: 1100px; margin: 0 auto; }
.empty { text-align: center; color: #999; padding: 40px; }
.target-title { font-weight: bold; margin-bottom: 2px; }
.target-content { color: #666; font-size: 13px; max-height: 60px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
