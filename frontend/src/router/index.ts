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
      redirect: '/student/home',
      children: [
        { path: 'home', component: () => import('@/views/student/HomeView.vue'), meta: { title: '首页' } },
        { path: 'qualification', component: () => import('@/views/student/QualificationView.vue'), meta: { title: '个人信息' } },
        { path: 'payment', component: () => import('@/views/student/PaymentView.vue'), meta: { title: '缴费' } },
        { path: 'report', component: () => import('@/views/student/ReportView.vue'), meta: { title: '报到' } },
        { path: 'announcements', component: () => import('@/views/student/AnnouncementView.vue'), meta: { title: '通知' } },
        { path: 'announcements/:id', component: () => import('@/views/student/AnnouncementDetailView.vue'), meta: { title: '通知详情' } },
        { path: 'onboarding', redirect: '/student/report' },
        { path: 'profile', redirect: '/student/qualification' }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { role: 'ADMIN' },
      redirect: '/admin/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/admin/DashboardView.vue'), meta: { title: '数据看板' } },
        { path: 'academics', component: () => import('@/views/admin/AcademicsView.vue'), meta: { title: '学院管理' } },
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
    return auth.role === 'ADMIN' ? '/admin/dashboard' : '/student/home'
  }
  return true
})

export default router
