<template>
  <div class="page-content">
<div>
        <div class="container">
          <template v-if="!isShelter">
            <div class="hero">
              <h1>找到你的毛孩子</h1>
              <p>领养代替购买，给流浪动物一个温暖的家</p>
              <el-button type="primary" size="large" @click="$router.push('/pets')">浏览待领养宠物</el-button>
            </div>

            <el-divider />

            <el-tabs v-model="activeTab" class="home-tabs">
              <el-tab-pane label="智能推荐" name="recommend">
                <div v-if="!hasProfile" class="no-recommend">
                  <p>完善匹配画像后，系统将根据你的居住空间、经验、偏好等为你智能推荐最合适的宠物</p>
                  <el-button type="warning" size="small" @click="$router.push('/questionnaire')">填写匹配画像</el-button>
                </div>
                <el-row v-else :gutter="20">
                  <el-col :span="8" v-for="pet in matchedPets" :key="pet.id">
                    <el-card :body-style="{ padding: '0' }" shadow="hover" class="pet-card" @click="$router.push(`/pet/${pet.id}`)">
                      <div class="pet-img" :style="{ backgroundImage: `url(${getFirstImage(pet.images)})` }">
                        <div class="match-badge" v-if="pet.matchScore">
                          <el-progress type="dashboard" :percentage="pet.matchScore" :width="48" :stroke-width="6" :color="scoreColor(pet.matchScore)">
                            <template #default="{ percentage }">
                              <span class="match-value">{{ percentage }}%</span>
                            </template>
                          </el-progress>
                        </div>
                      </div>
                      <div class="pet-info">
                        <h4>{{ pet.name }}</h4>
                        <p>{{ pet.type }} · {{ pet.breed }} · {{ pet.gender === '公' ? '♂' : '♀' }}</p>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col v-if="matchedPets.length === 0 && hasProfile" :span="24">
                    <div class="no-recommend">
                      <p>暂无可匹配的宠物，请稍后再试</p>
                    </div>
                  </el-col>
                </el-row>
              </el-tab-pane>
              <el-tab-pane label="全部宠物" name="all">
                <el-row :gutter="20">
                  <el-col :span="8" v-for="pet in pets" :key="pet.id">
                    <el-card :body-style="{ padding: '0' }" shadow="hover" class="pet-card" @click="$router.push(`/pet/${pet.id}`)">
                      <div class="pet-img" :style="{ backgroundImage: `url(${getFirstImage(pet.images)})` }"></div>
                      <div class="pet-info">
                        <h4>{{ pet.name }}</h4>
                        <p>{{ pet.type }} · {{ pet.breed }} · {{ pet.gender === '公' ? '♂' : '♀' }}</p>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
                <div class="pagination-wrap" v-if="totalAll > pageSize">
                  <el-pagination
                    :total="totalAll"
                    :page-size="pageSize"
                    v-model:current-page="pageAllNum"
                    layout="prev, pager, next"
                    @current-change="loadAllPets"
                    background
                  />
                </div>
              </el-tab-pane>
              <el-tab-pane label="最新上架" name="latest">
                <el-row :gutter="20">
                  <el-col :span="8" v-for="pet in latestPets" :key="pet.id">
                    <el-card :body-style="{ padding: '0' }" shadow="hover" class="pet-card" @click="$router.push(`/pet/${pet.id}`)">
                      <div class="pet-img" :style="{ backgroundImage: `url(${getFirstImage(pet.images)})` }"></div>
                      <div class="pet-info">
                        <h4>{{ pet.name }}</h4>
                        <p>{{ pet.type }} · {{ pet.breed }} · {{ pet.gender === '公' ? '♂' : '♀' }}</p>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
                <div class="pagination-wrap" v-if="totalLatest > pageSize">
                  <el-pagination
                    :total="totalLatest"
                    :page-size="pageSize"
                    v-model:current-page="pageLatestNum"
                    layout="prev, pager, next"
                    @current-change="loadLatestPets"
                    background
                  />
                </div>
              </el-tab-pane>
            </el-tabs>
          </template>
          <template v-else>
            <div class="hero shelter-hero">
              <h1>{{ userStore.userInfo?.shelterName || '我的救助站' }}</h1>
              <p>管理宠物信息，审核领养申请，帮助流浪动物找到新家</p>
            </div>
            <el-row :gutter="20" style="margin-top:30px">
              <el-col :span="8">
                <el-card class="stat-card" @click="$router.push('/shelter/pets')">
                  <el-statistic title="在架宠物" :value="stats.petCount" />
                  <p class="card-hint">点击管理宠物</p>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card" @click="$router.push('/shelter/adoptions')">
                  <el-statistic title="待审核申请" :value="stats.pendingCount">
                    <template #suffix>
                      <el-tag v-if="stats.pendingCount>0" type="danger" size="small">待处理</el-tag>
                    </template>
                  </el-statistic>
                  <p class="card-hint">点击审核</p>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <el-statistic title="成功领养" :value="stats.adoptedCount" />
                </el-card>
              </el-col>
            </el-row>
            <el-divider />
            <h3>我的宠物</h3>
            <el-table :data="myPets" stripe style="margin-top:16px">
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="type" label="类型" width="80" />
              <el-table-column prop="breed" label="品种" width="100" />
              <el-table-column prop="gender" label="性别" width="60" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status===0?'warning':'info'">{{ row.status===0?'待领养':'已领养' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160">
                <template #default="{ row }">
                  <el-button size="small" @click="$router.push(`/pet/${row.id}`)">查看</el-button>
                  <el-button size="small" type="danger" @click="deletePet(row)">下架</el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </div>
      </div>

    <!-- 下架确认弹窗 -->
    <Teleport to="body">
      <div v-if="delDialogVisible" class="del-overlay" @click.self="delDialogVisible = false">
        <div class="del-dialog">
          <h3>确认下架</h3>
          <p>确定下架宠物「{{ delTarget?.name }}」？</p>
          <div class="del-actions">
            <el-button @click="delDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="doDelete" :loading="delLoading">确认下架</el-button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { petAPI, adoptionAPI, matchingAPI } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isShelter } = storeToRefs(userStore)
