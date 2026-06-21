<template>
  <div class="page-content">
<div>
        <div class="game-container">
          <div class="game-title">
            <h2>领养乐园</h2>
            <p>点击小动物查看信息，给它们一个家</p>
          </div>
          <canvas ref="canvasRef" @click="handleCanvasClick" @mousemove="handleMouseMove"></canvas>
        </div>
      </div>
    <!-- 宠物详情弹窗 -->
    <el-dialog v-model="showDetail" :title="selectedPet?.name" width="500px" top="2vh" class="game-dialog">
      <div v-if="selectedPet" class="detail-content">
        <div class="detail-avatar" v-if="getFirstImg(selectedPet.images)">
          <img :src="getFirstImg(selectedPet.images)" class="detail-photo" />
        </div>
        <div class="detail-avatar" v-else>
          <canvas ref="detailCanvasRef" width="80" height="80"></canvas>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型">{{ selectedPet.type }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ selectedPet.breed }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedPet.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ selectedPet.age }}岁</el-descriptions-item>
          <el-descriptions-item label="颜色">{{ selectedPet.color }}</el-descriptions-item>
          <el-descriptions-item label="体重">{{ selectedPet.weight }}kg</el-descriptions-item>
          <el-descriptions-item label="健康">{{ selectedPet.healthStatus }}</el-descriptions-item>
          <el-descriptions-item label="疫苗">{{ selectedPet.vaccinated ? '已接种' : '未接种' }}</el-descriptions-item>
        </el-descriptions>
        <p class="desc">{{ selectedPet.description }}</p>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button type="primary" @click="showDetail = false; showApply = true">申请领养</el-button>
      </template>
    </el-dialog>

    <!-- 领养申请弹窗 -->
    <el-dialog v-model="showApply" title="申请领养" width="450px" top="2vh" class="game-dialog">
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="申请理由">
          <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="请简述您想领养这只宠物的原因" />
        </el-form-item>
        <el-form-item label="养宠经验">
          <el-input v-model="applyForm.experience" type="textarea" :rows="3" placeholder="请描述您过往的养宠经验" />
        </el-form-item>
        <el-form-item label="住房条件">
          <el-input v-model="applyForm.housingCondition" type="textarea" :rows="3" placeholder="请描述您的住房条件（如自有/租房、面积等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApply = false">取消</el-button>
        <el-button type="primary" @click="applyAdopt" :loading="applying">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { petAPI, adoptionAPI } from '../api'

const router = useRouter()
const canvasRef = ref(null)
const detailCanvasRef = ref(null)
const isShelter = computed(() => JSON.parse(sessionStorage.getItem('userInfo') || '{}')?.userType === 1)

const pets = ref([])
const showDetail = ref(false)
const showApply = ref(false)
const selectedPet = ref(null)
const applying = ref(false)
const applyForm = ref({ reason: '', experience: '', housingCondition: '' })

let animationId = null

// ── 真实照片缓存 ──
const photoCache = ref(new Map())

function loadImage(src) {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = () => resolve(null)
    img.src = src
  })
}

async function loadPhotos(petList) {
  const results = await Promise.all(petList.map(async (p) => {
    const url = getFirstImg(p.images)
    if (!url) return { id: p.id, img: null }
    const img = await loadImage(url)
    return { id: p.id, img }
  }))
  const map = new Map()
  results.forEach(r => map.set(r.id, r.img))
  photoCache.value = map
}

function drawPhoto(ctx, img, cx, cy, size) {
  if (!img) return
  const iw = img.naturalWidth
  const ih = img.naturalHeight
  const ratio = iw / ih
  let dw, dh
  if (ratio > 1) { dw = size; dh = size / ratio }
  else { dh = size; dw = size * ratio }
  ctx.save()
  ctx.beginPath()
  ctx.arc(cx + size / 2, cy + size / 2, size / 2, 0, Math.PI * 2)
  ctx.clip()
  ctx.drawImage(img, cx + (size - dw) / 2, cy + (size - dh) / 2, dw, dh)
  ctx.restore()
}

function drawPlaceholder(ctx, pet, cx, cy, size) {
  const colors = { '猫': '#FFD54F', '狗': '#FFAB40' }
  ctx.fillStyle = colors[pet.type] || '#B0BEC5'
  ctx.beginPath()
  ctx.arc(cx + size / 2, cy + size / 2, size / 2, 0, Math.PI * 2)
  ctx.fill()
  ctx.fillStyle = '#fff'
  ctx.font = 'bold 20px "Microsoft YaHei", sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(pet.name.charAt(0), cx + size / 2, cy + size / 2)
}

function getFirstImg(images) {
  if (!images || typeof images !== 'string') return null
  return images.split(',')[0]?.trim() || null
}

// 每个宠物的动画状态
const petStates = ref([])

