<script setup lang="ts">
import { RouterView, useRouter, useRoute } from 'vue-router'
import { ElMenu, ElMenuItem, ElContainer, ElHeader, ElAside, ElMain } from 'element-plus'
import { Monitor, Grid, User, Location, List, CircleCheck, EditPen } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const menuItems = [
  { path: '/', name: '仪表盘', icon: Monitor },
  { path: '/equipment', name: '障碍器材', icon: Grid },
  { path: '/rider', name: '骑手管理', icon: User },
  { path: '/station', name: '训练位管理', icon: Location },
  { path: '/recheck', name: '杆高复核', icon: CircleCheck },
  { path: '/review', name: '课后点评', icon: EditPen },
  { path: '/level-summary', name: '等级汇总', icon: List }
]

const handleMenuSelect = (index: string) => {
  router.push(index)
}
</script>

<template>
  <ElContainer class="app-container">
    <ElAside width="200" style="background: #2a3f5f;">
      <div class="logo-container">
        <div class="logo-icon">🐎</div>
        <div class="logo-text">马术俱乐部管理系统</div>
      </div>
      <ElMenu
        mode="vertical"
        :default-active="route.path"
        @select="handleMenuSelect"
        background-color="#2a3f5f"
        text-color="#fff"
        active-text-color="#409eff"
      >
        <ElMenuItem v-for="item in menuItems" :key="item.path" :index="item.path">
          <component :is="item.icon" :size="20" />
          <span>{{ item.name }}</span>
        </ElMenuItem>
      </ElMenu>
    </ElAside>
    <ElContainer>
      <ElHeader style="background: #fff; border-bottom: 1px solid #e6e6e6; padding: 0 20px;">
        <div class="header-title">{{ menuItems.find(i => i.path === route.path)?.name }}</div>
      </ElHeader>
      <ElMain style="padding: 20px; background: #f5f5f5;">
        <RouterView />
      </ElMain>
    </ElContainer>
  </ElContainer>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-container {
  height: 100vh;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #4a6181;
}

.logo-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  text-align: center;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  line-height: 60px;
}
</style>