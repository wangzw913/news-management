<template>
  <div>
    <div class="page-toolbar">
      <el-form :inline="true" :model="filters">
        <el-form-item><el-input v-model="filters.keyword" placeholder="搜索标题/内容" clearable @clear="search" /></el-form-item>
        <el-form-item><el-select v-model="filters.categoryId" placeholder="全部分类" clearable style="width:140px">
          <el-option v-for="c in categories" :key="c.categoryId" :label="c.name" :value="c.categoryId" />
        </el-select></el-form-item>
        <el-form-item><el-select v-model="filters.status" placeholder="全部状态" clearable style="width:120px">
          <el-option label="草稿" value="draft" /><el-option label="待审核" value="pending" />
          <el-option label="已发布" value="published" /><el-option label="已退回" value="rejected" />
        </el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">搜索</el-button><el-button @click="resetFilters">重置</el-button></el-form-item>
      </el-form>
      <div>
        <el-button v-if="userStore.role !== 'auditor'" type="primary" @click="$router.push('/admin/news/create')">+ 写稿</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" @selection-change="s => selected = s.map(r => r.newsId)">
        <el-table-column v-if="userStore.isAdmin" type="selection" width="40" />
        <el-table-column label="标题">
          <template #default="{ row }">
            <div><strong>{{ row.title }}</strong> <el-tag v-if="row.isTop" type="danger" size="small">置顶</el-tag></div>
            <div class="news-meta">{{ row.clicked }}次阅读</div>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="80" />
        <el-table-column prop="categoryName" label="分类" width="80" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            <div v-if="row.status==='rejected' && row.rejectReason" style="color:#dc2626;font-size:11px">{{ row.rejectReason }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/admin/news/${row.newsId}`)">查看</el-button>
            <el-button v-if="userStore.role !== 'auditor'" link type="primary" @click="$router.push(`/admin/news/${row.newsId}/edit`)">编辑</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.newsId)">
              <template #reference><el-button v-if="userStore.role !== 'auditor'" link type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;text-align:center">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
          layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const categories = ref([])
const selected = ref([])
const filters = reactive({ keyword: '', categoryId: null, status: '' })

onMounted(async () => {
  filters.status = route.query.status || ''
  try { categories.value = await request.get('/categories') } catch {}
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, ...filters }
    Object.keys(params).forEach(k => { if (!params[k]) delete params[k] })
    const data = await request.get('/news', { params })
    list.value = data.records; total.value = data.total
  } catch {} finally { loading.value = false }
}

function search() { page.value = 1; loadData() }
function resetFilters() { filters.keyword = ''; filters.categoryId = null; filters.status = ''; page.value = 1; loadData() }

async function handleDelete(id) {
  try { await request.delete(`/news/${id}`); ElMessage.success('删除成功'); loadData() } catch {}
}

function statusType(s) {
  const map = { draft: 'info', pending: 'warning', published: 'success', rejected: 'danger' }
  return map[s] || 'info'
}
function statusLabel(s) {
  const map = { draft: '草稿', pending: '待审核', published: '已发布', rejected: '已退回' }
  return map[s] || s
}
</script>

<style scoped>
.page-toolbar { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.news-meta { font-size: 12px; color: #98a2b3; margin-top: 2px; }
</style>
