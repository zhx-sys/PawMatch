<template>
  <div class="page-content">
<div>
        <div class="container">

          <!-- ========== 普通用户========== -->
          <template v-if="!isShelter">
            <h2>寄养服务</h2>
            <!-- 搜索格-->
            <el-card style="margin-bottom:20px">
              <el-row :gutter="12">
                <el-col :span="6">
                  <el-input v-model="filters.keyword" placeholder="搜索标题" />
                </el-col>
                <el-col :span="4">
                  <el-select v-model="filters.petType" placeholder="宠物类型" clearable style="width:100%">
                    <el-option label="狗" value="狗" /><el-option label="猫" value="猫" />
                  </el-select>
                </el-col>
                <el-col :span="3">
                  <el-input-number v-model="filters.minPrice" placeholder="最低价" :min="0" style="width:100%" controls-position="right" />
                </el-col>
                <el-col :span="3">
                  <el-input-number v-model="filters.maxPrice" placeholder="最高价" :min="0" style="width:100%" controls-position="right" />
                </el-col>
                <el-col :span="4">
                  <el-button type="primary" @click="searchServices" style="width:100%">搜索</el-button>
                </el-col>
                <el-col :span="4">
                  <el-button @click="resetFilters" style="width:100%">重置</el-button>
                </el-col>
              </el-row>
            </el-card>
            <!-- 服务卡片 -->
            <el-row :gutter="16">
              <el-col :span="8" v-for="s in services" :key="s.id">
                <el-card class="service-card" shadow="hover">
                  <h3>{{ s.title }}</h3>
                  <p class="svc-desc">{{ s.description }}</p>
                  <el-descriptions :column="1" border size="small" style="margin:12px 0">
                    <el-descriptions-item label="救助站">{{ s.shelterName }}</el-descriptions-item>
                    <el-descriptions-item label="类型">{{ s.petType }}</el-descriptions-item>
                    <el-descriptions-item label="价格">{{ s.pricePerDay }}元/天</el-descriptions-item>
                    <el-descriptions-item label="容量">{{ s.maxCapacity }}只</el-descriptions-item>
                  </el-descriptions>
                  <el-button type="primary" @click="openBookDialog(s)">预约寄养</el-button>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-if="!services.length && !serviceLoading" description="暂无寄养服务" />
            <el-pagination
              v-if="serviceTotal > filters.pageSize"
              style="margin-top:20px;justify-content:center"
              :total="serviceTotal" :page-size="filters.pageSize"
              v-model:current-page="filters.pageNum" layout="prev,pager,next"
              @current-change="searchServices" />

            <!-- 预约弹窗 -->
            <el-dialog v-model="showBook" title="预约寄养" width="480px" class="foster-dialog">
              <el-form :model="bookForm" label-width="90px">
                <el-form-item label="服务">{{ selectedService?.title }}</el-form-item>
                <el-form-item label="救助站">{{ selectedService?.shelterName }}</el-form-item>
                <el-form-item label="价格">{{ selectedService?.pricePerDay }}元/天</el-form-item>
                <el-form-item label="宠物名称"><el-input v-model="bookForm.petName" placeholder="你的宠物叫什么" /></el-form-item>
                <el-form-item label="宠物类型">
                  <el-select v-model="bookForm.petType" style="width:100%" popper-class="foster-popper">
                    <el-option label="狗" value="狗" /><el-option label="猫" value="猫" /><el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
                <el-form-item label="寄养日期">
                  <el-date-picker v-model="bookForm.dateRange" type="daterange" range-separator="至"
                    start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" popper-class="foster-date-popper" />
                </el-form-item>
                <el-form-item label="特殊需求"><el-input v-model="bookForm.specialRequests" type="textarea" :rows="2" placeholder="选填" /></el-form-item>
                <el-form-item label="预估费用" v-if="bookForm.dateRange?.length===2">
                  <span style="color:#e67e22;font-size:18px;font-weight:bold">
                    {{ calcPrice }} 元                  </span>
                  <span style="color:#999;margin-left:8px">（{{ calcDays }}天 × {{ selectedService?.pricePerDay }}元/天）</span>
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="showBook = false">取消</el-button>
                <el-button type="primary" @click="submitOrder" :loading="booking">提交预约</el-button>
              </template>
            </el-dialog>

            <!-- 我的订单 -->
            <el-divider />
            <h3 style="margin-bottom:16px">我的寄养订单</h3>
            <el-table :data="orders" stripe>
              <el-table-column prop="id" label="编号" width="80" />
              <el-table-column prop="serviceName" label="服务" />
              <el-table-column prop="shelterName" label="救助站" width="120" />
              <el-table-column prop="petName" label="宠物" width="80" />
              <el-table-column label="日期" width="200">
                <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
              </el-table-column>
              <el-table-column prop="totalPrice" label="金额" width="80" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160">
                <template #default="{ row }">
                  <el-button v-if="row.status===0" size="small" type="danger" @click="cancelOrder(row)">取消</el-button>
                  <el-button v-if="row.status===3 && !row.rating" size="small" type="warning" @click="openReview(row)">评价</el-button>
                  <span v-if="row.rating" style="color:#e67e22">已评 {{ row.rating }}星</span>
                </template>
              </el-table-column>
            </el-table>
          </template>

          <!-- ========== 救助站========== -->
          <template v-else>
            <el-tabs v-model="shelterTab" type="border-card">
              <!-- 我的寄养服务 -->
              <el-tab-pane label="我的寄养服务" name="services">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
                  <h3 style="margin:0">已发布服务</h3>
                  <el-button type="primary" @click="openServiceDialog()">发布新服务</el-button>
                </div>
                <el-table :data="myServices" stripe>
                  <el-table-column prop="title" label="标题" />
                  <el-table-column prop="petType" label="类型" width="80" />
                  <el-table-column prop="pricePerDay" label="价格" width="100">
                    <template #default="{ row }">{{ row.pricePerDay }}元/天</template>
                  </el-table-column>
                  <el-table-column prop="maxCapacity" label="容量" width="80" />
                  <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.status===1?'success':'info'">{{ row.status===1?'上架':'下架' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200">
                    <template #default="{ row }">
                      <el-button size="small" @click="openServiceDialog(row)">编辑</el-button>
                      <el-button v-if="row.status===1" size="small" type="danger" @click="delService(row)">下架</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!myServices.length" description="还没有发布寄养服务" />
              </el-tab-pane>

              <!-- 收到订单 -->
              <el-tab-pane label="收到订单" name="orders">
                <el-table :data="shelterOrders" stripe>
                  <el-table-column prop="id" label="编号" width="80" />
                  <el-table-column prop="serviceName" label="服务" />
                  <el-table-column prop="petName" label="宠物" width="80" />
                  <el-table-column prop="petType" label="类型" width="60" />
                  <el-table-column label="日期" width="200">
                    <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
                  </el-table-column>
                  <el-table-column prop="totalPrice" label="金额" width="80" />
                  <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="160">
                    <template #default="{ row }">
                      <el-button v-if="row.status===0" size="small" type="success" @click="confirmOrder(row)">确认接单</el-button>
                      <el-button v-if="row.status===1" size="small" type="primary" @click="completeOrder(row)">完成</el-button>
                      <span v-if="row.status===3||row.status===4" style="color:#999">-</span>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!shelterOrders.length" description="暂无订单" />
              </el-tab-pane>
            </el-tabs>
          </template>
        </div>
      </div>
    <!-- 发布/编辑服务弹窗 -->
    <el-dialog v-model="showService" :title="editingService?.id?'编辑服务':'发布新服务'" width="500px" top="2vh" class="foster-dialog">
      <el-form :model="serviceForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="serviceForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="serviceForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="宠物类型">
          <el-select v-model="serviceForm.petType" style="width:100%" popper-class="foster-popper">
            <el-option label="狗" value="狗" /><el-option label="猫" value="猫" /><el-option label="猫狗均可" value="猫狗均可" />
          </el-select>
        </el-form-item>
        <el-form-item label="每日价格"><el-input-number v-model="serviceForm.pricePerDay" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="最大容量"><el-input-number v-model="serviceForm.maxCapacity" :min="1" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showService = false">取消</el-button>
        <el-button type="primary" @click="saveService" :loading="savingService">保存</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="showReview" title="评价订单" width="400px" class="foster-dialog">
      <el-form>
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" show-score />
        </el-form-item>
        <el-form-item label="评价内容"><el-input v-model="reviewForm.comment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReview = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewing">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fosterAPI } from '../api'

