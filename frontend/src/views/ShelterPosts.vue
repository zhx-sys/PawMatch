<template>
  <div class="page-content">
<div>
        <div class="container">
          <div class="topbar">
            <h2>帖子审核</h2>
          </div>
          <el-table :data="posts" border stripe style="width:100%">
            <el-table-column prop="id" label="编号" width="70" />
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column label="摘要" min-width="200">
              <template #default="{ row }">{{ row.content?.slice(0, 80) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="170" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="audit(row.id, true)">通过</el-button>
                <el-button size="small" type="danger" @click="audit(row.id, false)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadPosts"
            style="margin-top:20px; justify-content:center"
          />
          <p v-if="!posts.length" style="text-align:center; color:#999; margin-top:40px">暂无待审核帖子</p>
        </div>
      </div>

    <!-- 审核确认弹窗 -->
    <Teleport to="body">
      <div v-if="auditVisible" class="del-overlay" @click.self="auditVisible = false">
        <div class="del-dialog">
          <h3>确认</h3>
          <p>确定{{ auditAction }}该帖子？</p>
          <div class="del-actions">
            <el-button @click="auditVisible = false">取消</el-button>
            <el-button :type="auditApproved ? 'success' : 'danger'" @click="doAudit" :loading="auditLoading">确认</el-button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { communityAPI } from '../api'

const router = useRouter()
const posts = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 审核弹窗
const auditVisible = ref(false)
const auditAction = ref('')
const auditApproved = ref(false)
const auditLoading = ref(false)
let pendingAuditId = null

onMounted(loadPosts)

async function loadPosts() {
  try {
    const res = await communityAPI.reviewList({ pageNum: pageNum.value, pageSize: pageSize.value })
    const data = res?.data
    posts.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {}
}

function audit(id, approved) {
  pendingAuditId = id
  auditApproved.value = approved
  auditAction.value = approved ? '通过' : '拒绝'
  auditVisible.value = true
}

async function doAudit() {
  auditLoading.value = true
  try {
    await communityAPI.reviewPost(pendingAuditId, auditApproved.value)
    ElMessage.success(`已${auditAction.value}`)
    auditVisible.value = false
    loadPosts()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    auditLoading.value = false
  }
}

function logout() { sessionStorage.clear(); router.push('/login') }
</script>

<style scoped>
.page-content { padding-top: 60px; }
.topbar { margin-bottom:20px; }
</style>
