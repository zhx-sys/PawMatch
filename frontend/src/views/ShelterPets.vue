<template>
  <div class="page-content">
<div>
        <div class="container">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
            <h2 style="margin:0">宠物管理</h2>
            <el-button type="primary" @click="openAddDialog">发布新宠物</el-button>
          </div>

          <el-row :gutter="16">
            <el-col :span="8" v-for="pet in pets" :key="pet.id" style="margin-bottom:16px">
              <el-card :body-style="{ padding: '0' }" shadow="hover" class="pet-card" @click="$router.push(`/pet/${pet.id}`)">
                <div class="pet-img" :style="{ backgroundImage: `url(${getFirstImage(pet.images)})` }"></div>
                <div class="pet-info">
                  <h4>{{ pet.name }}</h4>
                  <p>{{ pet.type }} · {{ pet.breed }} · {{ pet.age }}岁</p>
                  <el-tag :type="statusTagType(pet.status)" size="small">{{ statusText(pet.status) }}</el-tag>
                  <div class="pet-actions" @click.stop>
                    <el-button v-if="pet.status===0 || pet.status===1" size="small" type="danger" @click="delist(pet)">下架</el-button>
                    <el-button v-if="pet.status===2" size="small" type="success" @click="restore(pet)">上架</el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="!pets.length" description="还没有发布宠物" />
        </div>
      </div>
    <el-dialog v-model="showAddDialog" title="发布新宠物" width="600px" top="2vh" @closed="resetForm" class="pet-form-dialog">
      <el-form :model="newPet" label-width="80px" ref="petFormRef">
        <el-form-item label="名称"><el-input v-model="newPet.name" placeholder="请输入宠物名称" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="newPet.type" style="width:100%" popper-class="pet-form-popper">
            <el-option label="狗" value="狗" />
            <el-option label="猫" value="猫" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种"><el-input v-model="newPet.breed" placeholder="如：金毛、英短" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="newPet.gender">
            <el-radio value="公">公</el-radio>
            <el-radio value="母">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="newPet.age" :min="0" :max="30" style="width:100%" /> 岁</el-form-item>
        <el-form-item label="颜色"><el-input v-model="newPet.color" placeholder="毛色" /></el-form-item>
        <el-form-item label="体重(kg)"><el-input-number v-model="newPet.weight" :min="0" :precision="1" style="width:100%" /></el-form-item>
        <el-form-item label="健康状况">
          <el-select v-model="newPet.healthStatus" style="width:100%" placeholder="请选择" popper-class="pet-form-popper">
            <el-option label="健康" value="健康" />
            <el-option label="亚健康" value="亚健康" />
            <el-option label="生病" value="生病" />
          </el-select>
        </el-form-item>
        <el-form-item label="疫苗"><el-switch v-model="newPet.vaccinated" /></el-form-item>
        <el-form-item label="绝育"><el-switch v-model="newPet.sterilized" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="newPet.description" type="textarea" :rows="3" placeholder="描述宠物的性格、习惯等" /></el-form-item>
        <el-divider content-position="left">匹配画像（帮助系统精准推荐）</el-divider>
        <el-form-item label="体型大小">
          <el-select v-model="newPet.sizeLevel" style="width:100%" placeholder="请选择" popper-class="pet-form-popper">
            <el-option label="小型" value="小型" />
            <el-option label="中型" value="中型" />
            <el-option label="大型" value="大型" />
          </el-select>
        </el-form-item>
        <el-form-item label="活跃程度">
          <el-select v-model="newPet.activityLevel" style="width:100%" placeholder="请选择" popper-class="pet-form-popper">
            <el-option label="活泼好动" value="活泼好动" />
            <el-option label="温顺安静" value="温顺安静" />
            <el-option label="粘人精" value="粘人精" />
            <el-option label="独立自主" value="独立自主" />
          </el-select>
        </el-form-item>
        <el-form-item label="适合新手"><el-switch v-model="newPet.beginnerFriendly" /></el-form-item>
        <el-form-item label="适合儿童"><el-switch v-model="newPet.goodWithKids" /></el-form-item>
        <el-form-item label="适合多宠家庭"><el-switch v-model="newPet.goodWithPets" /></el-form-item>
        <el-form-item label="图片">
          <el-upload
            ref="uploadRef"
            action="/api/pet/upload"
            :headers="uploadHeaders"
            list-type="picture-card"
            :auto-upload="false"
            v-model:file-list="fileList"
            multiple
            :limit="9"
          >
            <el-icon><Plus /></el-icon>
            <template #file="{ file }">
              <div>
                <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
                <span class="el-upload-list__item-actions">
                  <span class="el-upload-list__item-preview" @click="handlePreview(file)">
                    <el-icon><ZoomIn /></el-icon>
                  </span>
                  <span class="el-upload-list__item-delete" @click="handleRemove(file)">
                    <el-icon><Delete /></el-icon>
                  </span>
                </span>
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPet" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="500px">
      <img :src="previewUrl" style="width:100%" alt="预览" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ZoomIn, Delete } from '@element-plus/icons-vue'
