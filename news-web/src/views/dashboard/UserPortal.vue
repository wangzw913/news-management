<template>
  <div>
    <!-- Hero Slider -->
    <div class="hero-section" v-if="heroItems.length">
      <el-carousel :interval="4000" height="280px" indicator-position="none" arrow="hover">
        <el-carousel-item v-for="(item, idx) in heroItems" :key="idx">
          <router-link :to="`/detail/${item.newsId}`" style="text-decoration:none">
            <div class="hero-bg" :style="{ background: `linear-gradient(135deg,${heroColors[idx%3][0]},${heroColors[idx%3][1]})` }">
              <div class="hero-overlay">
                <span class="hero-cat">{{ item.categoryName }}</span>
                <h2>{{ item.title }}</h2>
                <p>{{ truncate(item.summary || item.content, 120) }}</p>
              </div>
            </div>
          </router-link>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- Category Nav -->
    <div class="cat-nav">
      <router-link to="/" :class="{ active: !currentCat }" class="cat-nav-item">🔥 全部</router-link>
      <router-link v-for="cat in categories" :key="cat.categoryId" :to="`/?cat=${cat.categoryId}`"
        :class="{ active: currentCat == cat.categoryId }" class="cat-nav-item">{{ cat.name }}</router-link>
    </div>

    <!-- Main Content Row -->
    <div class="portal-row">
      <div class="portal-main">
        <div v-for="n in newsList" :key="n.newsId" class="news-card" @click="$router.push(`/detail/${n.newsId}`)">
          <div class="news-card-cover" :style="{ background: `linear-gradient(135deg,${gradients[n.categoryId%4][0]},${gradients[n.categoryId%4][1]})` }">
            <span class="cover-icon">{{ icons[n.categoryId] || '📰' }}</span>
          </div>
          <div class="news-card-body">
            <span class="cat-tag-sm">{{ n.categoryName }}</span>
            <h4>{{ n.title }}</h4>
            <p class="news-card-desc">{{ truncate(n.summary || n.content, 80) }}</p>
            <div class="news-card-meta">
              <span>{{ formatDate(n.createdAt) }}</span><span>👁 {{ n.clicked }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!newsList.length" description="暂无新闻" />
      </div>

      <div class="portal-sidebar">
        <router-link to="/submit" class="side-submit-card">
          <div class="submit-icon">✍️</div>
          <div class="submit-text"><strong>我要投稿</strong><span>分享你的见解</span></div>
          <div class="submit-arrow">→</div>
        </router-link>

        <div class="side-card">
          <div class="side-card-title">👤 我的</div>
          <router-link to="/profile/favorites" class="side-link">⭐ 我的收藏 <b>{{ profileStats.favorites || 0 }}</b></router-link>
          <router-link to="/profile/likes" class="side-link">👍 我的点赞 <b>{{ profileStats.likes || 0 }}</b></router-link>
          <router-link to="/profile/history" class="side-link">👁 浏览历史</router-link>
        </div>

        <div class="side-card">
          <div class="side-card-title">🔥 热门排行</div>
          <router-link v-for="(h, i) in hotNews" :key="h.newsId" :to="`/detail/${h.newsId}`" class="hot-item">
            <span :class="['hot-rank', `rank-${i+1}`]">{{ i+1 }}</span>
            <div><div class="hot-title">{{ h.title }}</div><div class="hot-meta">{{ h.clicked }} 阅读</div></div>
          </router-link>
        </div>

        <div class="side-card">
          <div class="side-card-title">📂 分类</div>
          <router-link v-for="cat in categories" :key="cat.categoryId" :to="`/?cat=${cat.categoryId}`" class="side-link">
            {{ cat.name }}
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'

const route = useRoute()
const categories = ref([])
const newsList = ref([])
const hotNews = ref([])
const heroItems = ref([])
const currentCat = ref(null)
const profileStats = ref({})
const catCounts = ref({})

const heroColors = [['#4F46E5','#7C3AED'],['#059669','#10B981'],['#D97706','#DC2626']]
const gradients = [['#EEF2FF','#E0E7FF'],['#ECFDF5','#D1FAE5'],['#FFFBEB','#FEF3C7'],['#FEF2F2','#FEE2E4']]
const icons = { 1:'🏀', 2:'📈', 3:'💻', 4:'📚', 5:'🏛️', 6:'🎓', 7:'🏠' }

onMounted(() => loadData())
watch(() => route.query.cat, () => { currentCat.value = route.query.cat || null; loadNews() })

async function loadData() {
  currentCat.value = route.query.cat || null
  try {
    const [cats, hot, profile] = await Promise.all([
      request.get('/categories'),
      request.get('/news/hot', { params: { limit: 6 } }),
      request.get('/user/profile/stats')
    ])
    categories.value = cats
    hotNews.value = hot
    profileStats.value = profile
    heroItems.value = hot.slice(0, 3)
    // 如果有置顶新闻，优先用置顶新闻做轮播
    try {
      const pinned = await request.get('/news', { params: { status: 'published', isTop: true, size: 5 } })
      if (pinned.records?.length) heroItems.value = pinned.records
    } catch {}
    await loadNews()
  } catch {}
}

async function loadNews() {
  try {
    const params = { status: 'published', size: 12 }
    if (currentCat.value) params.categoryId = currentCat.value
    const data = await request.get('/news', { params })
    newsList.value = data.records || []
  } catch {}
}

function truncate(text, len) {
  if (!text) return ''
  return text.replace(/<[^>]+>/g, '').substring(0, len) + (text.length > len ? '...' : '')
}

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  return `${date.getMonth()+1}-${String(date.getDate()).padStart(2,'0')}`
}
</script>

