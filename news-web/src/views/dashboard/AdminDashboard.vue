<template>
  <div>
    <div class="stats-row">
      <router-link to="/admin/news" class="stat-card"><div class="stat-icon">📰</div><div class="stat-num">{{ stats.totalNews }}</div><div class="stat-label">总稿件</div></router-link>
      <router-link to="/admin/audit" class="stat-card stat-yellow"><div class="stat-icon">🕐</div><div class="stat-num">{{ stats.pendingCount }}</div><div class="stat-label">待审核</div></router-link>
      <router-link to="/admin/news?status=published" class="stat-card stat-green"><div class="stat-icon">📅</div><div class="stat-num">{{ stats.todayCount }}</div><div class="stat-label">今日发布</div></router-link>
      <router-link to="/admin/users" class="stat-card stat-purple"><div class="stat-icon">👥</div><div class="stat-num">{{ stats.totalUsers }}</div><div class="stat-label">总用户</div></router-link>
    </div>

    <div class="card-row">
      <div class="card flex-2">
        <div class="card-title">📈 近7天发布趋势</div>
        <div style="height:200px"><v-chart :option="chartOption" autoresize /></div>
      </div>
      <div class="card flex-1">
        <div class="card-title">🔥 热门新闻 Top5</div>
        <ol class="hot-list" v-if="hotNews.length">
          <li v-for="(h, i) in hotNews" :key="i">{{ h.title }} <span>{{ h.clicked }}次阅读</span></li>
        </ol>
        <p v-else class="empty-text">暂无数据</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

const stats = reactive({ totalNews: 0, pendingCount: 0, todayCount: 0, totalUsers: 0 })
const hotNews = ref([])
const chartOption = ref({})

onMounted(async () => {
  try {
    const [s, t, h] = await Promise.all([
      request.get('/dashboard/stats'),
      request.get('/dashboard/trend'),
      request.get('/news/hot', { params: { limit: 5 } })
    ])
    Object.assign(stats, s)
    hotNews.value = h

    chartOption.value = {
      tooltip: {},
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: t.map(d => d.date) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: t.map(d => d.count), itemStyle: { color: '#e85d3f' }, barMaxWidth: 30 }]
    }
  } catch {}
})
</script>

<style scoped>
.stats-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 24px; }
.stat-card {
  background: #fff; border-radius: 16px; padding: 24px; text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,.04); text-decoration: none; color: inherit; display: block;
  transition: all .2s; position: relative; overflow: hidden;
}
.stat-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; }
.stat-card:nth-child(1)::before { background: linear-gradient(90deg, #6366F1, #8B5CF6); }
.stat-card:nth-child(2)::before { background: linear-gradient(90deg, #F59E0B, #FCD34D); }
.stat-card:nth-child(3)::before { background: linear-gradient(90deg, #10B981, #34D399); }
.stat-card:nth-child(4)::before { background: linear-gradient(90deg, #8B5CF6, #A78BFA); }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,.08); }
.stat-icon { font-size: 32px; margin-bottom: 12px; }
.stat-num { font-size: 36px; font-weight: 800; color: #1E293B; }
.stat-label { font-size: 13px; color: #64748B; margin-top: 6px; font-weight: 500; }
.card-row { display: flex; gap: 20px; }
.card { background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,.04); margin-bottom: 20px; }
.card-title { font-size: 15px; font-weight: 700; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid #F1F5F9; color: #1E293B; }
.flex-1 { flex: 1; } .flex-2 { flex: 2; }
.hot-list { padding-left: 20px; }
.hot-list li { padding: 8px 0; font-size: 13px; border-bottom: 1px solid #F1F5F9; display: flex; justify-content: space-between; color: #334155; }
.hot-list li span { font-size: 11px; color: #94A3B8; }
.empty-text { text-align: center; padding: 48px; color: #94A3B8; }
@media (max-width: 768px) { .card-row { flex-direction: column; } }
</style>
