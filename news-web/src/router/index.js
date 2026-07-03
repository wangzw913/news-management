import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  // 登录（唯一公开入口）
  { path: '/login', name: 'Login', component: () => import('@/views/login/LoginView.vue'), meta: { public: true } },

  // 错误页
  { path: '/403', component: () => import('@/views/error/403.vue'), meta: { public: true } },
  { path: '/404', component: () => import('@/views/error/404.vue'), meta: { public: true } },

  // 后台：Admin / Editor / Auditor
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: to => {
        const user = useUserStore()
        return user.isAdmin ? '/admin/dashboard' : user.isAuditor ? '/admin/dashboard-auditor' : '/admin/dashboard-editor'
      }},
      { path: 'dashboard', component: () => import('@/views/dashboard/AdminDashboard.vue'), meta: { roles: ['admin'] } },
      { path: 'dashboard-editor', component: () => import('@/views/dashboard/EditorDashboard.vue'), meta: { roles: ['editor'] } },
      { path: 'dashboard-auditor', component: () => import('@/views/dashboard/AuditorDashboard.vue'), meta: { roles: ['auditor'] } },
      { path: 'news', component: () => import('@/views/news/NewsList.vue'), meta: { roles: ['admin','editor','auditor'] } },
      { path: 'news/create', component: () => import('@/views/news/NewsEditor.vue'), meta: { roles: ['admin','editor'] } },
      { path: 'news/:id/edit', component: () => import('@/views/news/NewsEditor.vue'), meta: { roles: ['admin','editor'] } },
      { path: 'news/:id', component: () => import('@/views/news/NewsDetail.vue'), meta: { roles: ['admin','editor','auditor'] } },
      { path: 'audit', component: () => import('@/views/audit/AuditQueue.vue'), meta: { roles: ['admin','auditor'] } },
      { path: 'users', component: () => import('@/views/user/UserList.vue'), meta: { roles: ['admin'] } },
      { path: 'categories', component: () => import('@/views/category/CategoryManage.vue'), meta: { roles: ['admin'] } },
      { path: 'slides', component: () => import('@/views/slide/SlideManage.vue'), meta: { roles: ['admin'] } },
      { path: 'logs', component: () => import('@/views/log/OperationLog.vue'), meta: { roles: ['admin'] } },
    ]
  },

  // 用户门户
  {
    path: '/',
    component: () => import('@/layouts/PortalLayout.vue'),
    meta: { requiresAuth: true, roles: ['user'] },
    children: [
      { path: '', component: () => import('@/views/dashboard/UserPortal.vue') },
      { path: 'detail/:id', component: () => import('@/views/news/NewsDetail.vue') },
      { path: 'submit', component: () => import('@/views/news/NewsEditor.vue') },
      { path: 'profile', component: () => import('@/views/user/Profile.vue') },
      { path: 'profile/:tab', component: () => import('@/views/user/Profile.vue') },
      { path: 'search', component: () => import('@/views/search/SearchResult.vue') },
    ]
  },

  // 404 catch-all
  { path: '/:pathMatch(.*)*', redirect: '/404' },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 1. 公开页面直接放行
  if (to.meta.public) return next()

  // 2. 未登录 → 去登录页
  if (!userStore.isLoggedIn) {
    return next('/login')
  }

  // 3. 有 token 但无用户信息 → 先加载
  if (!userStore.username) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      userStore.logout()
      return next('/login')
    }
  }

  // 4. 已登录但访问登录页 → 按角色跳转
  if (to.path === '/login') {
    return next(userStore.isUser ? '/' : '/admin')
  }

  // 5. 角色不匹配 → 403 并提示当前角色
  if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    return next('/403')
  }

  next()
})

export default router
