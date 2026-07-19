import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'
import StudentLayout from '@/layout/StudentLayout.vue'
import AdminLayout from '@/layout/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: LoginView },
    {
      path: '/student',
      component: StudentLayout,
      meta: { role: 'STUDENT' },
      redirect: '/student/onboarding',
      children: [
        { path: 'onboarding', component: () => import('@/views/student/OnboardingView.vue'), meta: { title: '报到流程' } },
        { path: 'profile', component: () => import('@/views/student/ProfileView.vue'), meta: { title: '个人中心' } },
        { path: 'announcements', component: () => import('@/views/student/AnnouncementView.vue'), meta: { title: '公告通知' } }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { role: 'ADMIN' },
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/admin/DashboardView.vue'), meta: { title: '数据看板' } },
        { path: 'students', component: () => import('@/views/admin/StudentsView.vue'), meta: { title: '学生管理' } },
        { path: 'dorms', component: () => import('@/views/admin/DormsView.vue'), meta: { title: '宿舍管理' } },
        { path: 'fees', component: () => import('@/views/admin/FeesView.vue'), meta: { title: '缴费项目' } },
        { path: 'modifications', component: () => import('@/views/admin/ModificationsView.vue'), meta: { title: '资格修改审核' } },
        { path: 'announcements', component: () => import('@/views/admin/AnnouncementsView.vue'), meta: { title: '公告管理' } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path === '/login') {
    return true
  }
  const requiredRole = to.matched.find((record) => record.meta.role)?.meta.role
  if (!auth.token) {
    return '/login'
  }
  if (requiredRole && auth.role !== requiredRole) {
    return auth.role === 'ADMIN' ? '/admin/dashboard' : '/student/onboarding'
  }
  return true
})

export default router
