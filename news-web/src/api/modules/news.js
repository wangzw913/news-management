import request from '@/api/request'

export default {
  list: (params) => request.get('/news', { params }),
  detail: (id) => request.get(`/news/${id}`),
  create: (data) => request.post('/news', data),
  update: (id, data) => request.put(`/news/${id}`, data),
  delete: (id) => request.delete(`/news/${id}`),
  submit: (id) => request.put(`/news/${id}/submit`),
  hot: (limit = 6) => request.get('/news/hot', { params: { limit } }),
  batchDelete: (ids) => request.post('/admin/news/batch-delete', ids)
}
