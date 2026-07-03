<template>
  <div class="portal-page">
    <header class="portal-header">
      <div class="portal-header-inner">
        <router-link to="/" class="portal-logo">📰 新闻门户</router-link>
        <nav class="portal-nav">
          <router-link to="/" exact-active-class="active">首页</router-link>
          <router-link v-for="cat in categories" :key="cat.categoryId"
            :to="`/?cat=${cat.categoryId}`" active-class="active">{{ cat.name }}</router-link>
        </nav>
        <div class="portal-header-right">
          <form class="portal-search" @submit.prevent="goSearch">
            <input v-model="keyword" placeholder="搜索新闻..." />
            <button type="submit">🔍</button>
          </form>
          <div class="portal-user-menu">
            <span class="portal-user-btn">👤 {{ userStore.username }} ▾</span>
            <div class="portal-dropdown">
              <router-link to="/profile">🏠 个人中心</router-link>
              <router-link to="/profile/favorites">⭐ 我的收藏</router-link>
              <router-link to="/profile/submissions">📝 我的投稿</router-link>
              <router-link to="/submit">✍️ 写稿投稿</router-link>
              <a href="#" @click.prevent="handleLogout">🚪 退出</a>
            </div>
          </div>
        </div>
      </div>
    </header>
    <main class="portal-main-content">
      <router-view />
    </main>
    <footer class="portal-footer">© 2026 新闻发布管理系统 v3 · Spring Boot + Vue 3</footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const categories = ref([])
const keyword = ref('')

onMounted(async () => {
  try { categories.value = await request.get('/categories') } catch {}
})

function goSearch() {
  if (keyword.value.trim()) {
    router.push(`/search?keyword=${encodeURIComponent(keyword.value.trim())}`)
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* ===== v3 门户导航 ===== */
.portal-page { background: #F1F5F9; min-height: 100vh; }
.portal-header { background: rgba(255,255,255,.8); backdrop-filter: blur(12px); border-bottom: 1px solid #E2E8F0; position: sticky; top: 0; z-index: 100; }
.portal-header-inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; padding: 0 24px; height: 60px; gap: 20px; }
.portal-logo { font-size: 18px; font-weight: 800; background: linear-gradient(135deg, #6366F1, #8B5CF6); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-decoration: none; white-space: nowrap; }
.portal-nav { display: flex; gap: 4px; flex: 1; }
.portal-nav a { padding: 8px 16px; border-radius: 8px; font-size: 13px; font-weight: 600; color: #64748B; text-decoration: none; transition: all .15s; }
.portal-nav a:hover, .portal-nav a.active { background: #EEF2FF; color: #6366F1; }
.portal-header-right { display: flex; align-items: center; gap: 16px; }
.portal-search { display: flex; }
.portal-search input { padding: 8px 16px; border: 1.5px solid #E2E8F0; border-radius: 10px 0 0 10px; font-size: 13px; width: 180px; outline: none; background: #F8FAFC; transition: all .2s; }
.portal-search input:focus { border-color: #6366F1; background: #fff; box-shadow: 0 0 0 3px rgba(99,102,241,.1); }
.portal-search button { padding: 8px 14px; background: #6366F1; color: #fff; border: none; border-radius: 0 10px 10px 0; cursor: pointer; font-size: 14px; }
.portal-user-menu { position: relative; }
.portal-user-btn { cursor: pointer; font-size: 13px; font-weight: 600; color: #334155; padding: 8px 14px; border-radius: 8px; background: #F1F5F9; }
.portal-user-btn:hover { background: #E2E8F0; }
.portal-dropdown { display: none; position: absolute; right: 0; top: 100%; background: #fff; border-radius: 12px; box-shadow: 0 12px 40px rgba(0,0,0,.12); min-width: 200px; padding: 8px; z-index: 200; }
.portal-user-menu:hover .portal-dropdown { display: block; }
.portal-dropdown a { display: block; padding: 10px 14px; border-radius: 8px; font-size: 13px; color: #334155; text-decoration: none; }
.portal-dropdown a:hover { background: #F1F5F9; }
.portal-main-content { max-width: 1200px; margin: 0 auto; padding: 24px; }
.portal-footer { text-align: center; padding: 32px; color: #94A3B8; font-size: 12px; border-top: 1px solid #E2E8F0; margin-top: 48px; }
</style>