<style scoped>
.hero-section { margin-bottom: 20px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,.1); }
.hero-bg { min-height: 280px; display: flex; align-items: flex-end; padding: 40px; }
.hero-overlay { color: #fff; max-width: 600px; }
.hero-overlay h2 { font-size: 26px; margin: 10px 0; line-height: 1.3; text-shadow: 0 2px 8px rgba(0,0,0,.3); }
.hero-overlay p { font-size: 14px; opacity: .9; line-height: 1.6; }
.hero-cat { display: inline-block; padding: 3px 14px; background: rgba(255,255,255,.25); border-radius: 20px; font-size: 12px; font-weight: 600; }
.cat-nav { display: flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap; }
.cat-nav-item { padding: 6px 16px; border-radius: 20px; background: #fff; color: #6b7280; font-size: 13px; font-weight: 600; border: 1.5px solid #e5e7eb; text-decoration: none; }
.cat-nav-item.active, .cat-nav-item:hover { background: #e85d3f; color: #fff; border-color: #e85d3f; }
.portal-row { display: flex; gap: 24px; }
.portal-main { flex: 1; min-width: 0; }
.portal-sidebar { width: 280px; flex-shrink: 0; }
.news-card { display: flex; gap: 16px; background: #fff; border-radius: 8px; padding: 20px; margin-bottom: 12px; box-shadow: 0 1px 3px rgba(0,0,0,.08); cursor: pointer; border-left: 3px solid transparent; }
.news-card:hover { border-left-color: #e85d3f; box-shadow: 0 4px 12px rgba(0,0,0,.12); }
.news-card-cover { width: 80px; height: 80px; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.cover-icon { font-size: 32px; }
.news-card-body { flex: 1; }
.news-card h4 { font-size: 15px; margin: 6px 0; color: #111827; }
.news-card-desc { font-size: 13px; color: #6b7280; margin: 6px 0; }
.news-card-meta { display: flex; gap: 16px; font-size: 12px; color: #9ca3af; }
.cat-tag-sm { display: inline-block; padding: 1px 8px; border-radius: 10px; background: #ffe8e0; color: #e85d3f; font-size: 11px; font-weight: 600; }
.side-card { background: #fff; border-radius: 8px; padding: 16px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
.side-card-title { font-size: 14px; font-weight: 700; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 2px solid #e5e7eb; }
.side-link { display: block; padding: 6px 0; font-size: 13px; color: #6b7280; text-decoration: none; }
.side-link:hover { color: #e85d3f; }
.side-link b { color: #111827; float: right; }
.hot-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid #e5e7eb; text-decoration: none; }
.hot-item:hover { color: #e85d3f; }
.hot-rank { width: 24px; height: 24px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 800; color: #fff; flex-shrink: 0; }
.rank-1 { background: linear-gradient(135deg,#ef4444,#f97316); }
.rank-2 { background: linear-gradient(135deg,#f97316,#f59e0b); }
.rank-3 { background: linear-gradient(135deg,#eab308,#84cc16); }
.rank-4, .rank-5, .rank-6 { background: #9ca3af; }
.hot-title { font-size: 13px; color: #111827; font-weight: 500; line-height: 1.4; }
.hot-meta { font-size: 11px; color: #9ca3af; margin-top: 2px; }
.side-submit-card { display: flex; align-items: center; gap: 12px; background: linear-gradient(135deg,#e85d3f,#d14b2f); color: #fff; padding: 16px; border-radius: 8px; margin-bottom: 16px; text-decoration: none; box-shadow: 0 2px 12px rgba(232,93,63,.3); }
.side-submit-card:hover { transform: translateY(-2px); color: #fff; }
.submit-icon { font-size: 28px; }
.submit-text strong { display: block; font-size: 14px; }
.submit-text span { font-size: 11px; opacity: .8; }
.submit-arrow { font-size: 20px; margin-left: auto; opacity: .7; }

@media (max-width: 768px) {
  .portal-row { flex-direction: column; }
  .portal-sidebar { width: 100%; }
}
</style>
