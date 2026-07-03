<template>
  <div>
    <el-button link @click="goBack" style="margin-bottom:16px">← 返回</el-button>
    <div class="news-detail" v-loading="loading">
      <div class="detail-header">
        <el-tag>{{ detail.categoryName }}</el-tag>
        <h1>{{ detail.title }}</h1>
        <div class="detail-meta">
          <span>✍️ {{ detail.authorName }}</span><span>🕐 {{ detail.createdAt }}</span><span>👁 {{ detail.clicked }} 阅读</span>
        </div>
        <div class="detail-actions" style="margin-top:12px">
          <el-button :type="favorited ? 'warning' : 'default'" @click="toggleFavorite">⭐ {{ favorited ? '已收藏' : '收藏' }}</el-button>
          <el-button :type="liked ? 'primary' : 'default'" @click="toggleLike">👍 {{ liked ? '已点赞' : '点赞' }} ({{ likeCount }})</el-button>
        </div>
      </div>
      <div class="detail-content" v-html="detail.content?.replace(/\n/g,'<br>')" />

      <div v-if="detail.status === 'rejected' && detail.rejectReason" class="reject-box" style="margin-top:16px">
        <strong>退回原因：</strong>{{ detail.rejectReason }}
      </div>
    </div>

    <!-- Comments -->
    <div class="comment-section">
      <h3 style="margin-bottom:16px">💬 评论</h3>
      <div v-for="c in comments" :key="c.reviewId" class="comment">
        <div class="comment-avatar">{{ c.username?.charAt(0) }}</div>
        <div class="comment-body">
          <div class="comment-user">{{ c.username }} <span class="comment-time">{{ c.createdAt }}</span></div>
          <div class="comment-text">{{ c.content }}</div>
          <el-button link type="primary" size="small" @click="showReply(c.reviewId)">回复</el-button>
          <div v-for="r in c.replies" :key="r.reviewId" class="comment reply">
            <div class="comment-avatar">{{ r.username?.charAt(0) }}</div>
            <div class="comment-body">
              <div class="comment-user">{{ r.username }} <span class="comment-time">{{ r.createdAt }}</span></div>
              <div class="comment-text">{{ r.content }}</div>
            </div>
          </div>
          <div v-if="replyTo === c.reviewId" class="reply-form">
            <el-input v-model="replyContent" placeholder="写下回复..." size="small" />
            <el-button size="small" type="primary" @click="submitReply(c.reviewId)">回复</el-button>
            <el-button size="small" @click="replyTo = null">取消</el-button>
          </div>
        </div>
      </div>

      <div class="comment-form">
        <el-input v-model="newComment" type="textarea" :rows="3" placeholder="发表评论..." />
        <el-button type="primary" style="margin-top:8px" @click="submitComment">发表评论</el-button>
      </div>
    </div>

    <!-- Related Articles (TF-IDF content-based recommendation) -->
    <div v-if="relatedNews.length" class="related-section">
      <h3>📎 相关推荐 <span style="font-size:12px;color:#9ca3af;font-weight:400">（基于内容相似度）</span></h3>
      <div class="related-grid">
        <router-link v-for="r in relatedNews" :key="r.newsId" :to="detailLink(r.newsId)" class="related-card">
          <span class="cat-tag-sm">{{ r.categoryName }}</span>
          <h4>{{ r.title }}</h4>
          <div class="news-card-meta">
            <span>👁 {{ r.clicked }}</span><span>{{ r.authorName }}</span>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function detailLink(newsId) {
  // 判断当前在哪个布局下，生成正确路径
  if (route.path.startsWith('/admin')) return `/admin/news/${newsId}`
  return `/detail/${newsId}`
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    // 新标签页直接打开的，按角色跳回
    if (userStore.isUser) router.push('/')
    else router.push('/admin')
  }
}
const detail = ref({})
const comments = ref([])
const loading = ref(false)
const favorited = ref(false)
const liked = ref(false)
const likeCount = ref(0)
const newComment = ref('')
const replyTo = ref(null)
const replyContent = ref('')

