import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'

const DashboardView = () => import('@/views/DashboardView.vue')
const SectionView = () => import('@/views/SectionView.vue')
const GoalView = () => import('@/views/GoalView.vue')
const TaskView = () => import('@/views/TaskView.vue')
const RecordView = () => import('@/views/RecordView.vue')
const ProfileView = () => import('@/views/ProfileView.vue')
const AuthView = () => import('@/views/AuthView.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: AuthView, props: { mode: 'login' }, meta: { guestOnly: true } },
    { path: '/register', name: 'register', component: AuthView, props: { mode: 'register' }, meta: { guestOnly: true } },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'dashboard', component: DashboardView },
        { path: 'goals', name: 'goals', component: GoalView },
        { path: 'tasks', name: 'tasks', component: TaskView },
        { path: 'records', name: 'records', component: RecordView },
        {
          path: 'reviews',
          name: 'reviews',
          component: SectionView,
          props: { eyebrow: '回顾与调整', title: '智能复盘', description: '从计划与执行的差异中找到下一步改进方向。', action: '生成复盘' },
        },
        {
          path: 'library',
          name: 'library',
          component: SectionView,
          props: { eyebrow: '可靠的学习材料', title: '课程资料库', description: '整理课程资料，并基于可靠来源进行问答。', action: '添加资料' },
        },
        { path: 'profile', name: 'profile', component: ProfileView },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const hasToken = Boolean(localStorage.getItem('study_compass_token'))
  if (to.matched.some((record) => record.meta.requiresAuth) && !hasToken) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.meta.guestOnly && hasToken) return { name: 'dashboard' }
})

export default router
