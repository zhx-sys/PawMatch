<template>
  <div class="followup-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>领养回访</span>
          <el-button v-if="!isShelter" type="primary" size="small" @click="openDialog">发布回访</el-button>
        </div>
      </template>
      <div v-if="followups.length === 0" class="empty">暂无回访记录</div>
      <div v-else class="followup-list">
        <div v-for="f in followups" :key="f.id" class="followup-item">
          <div class="followup-header">
            <span>领养记录 #{{ f.adoptionId }}</span>
            <span class="followup-time">{{ formatTime(f.createTime) }}</span>
          </div>
          <div class="followup-content">{{ f.content }}</div>
          <div v-if="f.images" class="followup-images">
            <el-image
              v-for="(img, idx) in f.images.split(',')"
              :key="idx"
              :src="img"
              fit="cover"
              class="followup-img"
              :preview-src-list="f.images.split(',')"
            />
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发布回访" width="500px" destroy-on-close top="2vh" class="followup-dialog">
      <el-form :model="form" label-width="80px">
        <el-form-item label="领养记录">
          <el-select v-model="form.adoptionId" placeholder="选择领养记录" style="width:100%" popper-class="followup-popper">
            <el-option v-for="a in adoptions" :key="a.id" :label="a.petName + '（' + a.petType + '）'" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="回访内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="描述宠物的近况..." />
        </el-form-item>
        <el-form-item label="上传照片" required>
          <el-upload
            ref="uploadRef"
            v-model:file-list="fileList"
            action="/api/upload"
            :headers="{ Authorization: 'Bearer ' + token }"
            list-type="picture-card"
            :limit="9"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload"
            :on-success="handleSuccess"
            :on-error="handleUploadError"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { followupAPI, adoptionAPI } from '@/api'

const dialogVisible = ref(false)
const followups = ref([])
const adoptions = ref([])
const fileList = ref([])
const uploadedUrls = ref([])
const form = ref({ adoptionId: null, content: '' })
const token = sessionStorage.getItem('token') || ''

const isShelter = computed(() => {
  const info = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  return info.userType === 1
})

onMounted(() => {
  loadFollowups()
  loadAdoptions()
})

async function loadFollowups() {
  try {
    const user = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    if (user.userType === 1) {
      const res = await followupAPI.byShelter(user.userId)
      followups.value = res?.data || []
    } else {
      const adops = await adoptionAPI.my()
      const ads = adops?.data?.records || []
      const all = []
      for (const a of ads) {
        const r = await followupAPI.byAdoption(a.id)
        all.push(...(r?.data || []))
      }
      followups.value = all
    }
  } catch { /* ignore */ }
}

async function loadAdoptions() {
  try {
    const res = await adoptionAPI.my()
    adoptions.value = (res?.data?.records || []).filter(a => a.status === 3)
  } catch { /* ignore */ }
}

function openDialog() {
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.adoptionId) { ElMessage.warning('请选择领养记录'); return }
  if (!form.value.content) { ElMessage.warning('请填写回访内容'); return }
  if (uploadedUrls.value.length === 0) { ElMessage.warning('请上传至少一张照片'); return }
  try {
    const user = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    await followupAPI.create({
      adoptionId: form.value.adoptionId,
      userId: user.userId,
      shelterId: user.userType === 1 ? user.userId : 0,
      content: form.value.content,
      images: uploadedUrls.value.join(',')
    })
    ElMessage.success('发布成功')
    dialogVisible.value = false
    form.value = { adoptionId: null, content: '' }
    fileList.value = []
    uploadedUrls.value = []
    loadFollowups()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '发布失败')
  }
}

function beforeUpload(file) {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片文件'); return false }
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('图片大小不能超过 5MB'); return false }
  return true
}

function handleSuccess(response) {
  uploadedUrls.value.push(response.data)
}

function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

function handleExceed() {
  ElMessage.warning('最多上传9张照片')
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.followup-page { max-width: 800px; margin: 20px auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.empty { text-align: center; color: #999; padding: 40px; }
.followup-item { padding: 16px 0; border-bottom: 1px solid #eee; }
.followup-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.followup-time { color: #999; font-size: 12px; }
.followup-content { color: #333; margin-bottom: 8px; }
.followup-images { display: flex; gap: 8px; flex-wrap: wrap; }
.followup-img { width: 100px; height: 100px; border-radius: 4px; }
</style>
<style>
.followup-popper { z-index: 9999 !important; }
.followup-dialog .el-dialog__body { max-height:calc(90vh - 120px); overflow-y:auto; }
</style>
