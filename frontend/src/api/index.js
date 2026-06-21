import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)

// 认证
export const authAPI = {
  login: data => api.post('/auth/login', data),
  registerUser: data => api.post('/auth/register/user', data),
}

// 宠物
export const petAPI = {
  list: params => api.get('/pet/list', { params }),
  search: params => api.get('/pet/search', { params }),
  detail: id => api.get(`/pet/${id}`),
  create: data => api.post('/pet', data),
  delete: id => api.delete(`/pet/${id}`),
  restore: id => api.post(`/pet/${id}/restore`),
  carousel: () => api.get('/pet/carousel'),
}

// 领养
export const adoptionAPI = {
  apply: data => api.post('/adoption/apply', data),
  list: params => api.get('/adoption/list', { params }),
  my: (pageNum = 1, pageSize = 50) => api.get('/adoption/my', { params: { pageNum, pageSize } }),
  audit: (id, data) => api.put(`/adoption/${id}/audit`, data),
  complete: id => api.put(`/adoption/${id}/complete`),
}

// 社区
export const communityAPI = {
  postList: params => api.get('/community/post/list', { params }),
  postDetail: id => api.get(`/community/post/${id}`),
  createPost: data => api.post('/community/post', data),
  createComment: data => api.post('/community/comment', data),
  likePost: id => api.put(`/community/post/${id}/like`),
  reviewList: params => api.get('/community/post/review/list', { params }),
  reviewPost: (id, approved) => api.put(`/community/post/${id}/review`, { approved }),
  takeDownPost: id => api.put(`/community/post/${id}/take-down`),
}

// 寄养
export const fosterAPI = {
  searchService: params => api.get('/foster/service/search', { params }),
  serviceDetail: id => api.get(`/foster/service/${id}`),
  createService: data => api.post('/foster/service', data),
  updateService: (id, data) => api.put(`/foster/service/${id}`, data),
  deleteService: id => api.delete(`/foster/service/${id}`),
  createOrder: data => api.post('/foster/order', data),
  orderList: () => api.get('/foster/order/my'),
  cancelOrder: id => api.put(`/foster/order/${id}/cancel`),
  reviewOrder: (id, data) => api.put(`/foster/order/${id}/review`, data),
  shelterOrderList: params => api.get('/foster/order/list', { params }),
  confirmOrder: id => api.put(`/foster/order/${id}/confirm`),
  completeOrder: id => api.put(`/foster/order/${id}/complete`),
}

// 用户
export const userAPI = {
  info: () => api.get('/user/info'),
  update: data => api.put('/user/info', data),
  notifications: () => api.get('/user/notifications'),
}

// 通知
export const notificationAPI = {
  list: (userId, userType) => api.get('/notifications', { params: { userId, userType } }),
  unreadCount: (userId, userType) => api.get('/notifications/unread-count', { params: { userId, userType } }),
  markRead: id => api.put(`/notifications/${id}/read`),
  markAllRead: (userId, userType) => api.put('/notifications/read-all', null, { params: { userId, userType } }),
}

// 私信
export const messageAPI = {
  send: data => api.post('/message/send', data),
  conversation: (userId, userType, otherUserId, otherUserType) =>
    api.get(`/message/conversation/${otherUserId}/${otherUserType}`, { params: { userId, userType } }),
  conversations: (userId, userType) => api.get('/message/conversations', { params: { userId, userType } }),
  byAdoption: (userId, userType, adoptionId) =>
    api.get(`/message/adoption/${adoptionId}`, { params: { userId, userType } }),
  unreadCount: (userId, userType) => api.get('/message/unread', { params: { userId, userType } }),
  markRead: (userId, userType, fromUserId) =>
    api.put(`/message/read/${fromUserId}`, null, { params: { userId, userType } }),
}

// 收藏
export const petFavoriteAPI = {
  toggle: (userId, petId) => api.post('/pet-favorite/toggle', null, { params: { userId, petId } }),
  status: (userId, petId) => api.get('/pet-favorite/status', { params: { userId, petId } }),
  list: userId => api.get('/pet-favorite/list', { params: { userId } }),
  ids: userId => api.get('/pet-favorite/ids', { params: { userId } }),
}

// 回访
export const followupAPI = {
  create: data => api.post('/followup', data),
  byAdoption: adoptionId => api.get(`/followup/adoption/${adoptionId}`),
  byShelter: shelterId => api.get(`/followup/shelter/${shelterId}`),
}

// 举报
export const reportAPI = {
  create: data => api.post('/report', data),
  pending: () => api.get('/report/pending'),
  review: (id, status) => api.put(`/report/${id}/review`, { status }),
}

// 好友
export const friendAPI = {
  request: data => api.post('/friend/request', data),
  accept: (id, userId) => api.put(`/friend/accept/${id}`, null, { params: { userId } }),
  reject: (id, userId) => api.put(`/friend/reject/${id}`, null, { params: { userId } }),
  delete: (userId, friendId) => api.delete('/friend/delete', { params: { userId, friendId } }),
  list: (userId, userType) => api.get('/friend/list', { params: { userId, userType } }),
  pending: (userId, userType) => api.get('/friend/pending', { params: { userId, userType } }),
  check: (userId, friendId) => api.get('/friend/check', { params: { userId, friendId } }),
  search: (userId, keyword) => api.get('/friend/search', { params: { userId, keyword } }),
}

// 宠物推荐
export const recommendAPI = {
  get: userId => api.get('/pet/recommend', { params: { userId } }),
}

// 智能匹配
export const matchingAPI = {
  recommend: () => api.get('/matching/recommend'),
  getProfile: () => api.get('/matching/profile'),
  saveProfile: (data) => api.put('/matching/profile', data),
  questionnaire: () => api.get('/matching/questionnaire'),
}

// 信用体系
export const creditAPI = {
  logs: (params) => api.get('/credit/logs', { params }),
}

// 知识百科
export const wikiAPI = {
  categories: () => api.get('/wiki/categories'),
  entryDetail: id => api.get(`/wiki/entry/${id}`),
  entryList: params => api.get('/wiki/entry/list', { params }),
  createEntry: data => api.post('/wiki/entry', data),
  editEntry: (id, data) => api.put(`/wiki/entry/${id}`, data),
  reviewEntry: (id, approved) => api.put(`/wiki/entry/${id}/review`, { approved }),
  reviewList: params => api.get('/wiki/entry/review/list', { params }),
  markHelpful: id => api.put(`/wiki/entry/${id}/helpful`),
  helpfulStatus: id => api.get(`/wiki/entry/${id}/helpful-status`),
  revisions: id => api.get(`/wiki/entry/${id}/revisions`),
  myEntries: params => api.get('/wiki/entry/my/list', { params }),
  delistEntry: id => api.put(`/wiki/entry/${id}/delist`),
}

// 成长激励
export const growthAPI = {
  checkin: () => api.post('/growth/checkin'),
  myPoints: () => api.get('/growth/my-points'),
  myBadges: () => api.get('/growth/my-badges'),
  allBadges: () => api.get('/growth/badges'),
  pointsLog: params => api.get('/growth/points-log', { params }),
}

// 救助站主页
export const shelterProfileAPI = {
  profile: shelterId => api.get(`/shelter/${shelterId}/profile`),
  ranking: () => api.get('/shelter/ranking'),
}

export default api