import { petAPI } from '../api'

const router = useRouter()
const user = ref(JSON.parse(sessionStorage.getItem('userInfo') || '{}'))
const isShelter = computed(() => user.value?.userType === 1)

const pets = ref([])
const showAddDialog = ref(false)
const submitting = ref(false)
const uploadRef = ref(null)
const petFormRef = ref(null)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const newPet = ref({
  name: '', type: '狗', breed: '', gender: '公', age: 1,
  color: '', weight: 5, healthStatus: '健康',
  vaccinated: true, sterilized: false, description: '',
  sizeLevel: '', activityLevel: '', beginnerFriendly: false, goodWithKids: false, goodWithPets: false
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${sessionStorage.getItem('token') || ''}`
}))

onMounted(() => loadPets())

async function loadPets() {
  try {
    const res = await petAPI.list({ pageNum: 1, pageSize: 100 })
    pets.value = res?.data?.records || []
  } catch (e) {}
}

function getFirstImage(images) {
  if (!images) return '/placeholder.jpg'
  if (Array.isArray(images)) return images[0] || '/placeholder.jpg'
  if (typeof images === 'string') return images.split(',')[0]?.trim() || '/placeholder.jpg'
  return '/placeholder.jpg'
}

function statusText(status) {
  if (status === 0) return '待领养'
  if (status === 1) return '已领养'
  if (status === 2) return '已下架'
  return '未知'
}

function statusTagType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'info'
  if (status === 2) return ''
  return 'info'
}

async function delist(pet) {
  try {
    await ElMessageBox.confirm(`确定下架「${pet.name}」？下架后将不在用户端展示。`, '确认', { type: 'warning' })
    await petAPI.delete(pet.id)
    ElMessage.success('已下架')
    loadPets()
  } catch (e) { /* 取消或失败 */ }
}

async function restore(pet) {
  try {
    await ElMessageBox.confirm(`确定上架「${pet.name}」？`, '确认', { type: 'warning' })
    await petAPI.restore(pet.id)
    ElMessage.success('已上架')
    loadPets()
  } catch (e) { /* 取消或失败 */ }
}

function openAddDialog() {
  resetForm()
  showAddDialog.value = true
}

function onDialogOpened() {
  nextTick(() => {
    const body = document.querySelector('.pet-form-dialog .el-dialog__body')
    if (body) body.scrollTop = 0
  })
}

function resetForm() {
  newPet.value = {
    name: '', type: '狗', breed: '', gender: '公', age: 1,
    color: '', weight: 5, healthStatus: '健康',
    vaccinated: true, sterilized: false, description: '',
    sizeLevel: '', activityLevel: '', beginnerFriendly: false, goodWithKids: false, goodWithPets: false
  }
  fileList.value = []
}

function handlePreview(file) {
  previewUrl.value = file.url
  previewVisible.value = true
}

function handleRemove(file) {
  const index = fileList.value.indexOf(file)
  if (index > -1) fileList.value.splice(index, 1)
}

async function submitPet() {
  if (!newPet.value.name) { ElMessage.warning('请输入宠物名称'); return }
  submitting.value = true
  try {
    const formData = new FormData()
    Object.entries(newPet.value).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        formData.append(key, value)
      }
    })
    fileList.value.forEach(file => {
      if (file.raw) {
        formData.append('images', file.raw)
      }
    })
    await petAPI.create(formData)
    ElMessage.success('发布成功')
    showAddDialog.value = false
    loadPets()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

function logout() { sessionStorage.clear(); router.push('/login') }
</script>

<style scoped>
.page-content { padding-top: 60px; }
.pet-card { cursor:pointer; border-radius:12px; overflow:hidden; transition:transform .2s; }
.pet-card:hover { transform:translateY(-4px); }
.pet-img { height:200px; background-size:cover; background-position:center; background-color:#eee; }
.pet-info { padding:14px; }
.pet-info h4 { margin-bottom:4px; }
.pet-info p { color:#888; font-size:13px; margin-bottom:8px; }
.pet-actions { margin-top:10px; display:flex; gap:8px; }
</style>
<style>
.pet-form-dialog .el-dialog__body { max-height:calc(90vh - 120px); overflow-y:auto; }
.pet-form-popper { z-index: 9999 !important; }
</style>