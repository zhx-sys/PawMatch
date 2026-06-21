<template>
  <div class="page-content">
<div>
        <div class="toolbar">
          <h2>领养审核</h2>
          <el-radio-group v-model="statusFilter" @change="loadList">
            <el-radio-button :value="null">全部</el-radio-button>
            <el-radio-button :value="0">待审核</el-radio-button>
            <el-radio-button :value="1">已通过</el-radio-button>
            <el-radio-button :value="2">已拒绝</el-radio-button>
            <el-radio-button :value="3">已完成</el-radio-button>
          </el-radio-group>
        </div>

        <el-table :data="list" v-loading="loading" style="margin-top:16px">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="petName" label="宠物" width="120" />
          <el-table-column prop="petType" label="类型" width="80" />
          <el-table-column prop="userName" label="申请人" width="100" />
          <el-table-column prop="reason" label="领养理由" min-width="150" show-overflow-tooltip />
          <el-table-column prop="experience" label="饲养经验" min-width="150" show-overflow-tooltip />
          <el-table-column prop="housingCondition" label="住房条件" width="100" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="160" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" type="success" size="small" @click="audit(row.id, 1)">通过</el-button>
              <el-button v-if="row.status === 0" type="danger" size="small" @click="audit(row.id, 2)">拒绝</el-button>
              <el-button v-if="row.status === 1" type="primary" size="small" @click="complete(row.id)">完成领养</el-button>
              <span v-if="row.status === 2 && row.rejectReason" style="color:#999;font-size:12px">原因: {{ row.rejectReason }}</span>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="total > 0"
          style="margin-top:16px;justify-content:flex-end"
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="pageNum"
          @current-change="loadList"
        />
      </div>

    <!-- 拒绝原因弹窗 -->
    <Teleport to="body">
      <div v-if="rejectVisible" class="del-overlay" @click.self="rejectVisible = false">
        <div class="del-dialog" style="min-width:420px">
          <h3>拒绝申请</h3>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" style="margin:12px 0 20px" />
          <div class="del-actions">
            <el-button @click="rejectVisible = false">取消</el-button>
            <el-button type="danger" @click="doReject" :loading="rejectLoading">确认拒绝</el-button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 完成领养确认弹窗 -->
    <Teleport to="body">
      <div v-if="completeVisible" class="del-overlay" @click.self="completeVisible = false">
        <div class="del-dialog">
          <h3>确认</h3>
          <p>确认标记该领养已完成？</p>
          <div class="del-actions">
            <el-button @click="completeVisible = false">取消</el-button>
            <el-button type="primary" @click="doComplete" :loading="completeLoading">确认</el-button>
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
import { adoptionAPI } from '../api'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const statusFilter = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 拒绝弹窗
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectLoading = ref(false)
let pendingAuditId = null

// 完成领养弹窗
const completeVisible = ref(false)
const completeLoading = ref(false)
let pendingCompleteId = null

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await adoptionAPI.list(params)
    list.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function audit(id, status) {
  if (status === 2) {
    pendingAuditId = id
    rejectReason.value = ''
    rejectVisible.value = true
    return
  }
  try {
    await adoptionAPI.audit(id, { status })
    ElMessage.success('申请已通过')
    loadList()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function doReject() {
  rejectLoading.value = true
  try {
    await adoptionAPI.audit(pendingAuditId, { status: 2, rejectReason: rejectReason.value })
    ElMessage.success('申请已拒绝')
    rejectVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    rejectLoading.value = false
  }
}

async function complete(id) {
  pendingCompleteId = id
  completeVisible.value = true
}

async function doComplete() {
  completeLoading.value = true
  try {
    await adoptionAPI.complete(pendingCompleteId)
    ElMessage.success('领养已完成')
    completeVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    completeLoading.value = false
  }
}

function statusType(status) {
  return { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status]
}

function statusText(status) {
  return { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已完成' }[status]
}

function logout() { sessionStorage.clear(); router.push('/login') }
</script>

<style scoped>
.page-content { padding-top: 60px; }
.toolbar { display:flex; align-items:center; justify-content:space-between; }
.toolbar h2 { margin:0; }
</style>