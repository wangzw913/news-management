<template>
  <div>
    <h2 style="margin-bottom:20px">🔍 搜索：{{ keyword }}</h2>
    <div v-if="list.length">
      <div v-for="n in list" :key="n.newsId" class="news-card" @click="$router.push(`/detail/${n.newsId}`)">
        <div class="news-card-body">
          <span class="cat-tag-sm">{{ n.categoryName }}</span>
          <h4>{{ n.title }}</h4>
          <p class="news-card-desc">{{ truncate(n.summary || n.content, 100) }}</p>
        </div>
      </div>
    </div>
    <el-empty v-else description="未找到相关新闻" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'

const route = useRoute()
const keyword = ref('')
const list = ref([])

onMounted(async () => {
  keyword.value = route.query.keyword || ''
  if (keyword.value) {
    try { const data = await request.get('/news', { params: { keyword: keyword.value, status: 'published' } }); list.value = data.records || [] } catch {}
  }
})

function truncate(text, len) {
  if (!text) return ''
  return text.replace(/<[^>]+>/g, '').substring(0, len) + (text.length > len ? '...' : '')
}
</script>

<style scoped>
.news-card { background: #fff; border-radius: 8px; padding: 20px; margin-bottom: 12px; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,.08); border-left: 3px solid transparent; }
.news-card:hover { border-left-color: #e85d3f; box-shadow: 0 4px 12px rgba(0,0,0,.12); }
.cat-tag-sm { display: inline-block; padding: 1px 8px; border-radius: 10px; background: #ffe8e0; color: #e85d3f; font-size: 11px; font-weight: 600; }
.news-card h4 { font-size: 15px; margin: 6px 0; color: #111827; }
.news-card-desc { font-size: 13px; color: #6b7280; }
</style>
