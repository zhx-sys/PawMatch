<template>
  <div class="page-content">
    <div class="container">
      <el-row :gutter="24">
        <!-- 左侧：个人信息 -->
        <el-col :span="24">
          <el-card class="profile-card" shadow="never">
            <template #header>
              <div class="card-header">
                <h2>{{ info?.nickname || info?.shelterName || '个人中心' }}</h2>
                <div class="header-actions">
                  <el-button v-if="!isShelter" type="warning" size="small" @click="$router.push('/questionnaire')">
                    匹配画像
                  </el-button>
                  <el-button type="primary" size="small" @click="editDialogVisible = true" v-if="!isShelter">
                    <el-icon style="margin-right:4px"><Edit /></el-icon>编辑资料
                  </el-button>
                </div>
              </div>
            </template>

            <!-- 信用分展示 -->
            <div class="credit-score-bar" v-if="!isShelter">
              <div class="credit-main">
                <div class="credit-ring">
                  <el-progress type="dashboard" :percentage="info?.creditScore || 100" :width="80" :stroke-width="8" :color="creditColor">
                    <template #default="{ percentage }">
                      <span class="credit-value">{{ percentage }}</span>
                    </template>
                  </el-progress>
                </div>
                <div class="credit-label">
                  <span class="credit-title">信用分</span>
                  <el-button link type="primary" size="small" @click="showCreditLogs = true">查看记录</el-button>
                </div>
              </div>
            </div>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="账号">{{ info?.account }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ info?.userType===1?'救助站':'普通用户' }}</el-descriptions-item>
              <el-descriptions-item v-if="info?.email" label="邮箱">{{ info.email }}</el-descriptions-item>
              <el-descriptions-item v-if="info?.phone" label="电话">{{ info.phone }}</el-descriptions-item>
              <el-descriptions-item v-if="info?.city" label="城市">{{ info.province }}{{ info.city }}</el-descriptions-item>
              <el-descriptions-item v-if="!isShelter && info?.petPreference" label="宠物偏好">
                <el-tag v-for="tag in preferenceTags" :key="tag" size="small" type="warning" style="margin-right:6px">
                  {{ tag }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item v-if="!isShelter" label="匹配画像">
                <el-tag :type="info?.matchingProfileComplete ? 'success' : 'info'" size="small">
                  {{ info?.matchingProfileComplete ? '已填写' : '未填写' }}
                </el-tag>
                <el-button v-if="!info?.matchingProfileComplete" link type="warning" size="small" style="margin-left:8px" @click="$router.push('/questionnaire')">
                  去填写
                </el-button>
              </el-descriptions-item>
            </el-descriptions>

            <!-- 未设置偏好的引导 -->
            <div v-if="!isShelter && !info?.matchingProfileComplete" class="preference-hint">
              <el-icon><InfoFilled /></el-icon>
              <span>完善匹配画像，首页将为你智能推荐合适的毛孩子</span>
              <el-button type="warning" size="small" @click="$router.push('/questionnaire')">立即填写</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-divider />

      <template v-if="isShelter">
        <h3>收到的领养申请</h3>
        <el-table :data="adoptions" stripe style="margin-top:16px">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="petName" label="宠物" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status===0?'warning':row.status===1?'success':row.status===2?'danger':'info'">
                {{ ['待审核','已通过','已拒绝','已完成'][row.status] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="180" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <router-link :to="'/pet/' + row.petId">查看宠物</router-link>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template v-else>
        <h3>我的领养申请</h3>
        <el-table :data="adoptions" stripe style="margin-top:16px">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="petName" label="宠物" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status===0?'warning':row.status===1?'success':row.status===2?'danger':'info'">
                {{ ['待审核','已通过','已拒绝','已取消'][row.status] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="180" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <router-link :to="'/pet/' + row.petId">查看宠物</router-link>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <el-divider />

      <h3>消息通知</h3>
      <el-timeline style="margin-top:16px">
        <el-timeline-item v-for="n in notifications" :key="n.id" :timestamp="n.createTime" placement="top">
          <el-card>
            <h4>{{ n.title }}</h4>
            <p>{{ n.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <p v-if="!notifications.length" style="color:#999;text-align:center;padding:20px">暂无通知</p>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑资料" width="480px" destroy-on-close top="2vh" class="center-dialog">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            placeholder="选择生日"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
        <el-divider>修改密码（留空则不修改）</el-divider>
        <el-form-item label="旧密码">
          <el-input v-model="editForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="editForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="editForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 信用记录弹窗 -->
    <el-dialog v-model="showCreditLogs" title="信用记录" width="700px" destroy-on-close top="2vh" class="center-dialog">
      <el-table :data="creditRecords" stripe max-height="360">
        <el-table-column prop="reasonType" label="类型" width="150">
          <template #default="{ row }">
            <el-tag :type="row.scoreChange > 0 ? 'success' : row.scoreChange < 0 ? 'danger' : 'info'" size="small">
              {{ creditTypeLabel(row.reasonType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scoreChange" label="分值变化" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.scoreChange > 0 ? '#67c23a' : row.scoreChange < 0 ? '#f56c6c' : '#999' }">
              {{ row.scoreChange > 0 ? '+' : '' }}{{ row.scoreChange }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="scoreAfter" label="变更后" width="80" />
        <el-table-column prop="reasonDetail" label="原因" />
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      <div v-if="!creditRecords.length" style="text-align:center;color:#999;padding:30px">暂无信用记录</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { Edit, InfoFilled } from '@element-plus/icons-vue'
import { userAPI, adoptionAPI, creditAPI } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const { isShelter } = storeToRefs(userStore)
const info = ref(null)
const adoptions = ref([])
const notifications = ref([])
const editDialogVisible = ref(false)
const saving = ref(false)
const editForm = ref({
  nickname: '',
  birthday: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const showCreditLogs = ref(false)
const creditRecords = ref([])

const preferenceTags = computed(() => {
  const p = info.value?.petPreference
  if (!p) return []
  return p.split(/[,，\s]+/).filter(Boolean)
})

const creditColor = computed(() => {
  const s = info.value?.creditScore || 100
  if (s >= 80) return '#67c23a'
  if (s >= 60) return '#e6a23c'
  return '#f56c6c'
})

watch(showCreditLogs, (val) => {
  if (val) loadCreditLogs()
})

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    const [infoRes, notifRes] = await Promise.all([
      userAPI.info(),
      userAPI.notifications()
    ])
    const userData = infoRes?.data
    info.value = userData
    userStore.setUserInfo(userData)
    const shelter = userData?.userType === 1
    const adpRes = shelter ? await adoptionAPI.list() : await adoptionAPI.my()
    adoptions.value = adpRes?.data?.records || adpRes?.data || []
    notifications.value = notifRes?.data?.records || notifRes?.data || []
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  }
}

function openEdit() {
  const i = info.value
  editForm.value = {
    nickname: i?.nickname || '',
    birthday: i?.birthday || '',
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  editDialogVisible.value = true
}

async function saveProfile() {
  if (editForm.value.newPassword) {
    if (editForm.value.newPassword.length < 6) {
      ElMessage.warning('新密码至少6位')
      return
    }
    if (editForm.value.newPassword !== editForm.value.confirmPassword) {
      ElMessage.warning('两次输入的新密码不一致')
      return
    }
    if (!editForm.value.oldPassword) {
      ElMessage.warning('请输入旧密码')
      return
    }
  }
  saving.value = true
  try {
    const payload = {
      nickname: editForm.value.nickname.trim(),
      birthday: editForm.value.birthday || null
    }
    if (editForm.value.newPassword) {
      payload.oldPassword = editForm.value.oldPassword
      payload.newPassword = editForm.value.newPassword
    }
    await userAPI.update(payload)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    if (payload.nickname) {
      userStore.setUserInfo({ nickname: payload.nickname })
    }
    await loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function loadCreditLogs() {
  try {
    const res = await creditAPI.logs({ pageNum: 1, pageSize: 100 })
    creditRecords.value = res?.data?.records || []
  } catch (e) {
    creditRecords.value = []
  }
}

function creditTypeLabel(type) {
  const map = {
    INFO_COMPLETE: '信息完善',
    APPLY_SUBMIT: '提交申请',
    ADOPTION_COMPLETE: '领养完成',
    ADOPTION_REJECTED: '申请被拒',
    FOLLOWUP_DONE: '完成回访',
    FOLLOWUP_MISSED: '逾期回访',
    REPORT_VERIFIED: '举报核实',
    FLOOD_APPLY: '频繁申请'
  }
  return map[type] || type
}

function logout() { userStore.logout(); router.push('/login') }
</script>

<style scoped>
.page-content { padding-top: 60px; }
.profile-card { margin-bottom: 20px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #3d322b;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.credit-score-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
}
.credit-main {
  display: flex;
  align-items: center;
  gap: 20px;
}
.credit-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.credit-title {
  font-size: 18px;
  font-weight: 600;
  color: #3d322b;
}
.credit-value {
  font-size: 20px;
  font-weight: bold;
}
.preference-hint {
  margin-top: 16px;
  padding: 14px 18px;
  background: #fef6ea;
  border: 1px solid #f5c17e;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #b87316;
  font-size: 14px;
}
.preference-hint .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.preference-hint span {
  flex: 1;
}
.form-tip {
  color: #999;
  font-size: 12px;
  margin-top: 6px;
}
</style>
<style>
.center-dialog .el-dialog__body { max-height:calc(90vh - 120px); overflow-y:auto; }
</style>