const router = useRouter()
const isShelter = computed(() => JSON.parse(sessionStorage.getItem('userInfo') || '{}')?.userType === 1)

// --- 普通用户数据---
const services = ref([])
const serviceTotal = ref(0)
const serviceLoading = ref(false)
const filters = ref({ keyword: '', petType: '', minPrice: null, maxPrice: null, pageNum: 1, pageSize: 9 })

const showBook = ref(false)
const selectedService = ref(null)
const booking = ref(false)
const bookForm = ref({ petName: '', petType: '狗', dateRange: [], specialRequests: '' })
const calcDays = computed(() => {
  if (bookForm.value.dateRange?.length === 2) {
    const [s, e] = bookForm.value.dateRange
    return Math.max(1, Math.ceil((new Date(e) - new Date(s)) / 86400000) + 1)
  }
  return 0
})
const calcPrice = computed(() => calcDays.value * (selectedService.value?.pricePerDay || 0))

const orders = ref([])

const showReview = ref(false)
const reviewing = ref(false)
const reviewTarget = ref(null)
const reviewForm = ref({ rating: 5, comment: '' })

// --- 救助站数据---
const shelterTab = ref('services')
const myServices = ref([])
const shelterOrders = ref([])
const showService = ref(false)
const editingService = ref(null)
const savingService = ref(false)
const serviceForm = ref({ title: '', description: '', petType: '狗', pricePerDay: 0, maxCapacity: 1 })

