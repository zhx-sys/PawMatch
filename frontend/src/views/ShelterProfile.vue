<template>
  <div class="page-content">
    <div class="container">
      <!-- 顶部：救助站名片 -->
      <div class="hero">
        <el-avatar :size="72" class="hero-avatar">
          {{ shelterInfo?.nickname?.charAt(0) || 'S' }}
        </el-avatar>
        <div class="hero-info">
          <h1>{{ shelterInfo?.nickname }}</h1>
          <div class="hero-tags">
            <el-tag type="warning" size="large">救助站</el-tag>
            <el-tag v-if="stats.successRate >= 0.8" type="success">高成功率</el-tag>
            <el-tag v-if="stats.totalAdopted >= 10" type="" effect="plain">经验丰富</el-tag>
          </div>
        </div>
      </div>

      <!-- 数据卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-num">{{ stats.totalAdopted }}</div>
          <div class="stat-label">成功领养</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">{{ stats.currentPets }}</div>
          <div class="stat-label">在住宠物</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">{{ (stats.successRate * 100).toFixed(1) }}%</div>
          <div class="stat-label">成功率</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">{{ stats.avgResponseHours }}h</div>
          <div class="stat-label">平均响应</div>
        </div>
      </div>

      <!-- 领养故事 -->
      <h3 style="margin-top:28px" v-if="stories.length">领养故事</h3>
      <div v-if="stories.length" class="stories-row">
        <el-card v-for="story in stories" :key="story.id" class="story-card" @click="$router.push('/community/post/' + story.id)">
          <h4>{{ story.title }}</h4>
          <div class="story-meta">
            浏览 {{ story.viewCount }} · {{ story.createTime }}
          </div>
        </el-card>
      </div>

      <!-- 排行榜回链 -->
      <div style="margin-top:20px">
        <el-button text @click="$router.push('/shelter/ranking')">查看救助站排行榜</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { shelterProfileAPI } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const shelterInfo = ref({})
const stats = ref({ totalAdopted: 0, currentPets: 0, successRate: 0, avgResponseHours: 0 })
const stories = ref([])

onMounted(async () => {
  try {
    const res = await shelterProfileAPI.profile(route.params.id)
    shelterInfo.value = res?.data?.shelterInfo || {}
    stats.value = res?.data?.stats || {}
    stories.value = res?.data?.recentStories || []
  } catch (e) {
    ElMessage.error('救助站信息加载失败')
  }
})
</script>

<style scoped>
.page-content { padding-top: 60px; min-height: 100vh; background: #f7f4f0; }
.container { max-width: 880px; margin: 0 auto; padding: 20px; }
.hero {
  display: flex; align-items: center; gap: 20px;
  background: #fff; border-radius: 16px; padding: 28px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04); margin-bottom: 20px;
}
.hero-avatar { background: linear-gradient(135deg, #e67e22, #f39c12); color: #fff; }
.hero-info h1 { margin: 0 0 8px; font-size: 24px; color: #333; }
.hero-tags { display: flex; gap: 8px; }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 20px; text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
}
.stat-num { font-size: 28px; font-weight: bold; color: #e67e22; }
.stat-label { color: #888; margin-top: 4px; font-size: 14px; }

.stories-row { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 8px; }
.story-card { min-width: 200px; cursor: pointer; }
.story-card h4 { margin: 0; font-size: 15px; color: #333; }
.story-meta { color: #bbb; font-size: 12px; margin-top: 6px; }
</style>