const activeTab = ref('recommend')

const pets = ref([])
const latestPets = ref([])
const matchedPets = ref([])
const myPets = ref([])
const hasProfile = ref(false)
const stats = ref({ petCount: 0, pendingCount: 0, adoptedCount: 0 })

// 下架确认弹窗
const delDialogVisible = ref(false)
const delTarget = ref(null)
const delLoading = ref(false)

// Pagination
const pageAllNum = ref(1)
const pageLatestNum = ref(1)
const pageSize = 12
const totalAll = ref(0)
const totalLatest = ref(0)

onMounted(async () => {
  console.log('=== Home onMounted, isShelter:', isShelter.value, 'userId:', userStore.userId)
  if (isShelter.value) {
    console.log('=== entering loadShelterData')
    await loadShelterData()
  } else {
    try {
      const profileRes = await matchingAPI.getProfile()
      const profileData = profileRes?.data
      hasProfile.value = profileData?.matchingProfileComplete || profileData?.petPreference
    } catch (e) {
      ElMessage.error('加载失败，请稍后重试')
    }

    loadAllPets()
    loadLatestPets()

    if (hasProfile.value) {
      try {
        const matchRes = await matchingAPI.recommend()
        matchedPets.value = matchRes?.data || []
      } catch (e) {
        ElMessage.error('加载失败，请稍后重试')
      }
    }
  }
})

watch(activeTab, (tab) => {
  if (tab === 'all') {
    pageAllNum.value = 1
    loadAllPets()
  } else if (tab === 'latest') {
    pageLatestNum.value = 1
    loadLatestPets()
  }
})

async function loadAllPets() {
  try {
    const res = await petAPI.search({ pageNum: pageAllNum.value, pageSize })
    pets.value = res?.data?.records || []
    totalAll.value = res?.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  }
}

async function loadLatestPets() {
  try {
    const res = await petAPI.search({ pageNum: pageLatestNum.value, pageSize, sort: 'latest' })
    latestPets.value = res?.data?.records || []
    totalLatest.value = res?.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  }
}

function getFirstImage(images) {
  if (!images) return '/placeholder.jpg'
  if (Array.isArray(images)) return images[0] || '/placeholder.jpg'
  if (typeof images === 'string') return images.split(',')[0]?.trim() || '/placeholder.jpg'
  return '/placeholder.jpg'
}

function scoreColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

async function loadShelterData() {
  try {
    const [petRes, adpRes] = await Promise.all([
      petAPI.list({ pageNum: 1, pageSize: 100 }),
      adoptionAPI.list()
    ])
    console.log('petRes:', JSON.stringify(petRes))
    console.log('adpRes:', JSON.stringify(adpRes))
    myPets.value = petRes?.data?.records || []
    const adps = adpRes?.data?.records || adpRes?.data || []
    console.log('myPets:', myPets.value.length)
    console.log('adps:', adps.length)
    stats.value = {
      petCount: myPets.value.length,
      pendingCount: adps.filter(a => a.status === 0).length,
      adoptedCount: adps.filter(a => a.status === 3).length
    }
    console.log('stats:', JSON.stringify(stats.value))
  } catch (e) {
    console.error('loadShelterData error:', e)
    ElMessage.error('加载失败，请稍后重试')
  }
}

async function deletePet(pet) {
  delTarget.value = pet
  delDialogVisible.value = true
}

async function doDelete() {
  delLoading.value = true
  try {
    await petAPI.delete(delTarget.value.id)
    ElMessage.success('已下架')
    delDialogVisible.value = false
    loadShelterData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    delLoading.value = false
  }
}

function logout() { userStore.logout(); router.push('/login') }
</script>

<style scoped>
.page-content { padding-top: 60px; }
.hero { text-align:center; padding:60px 0; }
.hero h1 { font-size:36px; color:#e67e22; margin-bottom:12px; }
.hero p { color:#666; margin-bottom:24px; font-size:16px; }
.shelter-hero { background:linear-gradient(135deg,#e8f5e9,#c8e6c9); border-radius:12px; }
.pet-card { cursor:pointer; border-radius:12px; overflow:hidden; }
.pet-img { height:200px; background-size:cover; background-position:center; background-color:#eee; position:relative; }
.pet-info { padding:14px; }
.pet-info h4 { margin-bottom:4px; }
.pet-info p { color:#888; font-size:13px; }
.stat-card { cursor:pointer; text-align:center; padding:20px; transition:transform .2s; }
.stat-card:hover { transform:translateY(-4px); }
.card-hint { color:#aaa; font-size:12px; margin-top:8px; }
.no-recommend { text-align:center; color:#999; padding:40px 20px; }
.no-recommend p { margin-bottom:12px; font-size:14px; }
.match-badge { position:absolute; top:8px; right:8px; }
.match-value { font-size:11px; font-weight:bold; }
.home-tabs { margin-top: 8px; }
.home-tabs :deep(.el-tabs__header) { margin-bottom: 20px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }
</style>
