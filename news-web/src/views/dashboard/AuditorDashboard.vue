<template>
  <div>
    <div class="stats-row">
      <router-link to="/admin/audit" class="stat-card stat-yellow"><div class="stat-icon">🕐</div><div class="stat-num">{{ stats.pendingCount }}</div><div class="stat-label">待审核</div></router-link>
      <div class="stat-card stat-green"><div class="stat-icon">✅</div><div class="stat-num">{{ stats.todayPublished }}</div><div class="stat-label">今日通过</div></div>
      <div class="stat-card stat-red"><div class="stat-icon">↩️</div><div class="stat-num">{{ stats.todayRejected }}</div><div class="stat-label">今日退回</div></div>
    </div>
    <div class="card">
      <div class="card-title">📋 待审稿件预览</div>
      <el-table :data="pendingList" v-loading="loading" empty-text="暂无待审核稿件 🎉">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import request from '@/api/request'

const stats = reactive({ pendingCount: 0, todayPublished: 0, todayRejected: 0 })
const pendingList = ref([])
const loading = ref(false)

onMounted(async () => {
  try {
    const [s, p] = await Promise.all([
      request.get('/dashboard/stats'),
      request.get('/news', { params: { status: 'pending', size: 5 } })
    ])
    Object.assign(stats, s)
    pendingList.value = p.records || []
  } catch {}
})
</script>

<style scoped>
.stats-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,.06); border-top: 3px solid #e85d3f; text-decoration: none; color: inherit; display: block; }
.stat-card:hover { transform: translateY(-3px); }
.stat-icon { font-size: 28px; margin-bottom: 8px; }
.stat-num { font-size: 32px; font-weight: 800; }
.stat-label { font-size: 13px; color: #5f6b7a; margin-top: 4px; }
.stat-yellow { border-top-color: #f2c94c; } .stat-yellow .stat-num { color: #d97706; }
.stat-green { border-top-color: #27ae60; } .stat-green .stat-num { color: #059669; }
.stat-red { border-top-color: #eb5757; } .stat-red .stat-num { color: #dc2626; }
.card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,.06); }
.card-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid #eaecf0; }
</style>
