<template>
  <div class="page-content">
    <div class="container">
      <div class="edit-wrap">
        <el-page-header @back="goBack" :content="isEdit ? '编辑词条' : '创建词条'" style="margin-bottom:20px" />
        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="标题" required>
            <el-input v-model="form.title" placeholder="词条标题" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="分类">
            <el-popover
              :visible="popoverVisible"
              placement="bottom-start"
              :width="400"
              trigger="click"
              @show="popoverVisible = true"
              @hide="popoverVisible = false"
            >
              <template #reference>
                <div class="cat-trigger" @click="popoverVisible = !popoverVisible">
                  <span v-if="selectedCategoryPath" class="cat-label">{{ selectedCategoryPath }}</span>
                  <span v-else class="cat-placeholder">选择分类</span>
                  <span v-if="form.categoryId" class="cat-clear" @click.stop="clearCategory">&times;</span>
                  <span class="cat-arrow" :class="{ open: popoverVisible }">&#9650;</span>
                </div>
              </template>
              <div class="cat-tree-wrap">
                <el-tree
                  ref="treeRef"
                  :data="treeData"
                  :props="{ children: 'children', label: 'name' }"
                  node-key="id"
                  :default-expanded-keys="defaultExpandedKeys"
                  highlight-current
                  :expand-on-click-node="false"
                  @node-click="handleNodeClick"
                >
                  <template #default="{ node, data }">
                    <span class="tree-node" :class="{ leaf: !data.children || data.children.length === 0 }">
                      <span class="tree-node-label">{{ node.label }}</span>
                      <span v-if="selectedId === data.id" class="tree-node-check">&#10003;</span>
                    </span>
                  </template>
                </el-tree>
              </div>
            </el-popover>
          </el-form-item>
          <el-form-item label="摘要">
            <el-input v-model="form.summary" placeholder="简要描述（选填）" maxlength="500" show-word-limit type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="内容" required>
            <el-input v-model="form.content" placeholder="词条详细内容" type="textarea" :rows="12" />
          </el-form-item>
          <el-form-item v-if="isEdit" label="编辑说明">
            <el-input v-model="form.editSummary" placeholder="本次修改说明（选填）" maxlength="200" />
          </el-form-item>
          <el-form-item>
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" @click="submit" :loading="submitting">
              {{ isEdit ? '保存修改' : '提交词条' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { wikiAPI } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id && route.path.includes('/edit'))
const submitting = ref(false)

const form = ref({
  title: '',
  summary: '',
  content: '',
  categoryId: null,
  editSummary: ''
})

// 分类
const treeData = ref([])
const treeRef = ref(null)
const popoverVisible = ref(false)
const selectedId = ref(null)
const selectedCategoryPath = ref('')
const defaultExpandedKeys = ref([])

// 递归查找节点路径名称
function findPathName(tree, targetId, path = []) {
  for (const node of tree) {
    const current = [...path, node.name]
    if (node.id === targetId) return current
    if (node.children) {
      const found = findPathName(node.children, targetId, current)
      if (found) return found
    }
  }
  return null
}

// 递归查找节点及所有祖先ID
function findAncestorIds(tree, targetId, ancestors = []) {
  for (const node of tree) {
    if (node.id === targetId) return [...ancestors, node.id]
    if (node.children) {
      const found = findAncestorIds(node.children, targetId, [...ancestors, node.id])
      if (found) return found
    }
  }
  return null
}

function handleNodeClick(data) {
  // 只允许选择叶子节点（二级分类）
  if (data.children && data.children.length > 0) return
  selectedId.value = data.id
  form.value.categoryId = data.id
  const pathNames = findPathName(treeData.value, data.id)
  selectedCategoryPath.value = pathNames ? pathNames.join(' / ') : data.name
  popoverVisible.value = false
}

function clearCategory() {
  selectedId.value = null
  form.value.categoryId = null
  selectedCategoryPath.value = ''
}

onMounted(async () => {
  try {
    const res = await wikiAPI.categories()
    treeData.value = res?.data || []
  } catch (e) {}

  if (isEdit.value) {
    try {
      const res = await wikiAPI.entryDetail(route.params.id)
      const entry = res?.data
      if (entry) {
        form.value.title = entry.title
        form.value.summary = entry.summary
        form.value.content = entry.content
        form.value.categoryId = entry.categoryId
        // 设置已选分类
        if (entry.categoryId) {
          selectedId.value = entry.categoryId
          await nextTick()
          const pathNames = findPathName(treeData.value, entry.categoryId)
          selectedCategoryPath.value = pathNames ? pathNames.join(' / ') : ''
          const ancestorIds = findAncestorIds(treeData.value, entry.categoryId)
          defaultExpandedKeys.value = ancestorIds || []
        }
      }
    } catch (e) {
      ElMessage.error('词条不存在')
      router.push('/wiki')
    }
  }
})

function goBack() {
  if (isEdit.value) {
    router.push('/wiki/' + route.params.id)
  } else {
    router.push('/wiki')
  }
}

async function submit() {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  submitting.value = true
  try {
    const data = {
      title: form.value.title,
      summary: form.value.summary,
      content: form.value.content,
      categoryId: form.value.categoryId
    }
    if (isEdit.value) {
      data.editSummary = form.value.editSummary
      await wikiAPI.editEntry(route.params.id, data)
      ElMessage.success('编辑已提交')
      router.push('/wiki/' + route.params.id)
    } else {
      await wikiAPI.createEntry(data)
      ElMessage.success('词条已提交')
      router.push('/wiki')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-content { padding-top: 60px; min-height: 100vh; background: #f7f4f0; }
.container { max-width: 800px; margin: 0 auto; padding: 20px; }
.edit-wrap { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,.04); }

/* 分类触发器 */
.cat-trigger {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 12px;
  background: #f5f6f8;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s;
  user-select: none;
}
.cat-trigger:hover {
  border-color: #e67e22;
  background: #fdf6ee;
}
.cat-placeholder { color: #bbb; font-size: 14px; }
.cat-label { color: #333; font-size: 14px; flex: 1; }
.cat-clear {
  color: #ccc;
  font-size: 18px;
  margin-right: 6px;
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s;
}
.cat-clear:hover { color: #e67e22; }
.cat-arrow {
  font-size: 10px;
  color: #aaa;
  transition: transform 0.25s;
}
.cat-arrow.open { transform: rotate(180deg); }

/* 树容器 */
.cat-tree-wrap {
  max-height: 360px;
  overflow-y: auto;
  padding: 4px 0;
}
.cat-tree-wrap :deep(.el-tree-node__content) {
  height: 36px;
  padding: 0 8px;
  border-radius: 6px;
  margin: 1px 4px;
  transition: background 0.15s;
}
.cat-tree-wrap :deep(.el-tree-node__content:hover) {
  background: #fdf6ee;
}
.cat-tree-wrap :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: #fef0e0;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  font-size: 14px;
}
.tree-node.leaf { color: #555; }
.tree-node-label { flex: 1; }
.tree-node-check { color: #e67e22; font-weight: 700; margin-left: 8px; }
</style>