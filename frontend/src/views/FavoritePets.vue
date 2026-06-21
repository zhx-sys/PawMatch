<template>
  <div class="page-content">
    <div class="favorites-page">
      <el-card>
        <template #header>我的收藏</template>
        <div v-if="pets.length === 0" class="empty">暂无收藏</div>
        <div v-else class="pet-grid">
          <div v-for="pet in pets" :key="pet.id" class="pet-card" @click="$router.push(`/pet/${pet.id}`)">
            <el-image :src="pet.images || '/placeholder.png'" fit="cover" class="pet-img" />
            <div class="pet-info">
              <div class="pet-name">{{ pet.name }}</div>
              <div class="pet-breed">{{ pet.breed }}</div>
              <el-button type="danger" size="small" circle icon="el-icon-star-off" @click.stop="unfavorite(pet.id)" />
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { petFavoriteAPI } from '@/api'

export default {
  name: 'FavoritePets',
  data() {
    return { pets: [] }
  },
  created() {
    this.fetchList()
  },
  methods: {
    async fetchList() {
      const user = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
      const res = await petFavoriteAPI.list(user.userId)
      this.pets = res.data || []
    },
    async unfavorite(petId) {
      const user = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
      await petFavoriteAPI.toggle(user.userId, petId)
      this.pets = this.pets.filter(p => p.id !== petId)
    }
  }
}
</script>

<style scoped>
.favorites-page { max-width: 900px; margin: 0 auto; }
.empty { text-align: center; color: #999; padding: 40px; }
.pet-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
.pet-card { border: 1px solid #eee; border-radius: 8px; overflow: hidden; cursor: pointer; }
.pet-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.pet-img { width: 100%; height: 160px; }
.pet-info { padding: 10px; display: flex; justify-content: space-between; align-items: center; }
.pet-name { font-weight: bold; }
.pet-breed { color: #999; font-size: 13px; }
</style>
