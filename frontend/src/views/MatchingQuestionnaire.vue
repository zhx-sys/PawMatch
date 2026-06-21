<template>
  <div class="page-content">
    <div class="container">
      <div class="questionnaire-card">
        <h1>匹配画像问卷</h1>
        <p class="subtitle">完善以下信息，我们将为你智能匹配最合适的毛孩子</p>

        <el-form ref="formRef" :model="form" label-position="top" class="q-form">
          <el-form-item label="你的居住空间" required>
            <el-radio-group v-model="form.livingSpace">
              <el-radio-button value="公寓">公寓</el-radio-button>
              <el-radio-button value="普通住宅">普通住宅</el-radio-button>
              <el-radio-button value="大户型">大户型</el-radio-button>
              <el-radio-button value="别墅">别墅</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="家中是否有儿童">
            <el-radio-group v-model="form.hasChildren">
              <el-radio-button :value="false">无</el-radio-button>
              <el-radio-button :value="true">有</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="是否已有其他宠物">
            <el-radio-group v-model="form.hasOtherPets">
              <el-radio-button :value="false">无</el-radio-button>
              <el-radio-button :value="true">有</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="养宠经验">
            <el-radio-group v-model="form.petExperience">
              <el-radio-button value="新手">新手</el-radio-button>
              <el-radio-button value="有经验">有经验</el-radio-button>
              <el-radio-button value="资深">资深</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="作息规律">
            <el-radio-group v-model="form.dailyRoutine">
              <el-radio-button value="朝九晚五">朝九晚五</el-radio-button>
              <el-radio-button value="自由职业">自由职业</el-radio-button>
              <el-radio-button value="居家">居家</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="月度预算">
            <el-radio-group v-model="form.budgetRange">
              <el-radio-button value="低（<300/月）">低（<300/月）</el-radio-button>
              <el-radio-button value="中（300-800/月）">中（300-800/月）</el-radio-button>
              <el-radio-button value="高（>800/月）">高（>800/月）</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="宠物偏好（可选）">
            <el-input
              v-model="form.petPreference"
              type="textarea"
              :rows="2"
              placeholder="例如：猫 金毛 小型犬 柯基（多个关键词用空格或逗号分隔）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" size="large" @click="submitForm" :loading="saving">
              保存并查看推荐
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { matchingAPI } from '../api'

const router = useRouter()
const saving = ref(false)

const form = ref({
  livingSpace: '普通住宅',
  hasChildren: false,
  hasOtherPets: false,
  petExperience: '新手',
  dailyRoutine: '朝九晚五',
  budgetRange: '中（300-800/月）',
  petPreference: ''
})

onMounted(async () => {
  try {
    const res = await matchingAPI.getProfile()
    if (res?.data) {
      const d = res.data
      if (d.livingSpace) form.value.livingSpace = d.livingSpace
      if (d.hasChildren !== null) form.value.hasChildren = d.hasChildren
      if (d.hasOtherPets !== null) form.value.hasOtherPets = d.hasOtherPets
      if (d.petExperience) form.value.petExperience = d.petExperience
      if (d.dailyRoutine) form.value.dailyRoutine = d.dailyRoutine
      if (d.budgetRange) form.value.budgetRange = d.budgetRange
      if (d.petPreference) form.value.petPreference = d.petPreference
    }
  } catch (e) {}
})

async function submitForm() {
  saving.value = true
  try {
    await matchingAPI.saveProfile(form.value)
    ElMessage.success('画像保存成功')
    router.push('/home')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page-content { padding-top: 60px; }
.container { max-width: 640px; margin: 0 auto; padding: 0 20px; }
.questionnaire-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 2px 20px rgba(0,0,0,0.06);
}
.questionnaire-card h1 {
  font-size: 26px;
  color: #3d322b;
  margin-bottom: 8px;
}
.subtitle {
  color: #999;
  font-size: 14px;
  margin-bottom: 32px;
}
.q-form .el-form-item {
  margin-bottom: 24px;
}
.form-actions {
  text-align: center;
  padding-top: 16px;
}
</style>