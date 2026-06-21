<template>
  <div v-if="$route.path === '/login' || $route.path === '/register'">
    <router-view />
  </div>
  <div v-else class="app-layout">
    <SideDecorations />
    <NavBar />
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition :name="route.meta.transition || 'fade-slide'" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
import SideDecorations from './components/SideDecorations.vue'
</script>

<style>
*,
*::before,
*::after {
  box-sizing: border-box;
}

body {
  margin: 0;
  background: #f7f3ef;
  color: #3d322b;
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app-layout {
  position: relative;
  min-height: 100vh;
}

.main-content {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 80px;
}

@media (max-width: 1400px) {
  .main-content {
    max-width: 100%;
    padding: 0 16px 80px;
  }
}

.el-overlay {
  z-index: 3000 !important;
}

.el-message-box {
  z-index: 3001 !important;
}

.el-dialog__wrapper {
  z-index: 3010 !important;
}

.el-overlay-dialog {
  z-index: 3009 !important;
}

/* 下架确认弹窗 */
.del-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.del-dialog {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px 24px;
  min-width: 360px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18);
}

.del-dialog h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #1a1a2e;
}

.del-dialog p {
  margin: 0 0 20px;
  font-size: 14px;
  color: #606266;
}

.del-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ========== 路由过渡动画 ========== */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ========== 全局微交互 ========== */
.el-button {
  transition: transform 0.15s ease, box-shadow 0.15s ease !important;
}
.el-button:not(.el-button--text):not(.el-button--link):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.el-button:not(.el-button--text):not(.el-button--link):active {
  transform: translateY(0);
}

.el-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.el-card[shadow="hover"]:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1) !important;
}

/* ========== 滚动条美化 ========== */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #d4c5b9;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #b8a394;
}

/* ========== 全局平滑滚动 ========== */
html {
  scroll-behavior: smooth;
}
</style>
