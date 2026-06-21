<template>
  <div class="reg-page">
    <!-- 背景图层 -->
    <div class="bg-layer"></div>
    <div class="bg-overlay"></div>

    <!-- 浮动装饰 -->
    <div class="floating-paws">
      <span class="paw paw-1">&#x1f43e;</span>
      <span class="paw paw-2">&#x1f43e;</span>
      <span class="paw paw-3">&#x1f436;</span>
      <span class="paw paw-4">&#x1f431;</span>
      <span class="paw paw-5">&#x2764;</span>
      <span class="paw paw-6">&#x1f43e;</span>
    </div>

    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-content">
        <div class="brand-icon">&#x1f3e0;</div>
        <h1 class="brand-title">加入 PawMatch</h1>
        <p class="brand-subtitle">开启一段温暖的领养之旅</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-icon">&#x1f4dd;</span>
            <span>快速注册</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">&#x1f50d;</span>
            <span>智能匹配</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">&#x1f495;</span>
            <span>领养陪伴</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧注册区 -->
    <div class="form-panel">
      <div class="form-card">
        <div class="card-header">
          <h2>创建账号</h2>
          <p>填写信息加入 PawMatch 大家庭</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="0" class="reg-form">
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称" prefix-icon="EditPen" size="large" class="custom-input" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（至少8位）" prefix-icon="Lock" show-password size="large" class="custom-input" />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password size="large" class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="register" :loading="loading" class="reg-btn">注 册</el-button>
          </el-form-item>
        </el-form>

        <div class="links">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>

    <!-- 注册成功弹窗 -->
    <Teleport to="body">
      <div v-if="successVisible" class="del-overlay">
        <div class="del-dialog" style="min-width:380px;text-align:center">
          <h3>注册成功</h3>
          <p style="margin:16px 0 4px">您的账号为：<strong>{{ successAccount }}</strong></p>
          <p style="color:#909399;font-size:13px;margin:0 0 20px">请牢记账号，用于后续登录。</p>
          <div class="del-actions" style="justify-content:center">
            <el-button type="primary" @click="goLogin">去登录</el-button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ password: '', confirmPassword: '', nickname: '' })
const rules = {
  password: [{ required: true, min: 8, message: '密码至少8位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, cb) => value === form.password ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const successVisible = ref(false)
const successAccount = ref('')

async function goLogin() {
  successVisible.value = false
  router.push({ path: '/login', query: { account: successAccount.value, userType: 0 } })
}

async function register() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authAPI.registerUser(form)
    const account = res?.data?.account || ''
    successAccount.value = account
    successVisible.value = true
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.reg-page {
  display: flex;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ===== 背景图层 ===== */
.bg-layer {
  position: absolute;
  inset: 0;
  background:
    url('https://images.unsplash.com/photo-1544568100-847a948585b9?w=1920&q=80') center/cover no-repeat,
    linear-gradient(135deg, #f5a623 0%, #e67e22 40%, #d35400 100%);
  background-blend-mode: overlay;
  filter: brightness(0.4) saturate(0.85);
  z-index: 0;
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(245,166,35,0.3) 0%, rgba(211,84,0,0.4) 100%);
  z-index: 1;
}

/* ===== 浮动装饰 ===== */
.floating-paws {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  overflow: hidden;
}

.paw {
  position: absolute;
  font-size: 32px;
  opacity: 0.12;
  animation: floatPaw 8s ease-in-out infinite;
}

.paw-1 { top: 8%; left: 6%; animation-delay: 0s; font-size: 26px; }
.paw-2 { top: 78%; left: 10%; animation-delay: 1.5s; font-size: 34px; }
.paw-3 { top: 22%; left: 58%; animation-delay: 3s; font-size: 28px; }
.paw-4 { top: 82%; left: 45%; animation-delay: 4.5s; font-size: 32px; }
.paw-5 { top: 12%; left: 75%; animation-delay: 6s; font-size: 24px; }
.paw-6 { top: 68%; left: 80%; animation-delay: 2s; font-size: 30px; }

@keyframes floatPaw {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
  padding: 60px;
}

.brand-content {
  text-align: center;
  color: #fff;
  max-width: 420px;
}

.brand-icon {
  font-size: 72px;
  margin-bottom: 16px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.3));
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  letter-spacing: 2px;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.brand-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 32px 0;
  font-weight: 300;
  letter-spacing: 2px;
}

.brand-features {
  display: flex;
  justify-content: center;
  gap: 28px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  opacity: 0.85;
}

.feature-icon {
  font-size: 24px;
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
  max-width: 400px;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 48px 40px 40px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.25), 0 0 0 1px rgba(255,255,255,0.1);
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.card-header p {
  font-size: 14px;
  color: #95a5a6;
  margin: 0;
}

.reg-form {
  margin-top: 0;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: #f8f9fa;
  box-shadow: none;
  transition: all 0.3s;
  padding: 2px 12px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  background: #f0f1f3;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(230,126,34,0.2);
}

.reg-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #f5a623, #e67e22);
  border: none;
  transition: all 0.3s;
  margin-top: 8px;
}

.reg-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(230,126,34,0.35);
  background: linear-gradient(135deg, #f7b733, #e67e22);
}

.links {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #95a5a6;
}

.links a {
  color: #e67e22;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.3s;
}

.links a:hover {
  color: #d35400;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .brand-panel { display: none; }
  .form-panel { width: 100%; }
  .form-card { max-width: 380px; padding: 36px 28px 30px; }
}
</style>
（内容由AI生成，仅供参考）
