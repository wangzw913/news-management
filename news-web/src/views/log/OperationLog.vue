<template>
  <el-card>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="action" label="操作" width="150" />
      <el-table-column prop="target" label="目标" />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
    <div style="text-align:center;margin-top:16px">
      <el-pagination v-model:current-page="page" :total="total" layout="prev, pager, next" @current-change="loadData" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/admin/logs', { params: { page: page.value } })
    list.value = data.records; total.value = data.total
  } catch {} finally { loading.value = false }
}
</script>
