<template>
  <div v-loading="loading" class="detail-page">
    <div class="container" v-if="pet">
      <el-row :gutter="40">
        <el-col :span="10">
          <el-carousel height="400px" v-if="images.length">
            <el-carousel-item v-for="(img, i) in images" :key="i">
              <div class="detail-img" :style="{ backgroundImage: `url(${img})` }"></div>
            </el-carousel-item>
          </el-carousel>
          <div v-else class="detail-img" style="background-color:#eee"></div>
        </el-col>
        <el-col :span="14">
          <h1>{{ pet.name }}</h1>
          <el-tag>{{ pet.type }}</el-tag>
          <el-tag type="success">{{ pet.breed }}</el-tag>
          <el-tag :type="pet.status===0?'warning':'info'">{{ pet.status===0?'待领养':'已领养' }}</el-tag>
          <el-descriptions :column="2" border style="margin-top:20px">
            <el-descriptions-item label="性别">{{ pet.gender }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ pet.age }}岁</el-descriptions-item>
            <el-descriptions-item label="颜色">{{ pet.color }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ pet.weight }}kg</el-descriptions-item>
            <el-descriptions-item label="健康状况">{{ pet.healthStatus }}</el-descriptions-item>
            <el-descriptions-item label="疫苗">{{ pet.vaccinated?'已接种':'未接种' }}</el-descriptions-item>
            <el-descriptions-item label="绝育">{{ pet.sterilized?'已绝育':'未绝育' }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ pet.createTime }}</el-descriptions-item>
          </el-descriptions>
          <p class="desc">{{ pet.description }}</p>
          <el-button type="primary" size="large" v-if="pet.status===0 && !isShelter" @click="showApply = true">申请领养</el-button>
          <el-button type="success" size="large" v-if="!isShelter && pet.shelterId" @click="contactShelter" style="margin-left:8px">联系救助站</el-button>
          <el-button size="large" :type="favorited ? 'warning' : 'default'"
            v-if="!isShelter" @click="toggleFavorite" style="margin-left:8px">{{ favorited ? '已收藏' : '收藏' }}</el-button>
        </el-col>
      </el-row>
    </div>

    <el-dialog v-model="showApply" title="申请领养" width="450px" top="2vh" class="detail-dialog">
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="申请理由"><el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="请简述您想领养这只宠物的原因" /></el-form-item>
        <el-form-item label="养宠经验"><el-input v-model="applyForm.experience" type="textarea" :rows="3" placeholder="请描述您过往的养宠经验" /></el-form-item>
        <el-form-item label="住房条件"><el-input v-model="applyForm.housingCondition" type="textarea" :rows="3" placeholder="请描述您的住房条件（如自有/租房、面积等）" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApply = false">取消</el-button>
        <el-button type="primary" @click="applyAdopt" :loading="applying">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { petAPI, adoptionAPI, petFavoriteAPI } from '../api'

const route = useRoute()
const router = useRouter()
const isShelter = computed(() => JSON.parse(sessionStorage.getItem('userInfo') || '{}')?.userType === 1)
const pet = ref(null)
const loading = ref(true)
const applying = ref(false)
const showApply = ref(false)
const applyForm = ref({ reason: '', experience: '', housingCondition: '' })
const favorited = ref(false)

const images = computed(() => {
  const raw = pet.value?.images
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  return raw.split(',').map(s => s.trim()).filter(Boolean)
})

onMounted(async () => {
  try {
    const res = await petAPI.detail(route.params.id)
    pet.value = res?.data
    if (pet.value && !isShelter.value) {
      try {
        const user = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
        if (user.userId) {
          const favRes = await petFavoriteAPI.status(user.userId, pet.value.id)
          favorited.value = favRes?.data?.favorited || false
        }
      } catch (e) { /* 静默失败 */ }
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
})

async function applyAdopt() {
  applying.value = true
  try {
    await adoptionAPI.apply({ petId: pet.value.id, ...applyForm.value })
    ElMessage.success('申请已提交')
    showApply.value = false
    applyForm.value = { reason: '', experience: '', housingCondition: '' }
  } catch (e) {
    if (e?.response?.status === 409) {
      try {
        await ElMessageBox.confirm('24小时内已申请3次，继续提交将扣除10信用分，是否继续？', '警告', {
          confirmButtonText: '确认继续',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await adoptionAPI.apply({ petId: pet.value.id, ...applyForm.value, confirmFlood: true })
        ElMessage.success('申请已提交')
        showApply.value = false
        applyForm.value = { reason: '', experience: '', housingCondition: '' }
      } catch (confirmErr) {
        if (confirmErr !== 'cancel' && confirmErr?.message) {
          ElMessage.error(confirmErr.message)
        }
      }
    } else {
      ElMessage.error(e.message)
    }
  } finally {
    applying.value = false
  }
}

function contactShelter() {
  router.push({
    path: '/messages',
    query: { shelterId: pet.value.shelterId, petId: pet.value.id }
  })
}

async function toggleFavorite() {
  const user = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
  try {
    const res = await petFavoriteAPI.toggle(user.userId, pet.value.id)
    favorited.value = res?.data?.favorited
    ElMessage.success(favorited.value ? '已收藏' : '已取消收藏')
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>

<style scoped>
.detail-page { padding: 20px 40px; min-height: calc(100vh - 60px); }
.detail-img { width:100%; height:100%; background-size:cover; background-position:center; border-radius:12px; }
.desc { margin:20px 0; line-height:1.8; color:#666; }
</style>
<style>
.detail-dialog .el-dialog__body { max-height:calc(90vh - 120px); overflow-y:auto; }
</style>
