import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/components/Layout/AppLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
      },
      {
        path: 'curl-parser',
        name: 'CurlParser',
        component: () => import('@/views/CurlParser.vue'),
      },
      {
        path: 'templates',
        name: 'TemplateList',
        component: () => import('@/views/TemplateList.vue'),
      },
      {
        path: 'templates/:id',
        name: 'TemplateEdit',
        component: () => import('@/views/TemplateEdit.vue'),
      },
      {
        path: 'scenarios',
        name: 'ScenarioList',
        component: () => import('@/views/ScenarioList.vue'),
      },
      {
        path: 'scenarios/:id',
        name: 'ScenarioEdit',
        component: () => import('@/views/ScenarioEdit.vue'),
      },
      {
        path: 'monitor/:taskId',
        name: 'Monitor',
        component: () => import('@/views/Monitor.vue'),
      },
      {
        path: 'report/:taskId',
        name: 'Report',
        component: () => import('@/views/Report.vue'),
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
