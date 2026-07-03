<template>
  <el-card>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="roleName" label="角色" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'启用':'停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.status===1" @change="toggleStatus(row)" />
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const list = ref([])
const loading = ref(false)

onMounted(loadData)

async function loadData() {
  loading.value = true
  try { list.value = (await request.get('/admin/users')).records } catch {} finally { loading.value = false }
}

async function toggleStatus(user) {
  try { await request.put(`/admin/users/${user.userId}/toggle-status`); ElMessage.success('状态已更新'); loadData() } catch {}
}
</script>
