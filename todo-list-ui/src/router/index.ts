import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
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
      path: '/',
      name: 'Home',
      component: () => import('../views/Home.vue')
    },
    {
      path: '/recycle',
      name: 'RecycleBin',
      component: () => import('../views/RecycleBin.vue')
    },
    {
      path: '/logs/:id',
      name: 'LogView',
      component: () => import('../views/LogView.vue')
    }
  ]
})

export default router
