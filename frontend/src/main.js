import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Element Plus components - on-demand
import {
  ElAvatar, ElBadge, ElButton, ElCard, ElCarousel, ElCarouselItem,
  ElCascader, ElDatePicker, ElDescriptions, ElDescriptionsItem,
  ElDialog, ElDivider, ElDropdown, ElDropdownMenu, ElDropdownItem,
  ElEmpty, ElForm, ElFormItem, ElHeader, ElIcon, ElImage, ElInput,
  ElInputNumber, ElMenu, ElMenuItem, ElOption, ElPagination, ElProgress,
  ElRadio, ElRadioButton, ElRadioGroup, ElRate, ElRow, ElCol, ElSelect,
  ElStatistic, ElSwitch, ElTabPane, ElTable, ElTableColumn,
  ElTabs, ElTag, ElTimeline, ElTimelineItem, ElTree, ElUpload
} from 'element-plus'

// Element Plus styles - on-demand
import 'element-plus/theme-chalk/base.css'
import 'element-plus/theme-chalk/el-avatar.css'
import 'element-plus/theme-chalk/el-badge.css'
import 'element-plus/theme-chalk/el-button.css'
import 'element-plus/theme-chalk/el-card.css'
import 'element-plus/theme-chalk/el-carousel.css'
import 'element-plus/theme-chalk/el-carousel-item.css'
import 'element-plus/theme-chalk/el-cascader.css'
import 'element-plus/theme-chalk/el-date-picker.css'
import 'element-plus/theme-chalk/el-descriptions.css'
import 'element-plus/theme-chalk/el-descriptions-item.css'
import 'element-plus/theme-chalk/el-dialog.css'
import 'element-plus/theme-chalk/el-divider.css'
import 'element-plus/theme-chalk/el-dropdown.css'
import 'element-plus/theme-chalk/el-dropdown-menu.css'
import 'element-plus/theme-chalk/el-dropdown-item.css'
import 'element-plus/theme-chalk/el-empty.css'
import 'element-plus/theme-chalk/el-form.css'
import 'element-plus/theme-chalk/el-form-item.css'
import 'element-plus/theme-chalk/el-header.css'
import 'element-plus/theme-chalk/el-icon.css'
import 'element-plus/theme-chalk/el-image.css'
import 'element-plus/theme-chalk/el-input.css'
import 'element-plus/theme-chalk/el-input-number.css'
import 'element-plus/theme-chalk/el-menu.css'
import 'element-plus/theme-chalk/el-menu-item.css'
import 'element-plus/theme-chalk/el-option.css'
import 'element-plus/theme-chalk/el-pagination.css'
import 'element-plus/theme-chalk/el-progress.css'
import 'element-plus/theme-chalk/el-radio.css'
import 'element-plus/theme-chalk/el-radio-button.css'
import 'element-plus/theme-chalk/el-radio-group.css'
import 'element-plus/theme-chalk/el-rate.css'
import 'element-plus/theme-chalk/el-row.css'
import 'element-plus/theme-chalk/el-col.css'
import 'element-plus/theme-chalk/el-select.css'
import 'element-plus/theme-chalk/el-statistic.css'
import 'element-plus/theme-chalk/el-switch.css'
import 'element-plus/theme-chalk/el-tabs.css'
import 'element-plus/theme-chalk/el-tab-pane.css'
import 'element-plus/theme-chalk/el-table.css'
import 'element-plus/theme-chalk/el-table-column.css'
import 'element-plus/theme-chalk/el-tag.css'
import 'element-plus/theme-chalk/el-timeline.css'
import 'element-plus/theme-chalk/el-timeline-item.css'
import 'element-plus/theme-chalk/el-tree.css'
import 'element-plus/theme-chalk/el-upload.css'
import 'element-plus/theme-chalk/el-message.css'
import 'element-plus/theme-chalk/el-message-box.css'

import App from './App.vue'
import router from './router'
import './assets/style.css'

const app = createApp(App)

// Register Element Plus components
const epComponents = [
  ElAvatar, ElBadge, ElButton, ElCard, ElCarousel, ElCarouselItem,
  ElCascader, ElDatePicker, ElDescriptions, ElDescriptionsItem,
  ElDialog, ElDivider, ElDropdown, ElDropdownMenu, ElDropdownItem,
  ElEmpty, ElForm, ElFormItem, ElHeader, ElIcon, ElImage, ElInput,
  ElInputNumber, ElMenu, ElMenuItem, ElOption, ElPagination, ElProgress,
  ElRadio, ElRadioButton, ElRadioGroup, ElRate, ElRow, ElCol, ElSelect,
  ElStatistic, ElSwitch, ElTabPane, ElTable, ElTableColumn,
  ElTabs, ElTag, ElTimeline, ElTimelineItem, ElTree, ElUpload
]
epComponents.forEach(comp => app.component(comp.name, comp))

// Pinia
app.use(createPinia())

app.use(router)
app.mount('#app')