onMounted(() => {
  if (isShelter.value) loadShelterData()
  else { searchServices(); loadOrders() }
})

// === 普通用户方法===
async function searchServices() {
  serviceLoading.value = true
  try {
    const params = { pageNum: filters.value.pageNum, pageSize: filters.value.pageSize }
    if (filters.value.petType) params.petType = filters.value.petType
    if (filters.value.minPrice != null) params.minPrice = filters.value.minPrice
    if (filters.value.maxPrice != null) params.maxPrice = filters.value.maxPrice
    if (filters.value.keyword) params.keyword = filters.value.keyword
    const res = await fosterAPI.searchService(params)
    services.value = res?.data?.records || []
    serviceTotal.value = res?.data?.total || 0
  } catch (e) {} finally { serviceLoading.value = false }
}

function resetFilters() {
  filters.value = { keyword: '', petType: '', minPrice: null, maxPrice: null, pageNum: 1, pageSize: 9 }
  searchServices()
}

function openBookDialog(s) {
  selectedService.value = s
  bookForm.value = { petName: '', petType: s.petType || '狗', dateRange: [], specialRequests: '' }
  showBook.value = true
}

async function submitOrder() {
  if (!bookForm.value.petName) { ElMessage.warning('请输入宠物名称'); return }
  if (bookForm.value.dateRange?.length !== 2) { ElMessage.warning('请选择寄养日期'); return }
  booking.value = true
  try {
    await fosterAPI.createOrder({
      serviceId: selectedService.value.id,
      petName: bookForm.value.petName,
      petType: bookForm.value.petType,
      startDate: bookForm.value.dateRange[0],
      endDate: bookForm.value.dateRange[1],
      specialRequests: bookForm.value.specialRequests || undefined
    })
    ElMessage.success('预约成功，等待救助站确认')
    showBook.value = false
    loadOrders()
  } catch (e) { ElMessage.error(e.message) } finally { booking.value = false }
}

async function loadOrders() {
  try {
    const res = await fosterAPI.orderList()
    orders.value = res?.data?.records || res?.data || []
  } catch (e) {}
}

async function cancelOrder(row) {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '确认', { type: 'warning', appendTo: document.body })
    await fosterAPI.cancelOrder(row.id)
    ElMessage.success('已取消')
    loadOrders()
  } catch (e) {}
}

