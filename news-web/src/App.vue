<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
if (userStore.accessToken) {
  userStore.fetchUserInfo().catch(() => userStore.logout())
}
</script>