function initPetStates(rawPets) {
  petStates.value = rawPets.map((p, i) => ({
    id: p.id,
    name: p.name,
    type: p.type,
    breed: p.breed,
    color: p.color,
    age: p.age,
    weight: p.weight,
    gender: p.gender,
    healthStatus: p.healthStatus,
    vaccinated: p.vaccinated,
    sterilized: p.sterilized,
    description: p.description,
    images: p.images,
    x: 0, y: 0,
    dx: 0, dy: 0,
    size: 64,
    bobPhase: Math.random() * Math.PI * 2
  }))
  spreadPets()
}

function spreadPets() {
  const canvas = canvasRef.value
  if (!canvas) return
  const w = canvas.width
  const h = canvas.height
  const pad = 60
  const marginX = pad
  const marginY = 80
  const areaW = w - pad * 2
  const areaH = h - marginY - pad

  petStates.value.forEach(p => {
    p.x = marginX + Math.random() * (areaW - p.size)
    p.y = marginY + Math.random() * (areaH - p.size - 20)
    // 速度大幅降低
    const angle = Math.random() * Math.PI * 2
    const speed = 0.2 + Math.random() * 0.4
    p.dx = Math.cos(angle) * speed
    p.dy = Math.sin(angle) * speed
  })
}

function animate() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const w = canvas.width
  const h = canvas.height
  const pad = 60
  const marginY = 80

  ctx.clearRect(0, 0, w, h)

  // ── 矩形园区背景 ──
  // 草地
  ctx.fillStyle = '#A8D5A2'
  ctx.fillRect(pad, marginY, w - pad * 2, h - marginY - pad)

  // 草地纹理（小花朵和草叶）
  const seed = 42
  for (let i = 0; i < 60; i++) {
    const sx = pad + ((i * 137 + seed) % (w - pad * 2))
    const sy = marginY + ((i * 251 + seed) % (h - marginY - pad))
    const kind = i % 5
    if (kind === 0) {
      // 小草叶
      ctx.fillStyle = '#7CB342'
      ctx.fillRect(sx, sy - 3, 2, 6)
      ctx.fillRect(sx - 2, sy - 1, 2, 4)
      ctx.fillRect(sx + 2, sy - 1, 2, 4)
    } else if (kind === 1) {
      // 小花朵
      ctx.fillStyle = '#FFF9C4'
      ctx.beginPath()
      ctx.arc(sx, sy, 2.5, 0, Math.PI * 2)
      ctx.fill()
    }
  }

  // 围栏
  ctx.strokeStyle = '#8D6E63'
  ctx.lineWidth = 3
  ctx.strokeRect(pad, marginY, w - pad * 2, h - marginY - pad)

  // 围栏柱子
  ctx.fillStyle = '#A1887F'
  const postCount = Math.floor((w - pad * 2) / 50)
  for (let i = 0; i <= postCount; i++) {
    const px = pad + i * ((w - pad * 2) / postCount)
    ctx.fillRect(px - 3, marginY - 6, 6, h - marginY - pad + 12)
    // 柱子顶部圆球
    ctx.beginPath()
    ctx.arc(px, marginY - 4, 5, 0, Math.PI * 2)
    ctx.fillStyle = '#8D6E63'
    ctx.fill()
  }

  // ── 更新并绘制宠物 ──
  const areaLeft = pad
  const areaRight = w - pad
  const areaTop = marginY
  const areaBottom = h - pad

  petStates.value.forEach(p => {
    // 更新位置
    p.x += p.dx
    p.y += p.dy

    // 矩形边界反弹
    const spriteBottom = p.y + p.size + 20  // +20 给名字
    const spriteRight = p.x + p.size

    if (p.x < areaLeft) {
      p.x = areaLeft
      p.dx = Math.abs(p.dx)
    }
    if (spriteRight > areaRight) {
      p.x = areaRight - p.size
      p.dx = -Math.abs(p.dx)
    }
    if (p.y < areaTop) {
      p.y = areaTop
      p.dy = Math.abs(p.dy)
    }
    if (spriteBottom > areaBottom) {
      p.y = areaBottom - p.size - 20
      p.dy = -Math.abs(p.dy)
    }

    // 宠物间碰撞
    for (const other of petStates.value) {
      if (other === p) continue
      const ox = p.x + p.size / 2, oy = p.y + p.size / 2
      const tx = other.x + other.size / 2, ty = other.y + other.size / 2
      const dx = ox - tx, dy = oy - ty
      const dist = Math.sqrt(dx * dx + dy * dy)
      const minDist = p.size * 0.9
      if (dist < minDist && dist > 0) {
        const nx = dx / dist, ny = dy / dist
        p.x += nx * 1
        p.y += ny * 1
        other.x -= nx * 1
        other.y -= ny * 1
        // 速度交换
        const dot = (p.dx - other.dx) * nx + (p.dy - other.dy) * ny
        p.dx -= dot * nx * 0.8
        p.dy -= dot * ny * 0.8
        other.dx += dot * nx * 0.8
        other.dy += dot * ny * 0.8
      }
    }

    // 上下浮动效果
    const bobY = Math.sin(Date.now() * 0.002 + p.bobPhase) * 3

    // 绘制宠物
    const img = photoCache.value.get(p.id)
    if (img) {
      drawPhoto(ctx, img, p.x, p.y + bobY, p.size)
    } else {
      drawPlaceholder(ctx, p, p.x, p.y + bobY, p.size)
    }

    // 绘制名字背景和文字
    const nameX = p.x + p.size / 2
    const nameY = p.y + p.size + 16
    ctx.font = 'bold 13px "Microsoft YaHei", sans-serif'
    ctx.textAlign = 'center'
    const metrics = ctx.measureText(p.name)
    const nw = metrics.width + 12
    const nh = 20
    // 半透明白底
    ctx.fillStyle = 'rgba(255,255,255,0.85)'
    ctx.beginPath()
    ctx.roundRect(nameX - nw / 2, nameY - nh / 2 - 1, nw, nh, 8)
    ctx.fill()
    ctx.strokeStyle = 'rgba(0,0,0,0.1)'
    ctx.lineWidth = 1
    ctx.stroke()
    // 名字文字
    ctx.fillStyle = '#333'
    ctx.fillText(p.name, nameX, nameY + 4)
  })

  animationId = requestAnimationFrame(animate)
}

