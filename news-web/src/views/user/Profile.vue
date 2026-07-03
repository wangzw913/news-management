<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-avatar-lg">{{ userStore.username?.charAt(0) }}</div>
      <div><h2>{{ userStore.username }}</h2><p>{{ userStore.roleName }}</p></div>
    </div>

    <div class="profile-tabs">
      <router-link v-for="t in tabs" :key="t.key" :to="`/profile/${t.key}`"
        :class="['profile-tab', { active: activeTab === t.key }]">{{ t.label }}</router-link>
    </div>

    <div class="profile-content">
      <!-- Favorites -->
      <div v-if="activeTab === 'favorites'">
        <div v-for="item in dataList" :key="item.newsId" class="profile-item">
          <router-link :to="`/detail/${item.newsId}`" class="profile-item-title">{{ item.title }}</router-link>
          <span class="profile-item-meta">{{ item.categoryName }} · {{ item.createdAt }}</span>
        </div>
        <el-empty v-if="!dataList.length" description="暂无收藏" />
      </div>

      <!-- Likes -->
      <div v-if="activeTab === 'likes'">
        <div v-for="item in dataList" :key="item.newsId" class="profile-item">
          <router-link :to="`/detail/${item.newsId}`" class="profile-item-title">{{ item.title }}</router-link>
          <span class="profile-item-meta">{{ item.categoryName }} · {{ item.createdAt }}</span>
        </div>
        <el-empty v-if="!dataList.length" description="暂无点赞" />
      </div>

      <!-- History -->
      <div v-if="activeTab === 'history'">
        <div v-for="item in dataList" :key="item.newsId" class="profile-item">
          <router-link :to="`/detail/${item.newsId}`" class="profile-item-title">{{ item.title }}</router-link>
          <span class="profile-item-meta">{{ item.categoryName }} · {{ item.createdAt }}</span>
        </div>
        <el-empty v-if="!dataList.length" description="暂无浏览历史" />
      </div>

      <!-- Submissions -->
      <div v-if="activeTab === 'submissions'">
        <div v-for="item in dataList" :key="item.newsId" class="profile-item">
          <router-link :to="`/detail/${item.newsId}`" class="profile-item-title">{{ item.title }}</router-link>
          <div>
            <el-tag :type="item.status==='published'?'success':item.status==='rejected'?'danger':item.status==='pending'?'warning':'info'" size="small">{{ item.status }}</el-tag>
            <span class="profile-item-meta">{{ item.createdAt }}</span>
          </div>
        </div>
        <el-empty v-if="!dataList.length" description="暂无投稿" />
      </div>

      <!-- Change Password -->
      <div v-if="activeTab === 'password'">
        <el-form :model="pwForm" label-width="80px" class="profile-form">
          <el-form-item label="原密码"><el-input v-model="pwForm.oldPassword" type="password" /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwForm.newPassword" type="password" /></el-form-item>
          <el-form-item><el-button type="primary" @click="changePassword">修改密码</el-button></el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('favorites')
const dataList = ref([])
const pwForm = ref({ oldPassword: '', newPassword: '' })

const tabs = [
  { key: 'favorites', label: '⭐ 我的收藏' },
  { key: 'likes', label: '👍 我的点赞' },
  { key: 'history', label: '👁 浏览历史' },
  { key: 'submissions', label: '📝 我的投稿' },
  { key: 'password', label: '🔒 修改密码' },
]

watch(() => route.params.tab, loadTab, { immediate: true })
onMounted(() => { if (!route.params.tab) activeTab.value = 'favorites' })

function loadTab() {
  activeTab.value = route.params.tab || 'favorites'
  if (activeTab.value === 'password') return
  loadData()
}

async function loadData() {
  try {
    const endpoints = {
      favorites: '/favorites',
      likes: '/user/profile/likes',
      history: '/user/profile/history',
      submissions: '/user/profile/submissions',
    }
    const ep = endpoints[activeTab.value]
    if (!ep) return
    dataList.value = (await request.get(ep)).records || []
  } catch {}
}

async function changePassword() {
  try { await request.put('/user/password', pwForm.value); ElMessage.success('密码修改成功'); pwForm.value = { oldPassword: '', newPassword: '' } } catch {}
}
</script>

<style scoped>
.profile-container { max-width: 800px; margin: 0 auto; }
.profile-header { display: flex; align-items: center; gap: 20px; background: #fff; border-radius: 8px; padding: 24px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
.profile-avatar-lg { width: 64px; height: 64px; border-radius: 50%; background: linear-gradient(135deg,#e85d3f,#7c3aed); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 800; }
.profile-header h2 { font-size: 20px; margin-bottom: 4px; }
.profile-header p { font-size: 13px; color: #6b7280; }
.profile-tabs { display: flex; gap: 4px; margin-bottom: 16px; flex-wrap: wrap; }
.profile-tab { padding: 8px 16px; border-radius: 6px; font-size: 13px; font-weight: 600; color: #6b7280; text-decoration: none; background: #fff; border: 1.5px solid #e5e7eb; }
.profile-tab.active { background: #e85d3f; color: #fff; border-color: #e85d3f; }
.profile-content { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
.profile-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #e5e7eb; }
.profile-item:last-child { border-bottom: none; }
.profile-item-title { font-size: 14px; font-weight: 600; color: #111827; text-decoration: none; }
.profile-item-title:hover { color: #e85d3f; }
.profile-item-meta { font-size: 12px; color: #9ca3af; margin-left: 8px; }
.profile-form { max-width: 400px; }
</style>
