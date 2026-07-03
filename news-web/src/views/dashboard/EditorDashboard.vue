<template>
  <div>
    <div class="stats-row-editor">
      <router-link to="/admin/news" class="stat-card stat-gray"><div class="stat-icon">📝</div><div class="stat-num">{{ stats.myNews }}</div><div class="stat-label">我的稿件</div></router-link>
      <router-link to="/admin/news?status=pending" class="stat-card stat-yellow"><div class="stat-icon">🕐</div><div class="stat-num">{{ stats.myPending }}</div><div class="stat-label">待审核</div></router-link>
      <router-link to="/admin/news?status=published" class="stat-card stat-green"><div class="stat-icon">✅</div><div class="stat-num">{{ stats.myPublished }}</div><div class="stat-label">已发布</div></router-link>
      <router-link to="/admin/news?status=rejected" class="stat-card stat-red"><div class="stat-icon">↩️</div><div class="stat-num">{{ stats.myRejected }}</div><div class="stat-label">已退回</div></router-link>
    </div>
    <div class="quick-actions">
      <router-link to="/admin/news/create" class="quick-action-card">
        <div class="quick-action-icon">✍️</div><div class="quick-action-title">写新稿件</div><div class="quick-action-desc">开始撰写一篇新的新闻稿件</div>
      </router-link>
      <router-link v-if="stats.myRejected > 0" to="/admin/news?status=rejected" class="quick-action-card">
        <div class="quick-action-icon">📝</div><div class="quick-action-title">修改退回稿件</div><div class="quick-action-desc">{{ stats.myRejected }} 篇稿件被退回</div>
      </router-link>
      <router-link to="/admin/news?status=draft" class="quick-action-card">
        <div class="quick-action-icon">📄</div><div class="quick-action-title">继续草稿</div><div class="quick-action-desc">{{ stats.myDraft || 0 }} 篇草稿等待完成</div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import request from '@/api/request'

const stats = reactive({ myNews: 0, myPublished: 0, myPending: 0, myRejected: 0, myDraft: 0 })

onMounted(async () => {
  try { Object.assign(stats, await request.get('/dashboard/stats')) } catch {}
})
</script>

<style scoped>
.stats-row-editor { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 32px 24px; box-shadow: 0 1px 3px rgba(0,0,0,.06); display: flex; align-items: center; gap: 16px; text-decoration: none; color: inherit; border-top: 3px solid transparent; }
.stat-card:hover { transform: translateY(-3px); }
.stat-icon { font-size: 40px; }
.stat-num { font-size: 42px; font-weight: 800; }
.stat-label { font-size: 15px; color: #5f6b7a; }
.stat-gray { border-top-color: #828282; } .stat-gray .stat-num { color: #6b7280; }
.stat-yellow { border-top-color: #f2c94c; } .stat-yellow .stat-num { color: #d97706; }
.stat-green { border-top-color: #27ae60; } .stat-green .stat-num { color: #059669; }
.stat-red { border-top-color: #eb5757; } .stat-red .stat-num { color: #dc2626; }
.quick-actions { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 20px; }
.quick-action-card { background: #fff; border-radius: 8px; padding: 24px; text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,.06); text-decoration: none; color: inherit; display: block; }
.quick-action-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,.12); }
.quick-action-icon { font-size: 36px; margin-bottom: 8px; }
.quick-action-title { font-size: 15px; font-weight: 700; color: #111827; }
.quick-action-desc { font-size: 12px; color: #5f6b7a; margin-top: 4px; }
</style>
