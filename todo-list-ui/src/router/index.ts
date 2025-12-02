import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('../views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/recycle',
      name: 'RecycleBin',
      component: () => import('../views/RecycleBin.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/logs/:id',
      name: 'LogView',
      component: () => import('../views/LogView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/teams',
      name: 'TeamManage',
      component: () => import('../views/TeamManage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/teams/:id/tasks',
      name: 'TeamTaskView',
      component: () => import('../views/TeamTaskView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/teams/:id/recycle',
      name: 'TeamRecycleBin',
      component: () => import('../views/TeamRecycleBin.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 全局导航守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = !!userStore.token

  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    if (isAuthenticated) {
      next()
    } else {
      // 未登录，重定向到登录页面
      next('/login')
    }
  } else {
    // 如果已登录且尝试访问登录/注册页面，重定向到首页
    if (isAuthenticated && (to.path === '/login' || to.path === '/register')) {
      next('/home')
    } else {
      next()
    }
  }
})

export default router
