<template>
  <div class="login-page-v2">
    <div class="login-shell">
      <div class="login-brand">
        <div class="brand-inner">
          <div class="brand-badge">NEWS</div>
          <h1>新闻发布<br>管理系统</h1>
          <p class="brand-desc">多角色协作 · 审核工作流 · 一键发布</p>
          <div class="brand-quote">
            <div class="quote-line"></div>
            <p>"新闻是历史的初稿，也是时代的镜像。"</p>
          </div>
        </div>
      </div>

      <div class="login-form-side">
        <div class="form-wrapper">
          <div class="login-tabs-v2">
            <button :class="['tab-btn-v2', { active: tab === 'login' }]" @click="tab = 'login'">登录</button>
            <button :class="['tab-btn-v2', { active: tab === 'register' }]" @click="tab = 'register'">注册</button>
          </div>

          <el-alert v-if="error" :title="error" type="error" show-icon closable @close="error = ''" style="margin-bottom:16px" />
          <el-alert v-if="success" :title="success" type="success" show-icon closable @close="success = ''" style="margin-bottom:16px" />

          <!-- Login Form -->
          <form v-show="tab === 'login'" @submit.prevent="handleLogin">
            <div style="margin-bottom:16px"><el-input v-model="loginForm.username" placeholder="用户名" size="large" /></div>
            <div style="margin-bottom:20px"><el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password /></div>
            <button type="submit" class="btn-login" :disabled="loading">{{ loading ? '登录中...' : '登 录' }}</button>
          </form>

          <!-- Register Form -->
          <form v-show="tab === 'register'" @submit.prevent="handleRegister">
            <div style="margin-bottom:16px"><el-input v-model="regForm.username" placeholder="用户名（2-50字符）" size="large" /></div>
            <div style="margin-bottom:16px"><el-input v-model="regForm.password" type="password" placeholder="密码（6-50位）" size="large" show-password /></div>
            <div style="margin-bottom:16px"><el-input v-model="regForm.email" placeholder="邮箱（可选，用于找回密码）" size="large" /></div>
            <div style="margin-bottom:16px"><el-input v-model="regForm.phone" placeholder="手机号（可选）" size="large" /></div>
            <div style="font-size:12px;color:#94a3b8;margin-bottom:20px;line-height:1.8">
              📌 注册后默认为<b>普通用户</b>角色，可浏览新闻、收藏点赞、投稿评论。<br>
              如需成为编辑或审核员，请联系管理员在后台分配。
            </div>
            <button type="submit" class="btn-login" :disabled="loading">{{ loading ? '注册中...' : '注 册' }}</button>
          </form>

          <div class="login-hint">测试账号: admin / admin123 &nbsp;|&nbsp; auditor / auditor123 &nbsp;|&nbsp; editor / editor123 &nbsp;|&nbsp; user / user123</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const tab = ref('login')
const error = ref('')
const success = ref('')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', email: '', phone: '' })

async function handleLogin() {
  loading.value = true; error.value = ''
  try {
    await userStore.login(loginForm)
    if (userStore.isUser) router.push('/')
    else router.push('/admin')
  } catch (e) { error.value = e.message || '登录失败' }
  finally { loading.value = false }
}

async function handleRegister() {
  loading.value = true; error.value = ''; success.value = ''
  try {
    await userStore.register(regForm)
    success.value = '注册成功！请登录（默认为普通用户）'
    tab.value = 'login'
    loginForm.username = regForm.username
  } catch (e) { error.value = e.message || '注册失败' }
  finally { loading.value = false }
}
</script>

<style scoped>
/* ===== v3 全新登录页设计 ===== */
.login-page-v2 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.login-shell {
  display: flex;
  width: 1000px;
  min-height: 580px;
  background: rgba(255,255,255,.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(0,0,0,.2);
}
/* Left brand panel */
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 40%, #4338ca 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
  position: relative;
  overflow: hidden;
}
.login-brand::before {
  content: '';
  position: absolute;
  top: -120px; right: -120px;
  width: 400px; height: 400px;
  border-radius: 50%;
  background: rgba(99,102,241,.15);
}
.login-brand::after {
  content: '';
  position: absolute;
  bottom: -80px; left: -80px;
  width: 280px; height: 280px;
  border-radius: 50%;
  background: rgba(139,92,246,.1);
}
.brand-inner { position: relative; z-index: 1; }
.brand-badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(255,255,255,.1);
  border: 1px solid rgba(255,255,255,.2);
  border-radius: 20px;
  font-size: 11px;
  letter-spacing: 4px;
  font-weight: 700;
  margin-bottom: 32px;
  backdrop-filter: blur(10px);
}
.login-brand h1 {
  font-size: 36px;
  font-weight: 800;
  line-height: 1.25;
  margin-bottom: 16px;
  letter-spacing: -.5px;
}
.brand-desc { font-size: 15px; color: rgba(255,255,255,.55); line-height: 1.7; }
.brand-quote { margin-top: 56px; }
.quote-line { width: 48px; height: 3px; background: linear-gradient(90deg, #818cf8, #a78bfa); border-radius: 2px; margin-bottom: 20px; }
.brand-quote p { font-size: 13px; color: rgba(255,255,255,.35); font-style: italic; line-height: 1.8; }
/* Right form panel */
.login-form-side { flex: 1; display: flex; align-items: center; justify-content: center; padding: 48px; background: #fff; }
.form-wrapper { width: 360px; }
.login-tabs-v2 { display: flex; gap: 8px; margin-bottom: 32px; background: #f1f5f9; border-radius: 12px; padding: 4px; }
.tab-btn-v2 { flex: 1; padding: 10px 16px; border: none; background: transparent; font-size: 14px; font-weight: 600; color: #64748b; cursor: pointer; border-radius: 10px; transition: all .2s; }
.tab-btn-v2.active { background: #fff; color: #1e293b; box-shadow: 0 1px 3px rgba(0,0,0,.1); }
.role-cards-v2 { display: flex; gap: 8px; margin-bottom: 24px; flex-wrap: wrap; }
.role-chip {
  padding: 6px 14px; border-radius: 10px; cursor: pointer;
  font-size: 12px; color: #64748b; background: #f1f5f9;
  border: 2px solid transparent; transition: all .15s; font-weight: 500;
}
.role-chip:hover { background: #eef2ff; color: #4f46e5; }
.role-chip input { display: none; }
.role-chip.checked { background: #eef2ff; border-color: #6366f1; color: #4f46e5; font-weight: 600; }
.btn-login {
  width: 100%; padding: 14px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; border: none; border-radius: 12px;
  font-size: 15px; font-weight: 700; cursor: pointer;
  transition: all .2s; letter-spacing: 1px;
  box-shadow: 0 4px 16px rgba(99,102,241,.3);
}
.btn-login:hover { transform: translateY(-1px); box-shadow: 0 8px 24px rgba(99,102,241,.4); }
.btn-login:disabled { opacity: .6; cursor: not-allowed; transform: none; }
.login-hint { text-align: center; margin-top: 24px; font-size: 11px; color: #94a3b8; line-height: 2; }

@media (max-width: 768px) {
  .login-shell { flex-direction: column; width: 100%; min-height: auto; }
  .login-brand { padding: 36px 28px; }
  .login-brand h1 { font-size: 24px; }
  .brand-quote { margin-top: 28px; }
  .login-form-side { padding: 28px 20px; }
  .form-wrapper { width: 100%; }
}
</style>
