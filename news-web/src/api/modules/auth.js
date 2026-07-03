import request from '@/api/request'

export default {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  refresh: (refreshToken) => request.post('/auth/refresh', { refreshToken }),
  logout: (refreshToken) => request.post('/auth/logout', { refreshToken }),
  me: () => request.get('/auth/me')
}
