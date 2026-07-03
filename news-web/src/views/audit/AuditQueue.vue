<template>
  <div>
    <el-card v-for="n in list" :key="n.newsId" class="audit-card">
      <div class="audit-card-header">
        <div style="display:flex;justify-content:space-between;align-items:flex-start">
          <div>
            <h3 class="audit-title">{{ n.title }}</h3>
            <div class="audit-meta">
              <span>✍️ {{ n.authorName }}</span>
              <span>📂 {{ n.categoryName }}</span>
              <span>🕐 {{ n.createdAt }}</span>
            </div>
          </div>
          <el-button link type="primary" @click="toggleExpand(n.newsId)" class="expand-btn">
            {{ expandedId === n.newsId ? '▲ 收起全文' : '▼ 展开全文' }}
          </el-button>
        </div>
      </div>

      <!-- 折叠/展开全文 -->
      <div v-show="expandedId === n.newsId" class="audit-content-full">
        <div class="content-body" v-html="(n.content || '').replace(/\n/g, '<br>')" />
      </div>
      <div v-show="expandedId !== n.newsId" class="audit-content-preview" @click="toggleExpand(n.newsId)" style="cursor:pointer">
        {{ truncateContent(n.content, 200) }}
        <span v-if="(n.content || '').length > 200" style="color:#e85d3f;font-weight:600">…点击展开全文</span>
      </div>

      <div class="audit-actions">
        <el-button type="success" @click="approve(n.newsId)" :loading="approvingId === n.newsId">
          ✅ 审核通过
        </el-button>
        <el-button type="danger" @click="confirmReject(n.newsId)" :loading="rejectingId === n.newsId">
          ↩️ 退回
        </el-button>
        <el-input v-model="rejectReasons[n.newsId]" placeholder="退回原因（必填）" style="width:280px" size="small" />
        <el-button size="small" @click="openDetail(n.newsId)">🔗 在新窗口查看</el-button>
      </div>
    </el-card>

    <el-empty v-if="!loading && !list.length" description="暂无待审核稿件 🎉" />
    <div style="text-align:center;margin-top:16px">
      <el-pagination v-model:current-page="page" :total="total" layout="prev, pager, next" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const expandedId = ref(null)
const approvingId = ref(null)
const rejectingId = ref(null)
const rejectReasons = reactive({})

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/news', { params: { status: 'pending', page: page.value } })
    list.value = data.records; total.value = data.total
  } catch {} finally { loading.value = false }
}

function toggleExpand(id) {
  expandedId.value = expandedId.value === id ? null : id
}

function openDetail(id) {
  window.open(`/admin/news/${id}`, '_blank')
}

async function approve(id) {
  approvingId.value = id
  try { await request.put('/audit/approve', { ids: [id] }); ElMessage.success('审核通过'); expandedId.value = null; loadData() }
  catch {} finally { approvingId.value = null }
}

async function confirmReject(id) {
  if (!rejectReasons[id]) { ElMessage.warning('请填写退回原因'); return }
  try {
    await ElMessageBox.confirm('确认退回这篇稿件？作者将收到退回通知。', '确认退回', {
      confirmButtonText: '确认退回', cancelButtonText: '取消', type: 'warning'
    })
    await reject(id)
  } catch {}
}

async function reject(id) {
  rejectingId.value = id
  try { await request.put(`/audit/${id}/reject`, { reason: rejectReasons[id] }); ElMessage.success('已退回'); expandedId.value = null; loadData() }
  catch {} finally { rejectingId.value = null }
}

function truncateContent(text, len) {
  if (!text) return ''
  const clean = text.replace(/<[^>]+>/g, '')
  return clean.length > len ? clean.substring(0, len) : clean
}
</script>

<style scoped>
.audit-card { background: #fff; border-radius: 8px; padding: 20px; margin-bottom: 12px; border-left: 4px solid #f2c94c; }
.audit-card-header { margin-bottom: 10px; }
.audit-title { font-size: 18px; font-weight: 700; }
.audit-meta { display: flex; gap: 16px; margin-top: 4px; font-size: 12px; color: #5f6b7a; }
.expand-btn { font-size: 13px; white-space: nowrap; }
.audit-content-preview {
  font-size: 13px; color: #5f6b7a; background: #f6f6f8; padding: 12px;
  border-radius: 6px; margin: 10px 0; line-height: 1.6;
  max-height: 60px; overflow: hidden; transition: all .2s;
}
.audit-content-preview:hover { background: #edf2ff; }
.audit-content-full {
  background: #fafbfc; padding: 16px; border-radius: 6px; margin: 10px 0;
  border: 1px solid #e5e7eb;
}
.content-body { font-size: 15px; line-height: 2; color: #111827; white-space: pre-wrap; word-break: break-word; }
.audit-actions { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-top: 12px; }
</style>