function openReview(row) { reviewTarget.value = row; reviewForm.value = { rating: 5, comment: '' }; showReview.value = true }

async function submitReview() {
  reviewing.value = true
  try {
    await fosterAPI.reviewOrder(reviewTarget.value.id, reviewForm.value)
    ElMessage.success('评价成功')
    showReview.value = false
    loadOrders()
  } catch (e) { ElMessage.error(e.message) } finally { reviewing.value = false }
}

// === 救助站方法===
async function loadShelterData() {
  try {
    const [svcRes, ordRes] = await Promise.all([
      fosterAPI.searchService({ pageNum: 1, pageSize: 100 }),
      fosterAPI.shelterOrderList({ pageNum: 1, pageSize: 100 })
    ])
    myServices.value = svcRes?.data?.records || []
    shelterOrders.value = ordRes?.data?.records || []
  } catch (e) {}
}

function openServiceDialog(row) {
  editingService.value = row || null
  if (row) {
    serviceForm.value = { title: row.title, description: row.description, petType: row.petType, pricePerDay: row.pricePerDay, maxCapacity: row.maxCapacity }
  } else {
    serviceForm.value = { title: '', description: '', petType: '狗', pricePerDay: 0, maxCapacity: 1 }
  }
  showService.value = true
}

async function saveService() {
  if (!serviceForm.value.title) { ElMessage.warning('请输入标题'); return }
  savingService.value = true
  try {
    if (editingService.value?.id) {
      await fosterAPI.updateService(editingService.value.id, serviceForm.value)
      ElMessage.success('更新成功')
    } else {
      await fosterAPI.createService(serviceForm.value)
      ElMessage.success('发布成功')
    }
    showService.value = false
    loadShelterData()
  } catch (e) { ElMessage.error(e.message) } finally { savingService.value = false }
}

async function delService(row) {
  try {
    await ElMessageBox.confirm(`确定下架「${row.title}」？`, '确认', { type: 'warning', appendTo: document.body })
    await fosterAPI.deleteService(row.id)
    ElMessage.success('已下架')
    loadShelterData()
  } catch (e) {}
}

async function confirmOrder(row) {
  try {
    await ElMessageBox.confirm(`确定接单「${row.petName}」的寄养预约？`, '确认', { appendTo: document.body })
    await fosterAPI.confirmOrder(row.id)
    ElMessage.success('已确认接单')
    loadShelterData()
  } catch (e) {}
}

async function completeOrder(row) {
  try {
    await ElMessageBox.confirm(`确认完成「${row.petName}」的寄养已完成？`, '确认')
    await fosterAPI.completeOrder(row.id)
    ElMessage.success('已完成')
    loadShelterData()
  } catch (e) {}
}

// --- 通用 ---
function statusLabel(s) { return ['待确认','已确认','','已完成','已取消'][s] || '未知' }
function statusType(s) { return ['warning','primary','','success','danger'][s] || 'info' }

function logout() { sessionStorage.clear(); router.push('/login') }
</script>

<style scoped>
.page-content { padding-top: 60px; }
.service-card { margin-bottom:16px; border-radius:10px; }
.service-card:hover { border-color:#e67e22; }
.svc-desc { color:#888; font-size:13px; margin:8px 0; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
</style>
<style>
.foster-popper { z-index: 9999 !important; }
.foster-date-popper { z-index: 9999 !important; }
.foster-date-popper .el-picker-panel { background: #fff !important; }
.foster-date-popper .el-date-range-picker__content { background: #fff !important; }
.foster-date-popper .el-date-table td { padding: 2px 0 !important; }
.foster-date-popper .el-date-table-cell { cursor: pointer !important; border-radius: 50% !important; width: 32px !important; height: 32px !important; line-height: 32px !important; text-align: center !important; display: inline-block !important; }
.foster-date-popper .el-date-table-cell:hover { background: #e6f7ff !important; color: #409eff !important; }
.foster-dialog .el-dialog__body { max-height: calc(90vh - 120px); overflow-y: auto; }
</style>
（内容由AI生成，仅供参考）
