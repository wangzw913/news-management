<template>
  <div class="layout">
    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-brand">
        <div class="sidebar-logo">📰</div>
        <div class="sidebar-title">新闻管理系统</div>
      </div>
      <div class="sidebar-user">
        <div class="sidebar-avatar">{{ userStore.username?.charAt(0) }}</div>
        <div>
          <div class="sidebar-username">{{ userStore.username }}</div>
          <div class="sidebar-role">{{ userStore.roleName }}</div>
        </div>
      </div>
      <nav class="sidebar-nav">
        <template v-for="item in menuItems" :key="item.id">
          <div v-if="item.type === 'group'" class="nav-group">{{ item.name }}</div>
          <router-link v-else :to="item.path" class="nav-item" active-class="active">
            <span class="nav-icon">{{ item.icon }}</span>
            {{ item.name }}
            <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
          </router-link>
        </template>
      </nav>
      <div class="sidebar-footer">
        <a href="#" @click.prevent="handleLogout" class="nav-item">🚪 退出登录</a>
      </div>
    </aside>

    <button class="menu-toggle" @click="sidebarOpen = !sidebarOpen">☰</button>

    <main class="main-content">
      <header class="topbar">
        <div class="breadcrumb">{{ pageTitle }}</div>
        <div class="topbar-right">
          <span>{{ timeStr }}</span>
          <span>{{ userStore.username }} · {{ userStore.roleName }}</span>
        </div>
      </header>
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const sidebarOpen = ref(false)
const timeStr = ref('')
const pendingCount = ref(0)

let timer

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 10000)
  if (userStore.isAdmin || userStore.isAuditor) {
    request.get('/dashboard/stats').then(d => { pendingCount.value = d.pendingCount || 0 }).catch(() => {})
  }
})

onUnmounted(() => clearInterval(timer))

function updateTime() {
  const now = new Date()
  timeStr.value = now.toLocaleString('zh-CN')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

const menuItems = computed(() => {
  const r = userStore.role
  const items = []

  if (r === 'admin') {
    items.push(
      { id: 'dashboard', path: '/admin/dashboard', name: '仪表盘', icon: '📊' },
      { type: 'group', name: '内容管理' },
      { id: 'news_add', path: '/admin/news/create', name: '写稿', icon: '✏️' },
      { id: 'news_list', path: '/admin/news', name: '新闻管理', icon: '📝' },
      { id: 'audit', path: '/admin/audit', name: '审核队列', icon: '✅', badge: pendingCount.value || null },
      { type: 'group', name: '系统设置' },
      { id: 'users', path: '/admin/users', name: '用户管理', icon: '👥' },
      { id: 'categories', path: '/admin/categories', name: '分类管理', icon: '📂' },
      { id: 'slides', path: '/admin/slides', name: '轮播管理', icon: '🖼️' },
      { type: 'group', name: '其他' },
      { id: 'logs', path: '/admin/logs', name: '操作日志', icon: '📋' },
    )
  } else if (r === 'auditor') {
    items.push(
      { id: 'dashboard', path: '/admin/dashboard-auditor', name: '仪表盘', icon: '📊' },
      { id: 'audit', path: '/admin/audit', name: '审核队列', icon: '✅', badge: pendingCount.value || null },
      { id: 'news_list', path: '/admin/news', name: '新闻浏览', icon: '📝' },
    )
  } else if (r === 'editor') {
    items.push(
      { id: 'dashboard', path: '/admin/dashboard-editor', name: '仪表盘', icon: '📊' },
      { id: 'news_add', path: '/admin/news/create', name: '写稿', icon: '✏️' },
      { id: 'news_list', path: '/admin/news', name: '我的稿件', icon: '📝' },
    )
  }
  return items
})

const pageTitle = computed(() => {
  const item = menuItems.value.find(m => m.path === route.path)
  return item ? `${item.icon} ${item.name}` : ''
})
</script>

<style scoped>
.layout { display: flex; min-height: 100vh; }
/* ===== v3 深色侧边栏 ===== */
.sidebar { width: 240px; min-width: 240px; background: #0F172A; color: #94A3B8; display: flex; flex-direction: column; position: fixed; top: 0; left: 0; bottom: 0; z-index: 100; transition: transform .2s; }
.sidebar-brand { padding: 24px 20px; text-align: center; border-bottom: 1px solid rgba(255,255,255,.06); }
.sidebar-logo { font-size: 36px; margin-bottom: 8px; filter: drop-shadow(0 2px 8px rgba(99,102,241,.4)); }
.sidebar-title { font-size: 15px; font-weight: 700; color: #fff; letter-spacing: 1px; }
.sidebar-user { display: flex; align-items: center; gap: 12px; padding: 16px 20px; border-bottom: 1px solid rgba(255,255,255,.06); }
.sidebar-avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, #6366F1, #8B5CF6); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 15px; }
.sidebar-username { font-size: 13px; color: #fff; font-weight: 600; }
.sidebar-role { font-size: 11px; color: #64748B; }

.sidebar-nav { flex: 1; padding: 8px 0; overflow-y: auto; }
.nav-group { padding: 20px 20px 8px; font-size: 10px; font-weight: 700; color: rgba(255,255,255,.25); text-transform: uppercase; letter-spacing: 2.5px; }
.nav-item { display: flex; align-items: center; gap: 10px; padding: 10px 20px; color: #94A3B8; font-size: 13px; transition: all .15s; border-left: 3px solid transparent; text-decoration: none; margin: 2px 8px; border-radius: 0 8px 8px 0; }
.nav-item:hover { background: rgba(255,255,255,.04); color: #E2E8F0; }
.nav-item.active { background: rgba(99,102,241,.15); color: #A5B4FC; border-left-color: #6366F1; }
.nav-icon { font-size: 17px; width: 22px; text-align: center; }
.nav-badge { margin-left: auto; background: #EF4444; color: #fff; font-size: 10px; padding: 2px 7px; border-radius: 10px; font-weight: 700; }

.sidebar-footer { padding: 12px 20px; border-top: 1px solid rgba(255,255,255,.06); }

.main-content { margin-left: 240px; flex: 1; display: flex; flex-direction: column; }
.topbar { display: flex; align-items: center; justify-content: space-between; padding: 16px 28px; background: #fff; border-bottom: 1px solid #E2E8F0; position: sticky; top: 0; z-index: 50; }
.breadcrumb { font-size: 15px; font-weight: 700; color: #1E293B; }
.topbar-right { display: flex; align-items: center; gap: 20px; font-size: 12px; color: #64748B; }
.content { flex: 1; padding: 24px 28px; }
.menu-toggle { display: none; position: fixed; top: 12px; left: 12px; z-index: 200; background: #6366F1; color: #fff; border: none; width: 40px; height: 40px; border-radius: 10px; font-size: 20px; cursor: pointer; box-shadow: 0 4px 12px rgba(99,102,241,.3); }

@media (max-width: 768px) {
  .sidebar { transform: translateX(-100%); }
  .sidebar.open { transform: translateX(0); }
  .main-content { margin-left: 0; }
  .menu-toggle { display: block; }
}
</style>
