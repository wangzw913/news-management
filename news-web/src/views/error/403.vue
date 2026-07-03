<template>
  <div style="text-align:center;padding:100px 20px;max-width:500px;margin:0 auto">
    <div style="font-size:72px;margin-bottom:16px">🚫</div>
    <h2>403 权限不足</h2>
    <p style="color:#6b7280;margin-top:8px;line-height:1.8">
      你的当前角色是 <b>{{ userStore.roleName }}</b>，无权访问此页面。<br>
      如需切换角色，请重新登录。
    </p>
    <div style="margin-top:20px;display:flex;gap:12px;justify-content:center">
      <el-button type="primary" @click="goHome">返回首页</el-button>
      <el-button @click="userStore.logout(); $router.push('/login')">重新登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function goHome() {
  if (userStore.isUser) router.push('/')
  else router.push('/admin')
}
</script>
