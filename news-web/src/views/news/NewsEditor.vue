<template>
  <el-card>
    <template #header><h3>{{ isEdit ? '编辑稿件' : '写新稿件' }}</h3></template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="标题" prop="title"><el-input v-model="form.title" size="large" placeholder="请输入新闻标题" /></el-form-item>
      <el-form-item label="封面">
        <div style="display:flex;gap:16px;align-items:flex-start">
          <div v-if="form.coverImage" style="position:relative">
            <img :src="'/' + form.coverImage" style="width:200px;height:120px;object-fit:cover;border-radius:8px" />
            <el-button circle size="small" type="danger" style="position:absolute;top:-8px;right:-8px" @click="form.coverImage = ''">✕</el-button>
          </div>
          <div v-else class="cover-upload" @click="$refs.fileInput.click()" style="width:200px;height:120px">
            <div class="cover-placeholder">📷<br><span style="font-size:12px">点击上传封面</span></div>
          </div>
          <el-button v-if="form.coverImage" size="small" @click="$refs.fileInput.click()">更换封面</el-button>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleUpload" />
        </div>
        <div style="font-size:11px;color:#94a3b8;margin-top:4px">支持 JPG/PNG/GIF/WebP，不超过 5MB。留空则不显示封面。</div>
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="选择分类">
          <el-option v-for="c in categories" :key="c.categoryId" :label="c.name" :value="c.categoryId" />
        </el-select>
      </el-form-item>
      <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="2" placeholder="简要描述（可选）" /></el-form-item>
      <el-form-item label="内容"><div style="width:100%"><textarea v-model="form.content" class="editor-area" placeholder="请输入新闻正文内容..."></textarea></div></el-form-item>
      <el-form-item label="标签"><el-input v-model="form.tags" placeholder="逗号分隔，如：科技,AI,5G" /></el-form-item>
      <el-form-item label="来源"><el-input v-model="form.source" placeholder="新闻来源（可选）" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="success" @click="submitForAudit" :loading="submitting">提交审核</el-button>
        <el-button @click="goBack">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/admin/news')
}
const saving = ref(false)
const submitting = ref(false)
const categories = ref([])

const form = reactive({ title: '', categoryId: null, summary: '', content: '', tags: '', source: '', coverImage: '' })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

onMounted(async () => {
  try { categories.value = await request.get('/categories') } catch {}
  const id = route.params.id
  if (id) {
    isEdit.value = true
    try {
      const data = await request.get(`/news/${id}`)
      Object.assign(form, {
        title: data.title, categoryId: data.categoryId, summary: data.summary || '',
        content: data.content || '', tags: data.tags || '', source: data.source || '',
        coverImage: data.coverImage || ''
      })
    } catch {}
  }
})

async function handleUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('文件不能超过 5MB'); return }
  const fd = new FormData()
  fd.append('file', file)
  try {
    const { url } = await request.post('/upload/image', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    form.coverImage = url
    ElMessage.success('封面上传成功')
  } catch {}
}

async function saveDraft() {
  saving.value = true
  try {
    await saveNews()
    ElMessage.success('草稿已保存')
    router.push('/admin/news')
  } catch {} finally { saving.value = false }
}

async function submitForAudit() {
  if (!form.title || !form.categoryId) { ElMessage.warning('请填写标题和分类'); return }
  submitting.value = true
  try {
    const newsId = await saveNews()
    await request.put(`/news/${newsId}/submit`)
    ElMessage.success('已提交审核')
    router.push('/admin/news')
  } catch {} finally { submitting.value = false }
}

async function saveNews() {
  const payload = { ...form }
  if (isEdit.value) {
    await request.put(`/news/${route.params.id}`, payload)
    return route.params.id
  } else {
    const data = await request.post('/news', payload)
    return data.newsId
  }
}
</script>

<style scoped>
.editor-area { width: 100%; min-height: 350px; padding: 12px; border: 1.5px solid #E2E8F0; border-radius: 8px; font-size: 14px; line-height: 1.8; font-family: inherit; resize: vertical; outline: none; }
.editor-area:focus { border-color: #6366F1; box-shadow: 0 0 0 3px rgba(99,102,241,.1); }
.cover-upload { border: 2px dashed #E2E8F0; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all .2s; background: #F8FAFC; }
.cover-upload:hover { border-color: #6366F1; background: #EEF2FF; }
.cover-placeholder { text-align: center; color: #94A3B8; font-size: 20px; line-height: 1.5; }
</style>