function handleCanvasClick(e) {
  const canvas = canvasRef.value
  const rect = canvas.getBoundingClientRect()
  const scaleX = canvas.width / rect.width
  const scaleY = canvas.height / rect.height
  const mx = (e.clientX - rect.left) * scaleX
  const my = (e.clientY - rect.top) * scaleY

  for (let i = petStates.value.length - 1; i >= 0; i--) {
    const p = petStates.value[i]
    if (mx >= p.x && mx <= p.x + p.size && my >= p.y && my <= p.y + p.size + 20) {
      selectedPet.value = p
      showDetail.value = true
      nextTick(() => drawDetailAvatar())
      return
    }
  }
}

function handleMouseMove(e) {
  const canvas = canvasRef.value
  if (!canvas) return
  const rect = canvas.getBoundingClientRect()
  const scaleX = canvas.width / rect.width
  const scaleY = canvas.height / rect.height
  const mx = (e.clientX - rect.left) * scaleX
  const my = (e.clientY - rect.top) * scaleY

  let hovering = false
  for (const p of petStates.value) {
    if (mx >= p.x && mx <= p.x + p.size && my >= p.y && my <= p.y + p.size + 20) {
      hovering = true
      break
    }
  }
  canvas.style.cursor = hovering ? 'pointer' : 'default'
}

function drawDetailAvatar() {
  const canvas = detailCanvasRef.value
  if (!canvas || !selectedPet.value) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, 80, 80)
  const img = photoCache.value.get(selectedPet.value.id)
  if (img) {
    drawPhoto(ctx, img, 8, 8, 64)
  } else {
    drawPlaceholder(ctx, selectedPet.value, 8, 8, 64)
  }
}

async function applyAdopt() {
  if (!applyForm.value.reason || !applyForm.value.experience || !applyForm.value.housingCondition) {
    ElMessage.warning('请填写完整的领养申请信息')
    return
  }
  applying.value = true
  try {
    await adoptionAPI.apply({
      petId: selectedPet.value.id,
      ...applyForm.value
    })
    ElMessage.success('领养申请已提交，请等待救助站审核')
    showApply.value = false
    applyForm.value = { reason: '', experience: '', housingCondition: '' }
  } catch (e) {
    ElMessage.error(e.message || '申请失败')
  } finally {
    applying.value = false
  }
}

function logout() { sessionStorage.clear(); router.push('/login') }

onMounted(async () => {
  try {
    const res = await petAPI.search({ pageNum: 1, pageSize: 20 })
    const rawPets = res?.data?.records || []
    if (rawPets.length === 0) {
      ElMessage.info('暂无待领养宠物')
    }
    await loadPhotos(rawPets)
    initPetStates(rawPets)
  } catch (e) {
    ElMessage.error('加载宠物失败')
  }

  const canvas = canvasRef.value
  const container = canvas.parentElement

  function resize() {
    const w = Math.min(container.clientWidth - 40, 800)
    const h = Math.min(w * 0.75, 600)
    canvas.width = w
    canvas.height = h
    canvas.style.display = 'block'
    canvas.style.margin = '0 auto'
    spreadPets()
  }

  resize()
  animate()

  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
})

watch(showDetail, (val) => {
  if (!val) selectedPet.value = null
})
</script>

<style scoped>
.page-content { padding-top: 60px; }
.game-container { text-align:center; padding:20px 0; }
.game-title { margin-bottom:20px; }
.game-title h2 { color:#e67e22; margin-bottom:8px; font-size:28px; }
.game-title p { color:#888; }
.detail-content { text-align:center; }
.detail-avatar { margin-bottom:16px; }
.detail-avatar canvas { border-radius:12px; background:#f5f5f5; }
.detail-photo { width:160px; height:160px; object-fit:cover; border-radius:12px; }
.desc { color:#666; margin-top:16px; text-align:left; line-height:1.6; }
</style>
<style>
.game-dialog .el-dialog__body { max-height:calc(90vh - 120px); overflow-y:auto; }
</style>
