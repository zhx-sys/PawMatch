<template>
  <div class="page-content">
    <div class="container">
      <h1 class="page-title">我的成长</h1>

      <!-- 积分 & 等级卡片 -->
      <el-card class="card">
        <div class="level-section">
          <div class="level-info">
            <div class="level-badge">{{ level.level }}</div>
            <div>
              <h2>Lv{{ level.level }} {{ level.levelName }}</h2>
              <p>当前积分：{{ level.currentPoints }}</p>
            </div>
          </div>
          <div class="progress-wrap">
            <el-progress
              :percentage="levelProgress"
              :stroke-width="16"
              :color="'#e67e22'"
            />
            <p class="progress-text">
              {{ level.currentPoints }} / {{ level.nextLevelPoints || '--' }} 积分
            </p>
          </div>
        </div>
        <el-button type="primary" :disabled="checkedIn" @click="doCheckin" style="margin-top:16px">
          {{ checkedIn ? '今日已签到' : '每日签到 +5' }}
        </el-button>
      </el-card>

      <!-- 徽章墙 -->
      <h3 style="margin-top:24px">徽章墙</h3>
      <div class="badges-grid">
        <div
          v-for="badge in allBadges"
          :key="badge.id"
          class="badge-item"
          :class="{ owned: ownedBadgeIds.has(badge.id) }"
        >
          <div class="badge-icon">{{ badge.icon || '🏅' }}</div>
          <div class="badge-name">{{ badge.name }}</div>
          <div class="badge-desc">{{ badge.description }}</div>
        </div>
        <div v-if="allBadges.length === 0" class="empty">暂无徽章</div>
      </div>

      <!-- 积分流水 -->
      <h3 style="margin-top:24px">积分流水</h3>
      <el-table :data="pointsLog" stripe style="width:100%">
        <el-table-column prop="action" label="行为" width="180" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="积分" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.points >= 0 ? '#67c23a' : '#f56c6c' }">
              {{ row.points >= 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      <el-pagination
        v-if="logTotal > 20"
        v-model:current-page="logPage"
        :page-size="20"
        :total="logTotal"
        layout="prev, pager, next"
        @current-change="loadPointsLog"
        style="margin-top:12px;justify-content:center"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { growthAPI } from '@/api'
import { ElMessage } from 'element-plus'

const level = ref({ level: 1, levelName: '萌新', currentPoints: 0, nextLevelPoints: 99 })
const allBadges = ref([])
const ownedBadgeIds = ref(new Set())
const pointsLog = ref([])
const logPage = ref(1)
const logTotal = ref(0)
const checkedIn = ref(false)

const levelProgress = computed(() => {
  const l = level.value
  if (!l.nextLevelPoints || l.nextLevelPoints === 0) return 100
  // 计算在当前等级中的进度
  const levelBase = getLevelBase(l.level)
  const range = l.nextLevelPoints - levelBase
  if (range <= 0) return 100
  return Math.min(100, Math.round(((l.currentPoints - levelBase) / range) * 100))
})

function getLevelBase(lv) {
  const bases = [0, 0, 100, 300, 700, 1500]
  return bases[lv] || 0
}

onMounted(() => {
  loadMyPoints()
  loadAllBadges()
  loadPointsLog()
})

async function loadMyPoints() {
  try {
    const res = await growthAPI.myPoints()
    level.value = res?.data || level.value
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  }
}

async function loadAllBadges() {
  try {
    const [allRes, myRes] = await Promise.all([
      growthAPI.allBadges(),
      growthAPI.myBadges()
    ])
    allBadges.value = allRes?.data || []
    const myBadges = myRes?.data || []
    ownedBadgeIds.value = new Set(myBadges.map(b => b.id))
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  }
}

async function loadPointsLog() {
  try {
    const res = await growthAPI.pointsLog({ pageNum: logPage.value, pageSize: 20 })
    pointsLog.value = res?.data?.records || []
    logTotal.value = res?.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  }
}

async function doCheckin() {
  try {
    const res = await growthAPI.checkin()
    if (res?.code === 200) {
      ElMessage.success('签到成功 +5积分')
      checkedIn.value = true
      loadMyPoints()
    } else {
      ElMessage.warning(res?.message || '今日已签到')
      checkedIn.value = true
    }
  } catch (e) {
    // 409 or duplicate: already checked in
    checkedIn.value = true
    ElMessage.warning('今日已签到')
  }
}
</script>

<style scoped>
.page-content { padding-top: 60px; min-height: 100vh; background: #f7f4f0; }
.container { max-width: 900px; margin: 0 auto; padding: 20px; }
.page-title { font-size: 24px; margin: 0 0 20px; color: #333; }
.card { border-radius: 12px; }
.level-section { display: flex; flex-direction: column; gap: 16px; }
.level-info { display: flex; align-items: center; gap: 16px; }
.level-badge {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, #e67e22, #f39c12);
  color: #fff; font-size: 24px; font-weight: bold;
  display: flex; align-items: center; justify-content: center;
}
.level-info h2 { margin: 0; color: #333; }
.level-info p { color: #888; margin: 4px 0 0; }
.progress-wrap { flex: 1; }
.progress-text { color: #aaa; font-size: 13px; margin: 4px 0 0; text-align: right; }

.badges-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 12px; }
.badge-item {
  background: #fff; border-radius: 12px; padding: 16px; text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,.04); opacity: 0.35; transition: opacity .3s;
}
.badge-item.owned { opacity: 1; background: linear-gradient(135deg, #fff9eb, #fff); border: 2px solid #f5d778; }
.badge-icon { font-size: 32px; margin-bottom: 4px; }
.badge-name { font-weight: 500; color: #333; font-size: 14px; }
.badge-desc { color: #aaa; font-size: 12px; margin-top: 4px; }
.empty { text-align: center; padding: 60px 0; color: #bbb; font-size: 16px; grid-column: 1 / -1; }
</style>