onMounted(async () => {
  const id = route.params.id
  loading.value = true
  try {
    const [d, c] = await Promise.all([
      request.get(`/news/${id}`),
      request.get(`/reviews/news/${id}`)
    ])
    detail.value = d; comments.value = c; await loadRelated()
  } catch {} finally { loading.value = false }

  try {
    const [f, l, lc] = await Promise.all([
      request.get(`/favorites/check/${id}`),
      request.get(`/likes/check/${id}`),
      request.get(`/likes/count/${id}`)
    ])
    favorited.value = f.favorited; liked.value = l.liked; likeCount.value = lc.count
  } catch {}
})

async function toggleFavorite() {
  try { const r = await request.post(`/favorites/toggle/${route.params.id}`); favorited.value = r.favorited } catch {}
}
async function toggleLike() {
  try { const r = await request.post(`/likes/toggle/${route.params.id}`); liked.value = r.liked; likeCount.value += r.liked ? 1 : -1 } catch {}
}
const relatedNews = ref([])
async function loadRelated() {
  try { relatedNews.value = await request.get(`/recommend/similar/${route.params.id}?limit=5`) } catch {}
}
async function submitComment() {
  if (!newComment.value.trim()) return
  try {
    await request.post('/reviews', { newsId: Number(route.params.id), content: newComment.value })
    ElMessage.success('评论成功')
    newComment.value = ''
    const c = await request.get(`/reviews/news/${route.params.id}`)
    comments.value = c
  } catch {}
}
function showReply(id) { replyTo.value = id; replyContent.value = '' }
async function submitReply(parentId) {
  if (!replyContent.value.trim()) return
  try {
    await request.post('/reviews', { newsId: Number(route.params.id), parentId, content: replyContent.value })
    ElMessage.success('回复成功')
    replyTo.value = null
    const c = await request.get(`/reviews/news/${route.params.id}`)
    comments.value = c
  } catch {}
}
</script>

<style scoped>
.news-detail { background: #fff; border-radius: 8px; padding: 32px; box-shadow: 0 1px 3px rgba(0,0,0,.08); margin-bottom: 20px; }
.detail-header { margin-bottom: 24px; padding-bottom: 20px; border-bottom: 2px solid #e5e7eb; }
.detail-header h1 { font-size: 24px; line-height: 1.4; margin: 12px 0; }
.detail-meta { display: flex; gap: 20px; font-size: 13px; color: #6b7280; }
.detail-content { font-size: 15px; line-height: 2; color: #111827; }
.comment-section { background: #fff; border-radius: 8px; padding: 24px 32px; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
.comment { display: flex; gap: 12px; padding: 14px 0; border-bottom: 1px solid #e5e7eb; }
.comment.reply { margin-left: 48px; border-bottom: none; padding: 10px 0 0; }
.comment-avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg,#e85d3f,#7c3aed); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; flex-shrink: 0; }
.comment-body { flex: 1; }
.comment-user { font-size: 13px; font-weight: 600; }
.comment-time { font-size: 11px; color: #9ca3af; font-weight: 400; margin-left: 8px; }
.comment-text { font-size: 14px; margin-top: 4px; color: #111827; line-height: 1.6; }
.comment-form { margin-top: 20px; padding-top: 16px; border-top: 2px solid #e5e7eb; }
.reply-form { margin-top: 10px; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.detail-actions { display: flex; gap: 8px; }
.reject-box { padding: 10px; background: #fdeded; border-radius: 6px; color: #dc2626; font-size: 13px; }
.related-section { margin: 40px 0 20px; }
.related-section h3 { margin-bottom: 12px; font-size: 16px; }
.related-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
.related-card { background: #fff; border-radius: 8px; padding: 16px; box-shadow: 0 1px 3px rgba(0,0,0,.08); text-decoration: none; display: block; transition: all .15s; }
.related-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,.12); transform: translateY(-2px); }
.related-card h4 { font-size: 14px; margin: 8px 0 4px; color: #111827; }
.cat-tag-sm { display: inline-block; padding: 1px 8px; border-radius: 10px; background: #ffe8e0; color: #e85d3f; font-size: 11px; font-weight: 600; }
.news-card-meta { display: flex; gap: 12px; font-size: 12px; color: #9ca3af; }
</style>
