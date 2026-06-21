import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/home', name: 'Home', component: () => import('../views/Home.vue'), meta: { requiresAuth: true } },
  { path: '/pets', name: 'PetList', component: () => import('../views/PetList.vue'), meta: { requiresAuth: true } },
  { path: '/pet/:id', name: 'PetDetail', component: () => import('../views/PetDetail.vue'), meta: { requiresAuth: true } },
  { path: '/game', name: 'PetGame', component: () => import('../views/PetGame.vue'), meta: { requiresAuth: true } },
  { path: '/foster', name: 'Foster', component: () => import('../views/Foster.vue'), meta: { requiresAuth: true } },
  { path: '/community', name: 'Community', component: () => import('../views/Community.vue'), meta: { requiresAuth: true } },
  { path: '/community/post/:id', name: 'PostDetail', component: () => import('../views/PostDetail.vue'), meta: { requiresAuth: true } },
  { path: '/my', name: 'UserCenter', component: () => import('../views/UserCenter.vue'), meta: { requiresAuth: true } },
  { path: '/questionnaire', name: 'MatchingQuestionnaire', component: () => import('../views/MatchingQuestionnaire.vue'), meta: { requiresAuth: true } },
  { path: '/shelter/pets', name: 'ShelterPets', component: () => import('../views/ShelterPets.vue'), meta: { requiresAuth: true } },
  { path: '/shelter/adoptions', name: 'ShelterAdoptions', component: () => import('../views/ShelterAdoptions.vue'), meta: { requiresAuth: true } },
  { path: '/shelter/foster', name: 'ShelterFoster', component: () => import('../views/ShelterFoster.vue'), meta: { requiresAuth: true } },
  { path: '/shelter/posts', name: 'ShelterPosts', component: () => import('../views/ShelterPosts.vue'), meta: { requiresAuth: true } },

  { path: '/notifications', name: 'Notifications', component: () => import('../views/Notifications.vue'), meta: { requiresAuth: true } },
  { path: '/messages', name: 'Messages', component: () => import('../views/Messages.vue'), meta: { requiresAuth: true } },
  { path: '/favorites', name: 'FavoritePets', component: () => import('../views/FavoritePets.vue'), meta: { requiresAuth: true } },
  { path: '/followups', name: 'UserFollowups', component: () => import('../views/UserFollowups.vue'), meta: { requiresAuth: true } },
  { path: '/shelter/reports', name: 'ReportManagement', component: () => import('../views/ReportManagement.vue'), meta: { requiresAuth: true } },
  { path: '/shelter/wiki-audit', name: 'ShelterWikiAudit', component: () => import('../views/ShelterWikiAudit.vue'), meta: { requiresAuth: true } },
  { path: '/wiki', name: 'Wiki', component: () => import('../views/Wiki.vue'), meta: { requiresAuth: true } },
  { path: '/wiki/create', name: 'WikiCreate', component: () => import('../views/WikiEdit.vue'), meta: { requiresAuth: true } },
  { path: '/wiki/:id', name: 'WikiDetail', component: () => import('../views/WikiDetail.vue'), meta: { requiresAuth: true } },
  { path: '/wiki/:id/edit', name: 'WikiEdit', component: () => import('../views/WikiEdit.vue'), meta: { requiresAuth: true } },
  { path: '/profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { requiresAuth: true } },
  { path: '/shelter/:id', name: 'ShelterProfile', component: () => import('../views/ShelterProfile.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/home')
  } else if (to.path.startsWith('/shelter')) {
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    if (userInfo.userType !== 1) {
      next('/')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
