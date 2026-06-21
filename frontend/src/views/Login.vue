<template>
  <div class="login-page">
    <!-- 左侧轮播区 -->
    <div class="brand-panel">
      <!-- 宠物图片轮播 -->
      <div class="carousel-container">
        <div
          v-for="(pet, i) in carouselPets"
          :key="pet.id"
          class="carousel-slide"
          :class="{ active: i === currentSlide }"
        >
          <img :src="pet.images" :alt="pet.name" class="carousel-img" />
          <div class="carousel-pet-info">
            <span class="carousel-pet-name">{{ pet.name }}</span>
            <span class="carousel-pet-breed">{{ pet.breed }}</span>
          </div>
        </div>
      </div>
      <!-- 品牌信息叠加层 -->
      <div class="brand-overlay">
        <div class="brand-icon">&#x1f43e;</div>
        <h1 class="brand-title">PawMatch</h1>
        <p class="brand-subtitle">让每一个生命都能找到温暖的家</p>
        <!-- 轮播指示器 -->
        <div class="carousel-indicators">
          <span
            v-for="(pet, i) in carouselPets"
            :key="'dot-' + pet.id"
            class="carousel-dot"
            :class="{ active: i === currentSlide }"
            @click="currentSlide = i"
          ></span>
        </div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="form-panel">
      <div class="form-card">
        <div class="card-header">
          <div class="header-icon">&#x1f43e;</div>
          <h2>欢迎回来</h2>
          <p>登录你的 PawMatch 账号</p>
        </div>

        <el-radio-group v-model="userType" class="type-switch">
          <el-radio-button :value="0">普通用户</el-radio-button>
          <el-radio-button :value="1">救助站</el-radio-button>
        </el-radio-group>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="0" class="login-form">
          <el-form-item prop="account">
            <el-input v-model="form.account" placeholder="账号" prefix-icon="User" size="large" class="custom-input" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password size="large" class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="login" :loading="loading" class="login-btn">登 录</el-button>
          </el-form-item>
        </el-form>

        <div class="links">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI, petAPI } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const userType = ref(0)
const form = reactive({ account: '', password: '' })
const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 轮播相关
const carouselPets = ref([])
const currentSlide = ref(0)
let carouselTimer = null

onMounted(() => {
  if (route.query.account) {
    form.account = route.query.account
    userType.value = Number(route.query.userType) || 0
  }
  fetchCarouselPets()
})

onBeforeUnmount(() => {
  clearInterval(carouselTimer)
})

async function fetchCarouselPets() {
  try {
    const res = await petAPI.carousel()
    carouselPets.value = res.data || []
    if (carouselPets.value.length > 0) {
      carouselTimer = setInterval(() => {
        currentSlide.value = (currentSlide.value + 1) % carouselPets.value.length
      }, 4000)
    }
  } catch { /* 轮播加载失败不影响登录 */ }
}

async function login() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authAPI.login({ ...form, userType: userType.value })
    sessionStorage.setItem('token', res.data.token)
    sessionStorage.setItem('userInfo', JSON.stringify(res.data))
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.login-page {
  display: flex;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ===== 左侧品牌区（含轮播） ===== */
.brand-panel {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
  overflow: hidden;
  background: #1a1a2e;
}

/* 轮播容器 */
.carousel-container {
  position: absolute;
  inset: 0;
}

.carousel-slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 1s ease-in-out;
}

.carousel-slide.active {
  opacity: 1;
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-pet-info {
  position: absolute;
  bottom: 24%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  padding: 6px 18px;
  border-radius: 20px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.carousel-pet-name {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

.carousel-pet-breed {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

/* 品牌叠加层 */
.brand-overlay {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  max-width: 420px;
  padding: 0 40px;
  /* 底部渐变遮罩提升文字可读性 */
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
}

/* 全屏暗色遮罩 */
.brand-panel::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0.2) 50%, rgba(0,0,0,0.45) 100%);
  z-index: 1;
  pointer-events: none;
}

.brand-icon {
  font-size: 72px;
  margin-bottom: 16px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.3));
}

.brand-title {
  font-size: 48px;
  font-weight: 800;
  letter-spacing: 2px;
  margin: 0 0 12px 0;
}

.brand-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 32px 0;
  font-weight: 300;
  letter-spacing: 2px;
}

/* 轮播指示器 */
.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 28px;
}

.carousel-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s;
}

.carousel-dot.active {
  background: #fff;
  width: 24px;
  border-radius: 4px;
}

/* ===== 右侧表单区 ===== */
.form-panel {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
  padding: 40px;
}

.form-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 24px;
  padding: 48px 44px 44px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.06),
    0 12px 48px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.5);
  animation: cardSlideIn 0.7s cubic-bezier(0.22, 0.61, 0.36, 1);
}

@keyframes cardSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon {
  font-size: 40px;
  margin-bottom: 8px;
  animation: pawBounce 2s ease-in-out infinite;
}

@keyframes pawBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.card-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
}

.card-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
  font-weight: 400;
}

/* 用户类型切换 */
.type-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 4px;
}

.type-switch :deep(.el-radio-button) {
  flex: 1;
}

.type-switch :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 10px 0;
  border-radius: 10px;
  font-weight: 500;
  font-size: 14px;
  border: none;
  background: transparent;
  color: #909399;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.type-switch :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #fff;
  color: #e67e22;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.login-form {
  margin-top: 0;
}

/* 输入框 */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #f5f6f8;
  box-shadow: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 4px 16px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  background: #eef0f4;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow:
    0 0 0 2px rgba(230, 126, 34, 0.15),
    0 2px 8px rgba(230, 126, 34, 0.06);
}

.custom-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #2c3e50;
}

.custom-input :deep(.el-input__prefix-inner) {
  color: #c0c4cc;
  transition: color 0.3s;
}

.custom-input :deep(.el-input__wrapper.is-focus .el-input__prefix-inner) {
  color: #e67e22;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 50px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #f5a623 0%, #e67e22 100%);
  border: none;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: 12px;
  box-shadow: 0 4px 16px rgba(230, 126, 34, 0.25);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(230, 126, 34, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn.is-loading {
  background: linear-gradient(135deg, #f8c56a 0%, #e67e22 100%);
}

/* 注册链接 */
.links {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #909399;
}

.links a {
  color: #e67e22;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
  margin-left: 2px;
}

.links a:hover {
  color: #d35400;
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .brand-panel { display: none; }
  .form-panel { width: 100%; }
  .form-card { max-width: 380px; padding: 40px 32px 36px; }
}
</style>
（内容由AI生成，仅供参考）
