<template>
  <div class="page-content">
<div>
        <div class="container">
          <h2 style="margin-bottom:20px">寄养服务管理</h2>

          <el-tabs v-model="shelterTab" type="border-card">
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
        </div>
      </div>
    <!-- 发布/编辑服务弹窗 -->
    <el-dialog v-model="showService" :title="editingService?.id?'编辑服务':'发布新服务'" width="500px">
      <el-form :model="serviceForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="serviceForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="serviceForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="宠物类型">
          <el-select v-model="serviceForm.petType" style="width:100%" popper-class="shelter-foster-popper">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fosterAPI } from '../api'

const router = useRouter()
const user = ref(JSON.parse(sessionStorage.getItem('userInfo') || '{}'))

const shelterTab = ref('services')
const myServices = ref([])
const shelterOrders = ref([])
const showService = ref(false)
const editingService = ref(null)
const savingService = ref(false)
const serviceForm = ref({ title: '', description: '', petType: '狗', pricePerDay: 0, maxCapacity: 1 })

onMounted(loadShelterData)

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
    await ElMessageBox.confirm(`确定下架「${row.title}」？`, '确认', { type: 'warning' })
    await fosterAPI.deleteService(row.id)
    ElMessage.success('已下架')
    loadShelterData()
  } catch (e) {}
}

async function confirmOrder(row) {
  try {
    await ElMessageBox.confirm(`确定接单「${row.petName}」的寄养预约？`, '确认')
    await fosterAPI.confirmOrder(row.id)
    ElMessage.success('已确认接单')
    loadShelterData()
  } catch (e) {}
}

async function completeOrder(row) {
  try {
    await ElMessageBox.confirm(`确认「${row.petName}」的寄养已完成？`, '确认')
    await fosterAPI.completeOrder(row.id)
    ElMessage.success('已完成')
    loadShelterData()
  } catch (e) {}
}

function statusLabel(s) { return ['待确认','已确认','','已完成','已取消'][s] || '未知' }
function statusType(s) { return ['warning','primary','','success','danger'][s] || 'info' }

function logout() { sessionStorage.clear(); router.push('/login') }
</script>

<style scoped>
.page-content { padding-top: 60px; }
.container { max-width:1200px; margin:0 auto; padding:20px 0; }
</style>
<style>
.shelter-foster-popper { z-index: 9999 !important; }
</style>
（内容由AI生成，仅供参考）
