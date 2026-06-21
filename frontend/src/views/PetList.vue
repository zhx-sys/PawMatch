<template>
  <div class="pet-list-page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <span class="filter-label">种类：</span>
        <div class="filter-options">
          <button :class="['filter-btn', { active: !filters.type }]" @click="setFilter('type', '')">全部</button>
          <button :class="['filter-btn', { active: filters.type === '狗' }]" @click="setFilter('type', '狗')">狗狗</button>
          <button :class="['filter-btn', { active: filters.type === '猫' }]" @click="setFilter('type', '猫')">猫咪</button>
          <button :class="['filter-btn', { active: filters.type === '其他' }]" @click="setFilter('type', '其他')">其他</button>
        </div>
        <span class="filter-label">年龄：</span>
        <div class="filter-options">
          <button :class="['filter-btn', { active: !filters.ageRange }]" @click="setFilter('ageRange', '')">全部</button>
          <button :class="['filter-btn', { active: filters.ageRange === 'baby' }]" @click="setFilter('ageRange', 'baby')">幼年期(&lt;1岁)</button>
          <button :class="['filter-btn', { active: filters.ageRange === 'young' }]" @click="setFilter('ageRange', 'young')">青年期(1-3岁)</button>
          <button :class="['filter-btn', { active: filters.ageRange === 'adult' }]" @click="setFilter('ageRange', 'adult')">成年期(3-7岁)</button>
          <button :class="['filter-btn', { active: filters.ageRange === 'senior' }]" @click="setFilter('ageRange', 'senior')">老年期(7岁+)</button>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">体型：</span>
        <div class="filter-options">
          <button :class="['filter-btn', { active: !filters.sizeLevel }]" @click="setFilter('sizeLevel', '')">全部</button>
          <button :class="['filter-btn', { active: filters.sizeLevel === '小型' }]" @click="setFilter('sizeLevel', '小型')">小型</button>
          <button :class="['filter-btn', { active: filters.sizeLevel === '中型' }]" @click="setFilter('sizeLevel', '中型')">中型</button>
          <button :class="['filter-btn', { active: filters.sizeLevel === '大型' }]" @click="setFilter('sizeLevel', '大型')">大型</button>
        </div>
        <span class="filter-label">性格：</span>
        <div class="filter-options">
          <button :class="['filter-btn', { active: !filters.personality }]" @click="setFilter('personality', '')">全部</button>
          <button :class="['filter-btn', { active: filters.personality === 'active' }]" @click="setFilter('personality', 'active')">活泼好动</button>
          <button :class="['filter-btn', { active: filters.personality === 'calm' }]" @click="setFilter('personality', 'calm')">温顺安静</button>
          <button :class="['filter-btn', { active: filters.personality === 'clingy' }]" @click="setFilter('personality', 'clingy')">粘人精</button>
          <button :class="['filter-btn', { active: filters.personality === 'independent' }]" @click="setFilter('personality', 'independent')">独立自主</button>
        </div>
        <div class="search-box">
          <el-input v-model="filters.keyword" placeholder="搜索名字或品种..." :prefix-icon="Search" @keyup.enter="search" clearable @clear="search" />
        </div>
      </div>
    </div>

    <!-- 宠物卡片网格 -->
    <div class="pet-grid" v-if="pets.length">
      <div class="pet-card" v-for="pet in pets" :key="pet.id" @click="$router.push(`/pet/${pet.id}`)">
        <div class="card-img">
          <img :src="getFirstImage(pet.images)" :alt="pet.name" />
          <button class="fav-btn" :class="{ favorited: favoriteIds.includes(pet.id) }" @click.stop="toggleFavorite(pet)">
            <svg viewBox="0 0 24 24" width="20" height="20" :fill="favoriteIds.includes(pet.id) ? '#ff4757' : 'none'" :stroke="favoriteIds.includes(pet.id) ? '#ff4757' : '#ccc'" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
          </button>
        </div>
        <div class="card-body">
          <h4 class="pet-name">{{ pet.name }}</h4>
          <div class="pet-tags">
            <span class="tag tag-age">{{ pet.age }}岁</span>
            <span class="tag tag-personality">{{ personalityLabel(pet.activityLevel) }}</span>
          </div>
          <div class="health-status">
            <span class="dot" :class="pet.healthStatus === '健康' ? 'dot-green' : pet.healthStatus === '亚健康' ? 'dot-yellow' : pet.healthStatus === '生病' ? 'dot-red' : 'dot-yellow'"></span>
            <span class="health-text">{{ pet.healthStatus || '未知' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!pets.length && !loading" description="还没有找到合适的宠物" />

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        :total="total"
        :page-size="pageSize"
        v-model:current-page="currentPage"
        layout="prev, pager, next"
        @current-change="loadPets"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { petAPI, petFavoriteAPI } from '../api'

const router = useRouter()
const user = computed(() => JSON.parse(sessionStorage.getItem('userInfo') || '{}'))

const pets = ref([])
const currentPage = ref(1)
const pageSize = 12
const total = ref(0)
const loading = ref(false)
const favoriteIds = ref([])

const filters = ref({
  type: '',
  ageRange: '',
  sizeLevel: '',
  personality: '',
  keyword: ''
})

function getFirstImage(images) {
  if (!images) return '/placeholder.jpg'
  if (Array.isArray(images)) return images[0] || '/placeholder.jpg'
  if (typeof images === 'string') return images.split(',')[0]?.trim() || '/placeholder.jpg'
  return '/placeholder.jpg'
}

function personalityLabel(level) {
  const map = { '活泼好动': '活泼好动', '温顺安静': '温顺安静', '粘人精': '粘人精', '独立自主': '独立自主',
                 '高': '活泼好动', '低': '温顺安静' }
  return map[level] || '温顺安静'
}

function setFilter(key, value) {
  filters.value[key] = value
  search()
}

onMounted(() => {
  loadPets()
  loadFavoriteIds()
})

async function loadFavoriteIds() {
  try {
    const res = await petFavoriteAPI.ids(user.value.userId)
    favoriteIds.value = res?.data || []
  } catch (e) {}
}

async function toggleFavorite(pet) {
  try {
    await petFavoriteAPI.toggle(user.value.userId, pet.id)
    const idx = favoriteIds.value.indexOf(pet.id)
    if (idx > -1) {
      favoriteIds.value.splice(idx, 1)
    } else {
      favoriteIds.value.push(pet.id)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function buildSearchParams() {
  const params = {
    pageNum: currentPage.value,
    pageSize,
    keyword: filters.value.keyword || undefined,
    type: filters.value.type || undefined,
    sizeLevel: filters.value.sizeLevel || undefined
  }

  // 年龄范围映射
  switch (filters.value.ageRange) {
    case 'baby': params.maxAge = 0; break
    case 'young': params.minAge = 1; params.maxAge = 3; break
    case 'adult': params.minAge = 3; params.maxAge = 7; break
    case 'senior': params.minAge = 7; break
  }

  // 性格映射
  switch (filters.value.personality) {
    case 'active': params.activityLevel = '活泼好动'; break
    case 'calm': params.activityLevel = '温顺安静'; break
    case 'clingy': params.activityLevel = '粘人精'; break
    case 'independent': params.activityLevel = '独立自主'; break
  }

  return params
}

async function loadPets() {
  loading.value = true
  try {
    const res = await petAPI.search(buildSearchParams())
    pets.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function search() {
  currentPage.value = 1
  loadPets()
}
</script>

<style scoped>
.pet-list-page {
  min-height: 100vh;
  background: #FDF8F0;
  padding: 24px 40px 40px;
  padding-top: 80px;
}

/* 筛选栏 */
.filter-bar {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  min-width: 48px;
}

.filter-options {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  background: #f5f0e8;
  color: #666;
  transition: all 0.2s;
  white-space: nowrap;
}
.filter-btn:hover {
  background: #ffe0cc;
  color: #e67e22;
}
.filter-btn.active {
  background: #f5a623;
  color: #fff;
}

.search-box {
  margin-left: auto;
  width: 240px;
  flex-shrink: 0;
}

/* 卡片网格 */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.pet-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}
.pet-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 24px rgba(0,0,0,0.1);
}

.card-img {
  position: relative;
  aspect-ratio: 4/5;
  overflow: hidden;
  background: #f0ebe0;
}
.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.fav-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.85);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
  backdrop-filter: blur(4px);
}
.fav-btn:hover {
  transform: scale(1.1);
}
.fav-btn.favorited {
  background: rgba(255,255,255,0.95);
}

.card-body {
  padding: 14px;
}

.pet-name {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}

.pet-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.tag {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.tag-age {
  background: #ffe0e6;
  color: #e8496c;
}
.tag-personality {
  background: #fff3e0;
  color: #e67e22;
}

.health-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.dot-green {
  background: #4CAF50;
}
.dot-yellow {
  background: #FFC107;
}

.health-text {
  font-size: 13px;
  color: #888;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .pet-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 900px) {
  .pet-grid { grid-template-columns: repeat(2, 1fr); }
  .pet-list-page { padding: 16px 16px 32px; padding-top: 70px; }
  .search-box { width: 100%; margin-left: 0; }
}
@media (max-width: 560px) {
  .pet-grid { grid-template-columns: 1fr; }
}
</style>
