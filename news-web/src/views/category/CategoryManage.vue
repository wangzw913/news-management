<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between;align-items:center"><span>分类管理</span><el-button type="primary" size="small" @click="openDialog()">+ 新增</el-button></div></template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.categoryId)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editing ? '编辑分类' : '新增分类'" width="400px">
    <el-form :model="form">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const form = reactive({ name: '', sortOrder: 0 })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try { list.value = await request.get('/categories') } catch {} finally { loading.value = false }
}

function openDialog(row) {
  if (row) { editing.value = row; Object.assign(form, row) }
  else { editing.value = null; form.name = ''; form.sortOrder = 0 }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (editing.value) { await request.put(`/admin/categories/${editing.value.categoryId}`, form) }
    else { await request.post('/admin/categories', form) }
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch {}
}

async function handleDelete(id) {
  try { await request.delete(`/admin/categories/${id}`); ElMessage.success('删除成功'); loadData() } catch {}
}
</script>
