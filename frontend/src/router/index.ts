import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue')
  },
  {
    path: '/equipment',
    name: 'Equipment',
    component: () => import('../views/Equipment.vue')
  },
  {
    path: '/rider',
    name: 'Rider',
    component: () => import('../views/Rider.vue')
  },
  {
    path: '/station',
    name: 'Station',
    component: () => import('../views/Station.vue')
  },
  {
    path: '/level-summary',
    name: 'LevelSummary',
    component: () => import('../views/LevelSummary.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router