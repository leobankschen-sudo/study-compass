<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NAvatar, NButton, NDropdown } from 'naive-ui'
import type { DropdownOption } from 'naive-ui'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const navigation = [
  { label: '首页', name: 'dashboard' },
  { label: '学习目标', name: 'goals' },
  { label: '任务计划', name: 'tasks' },
  { label: '学习记录', name: 'records' },
  { label: '智能复盘', name: 'reviews' },
  { label: '资料库', name: 'library' },
]

const activeName = computed(() => route.name)
const profileOptions: DropdownOption[] = [
  { label: '个人中心', key: 'profile' },
  { label: '退出登录', key: 'logout' },
]
const navigationOptions: DropdownOption[] = navigation.map((item) => ({ label: item.label, key: item.name }))

function handleProfile(key: string | number) {
  if (key === 'profile') router.push({ name: 'profile' })
  if (key === 'logout') {
    userStore.logout()
    router.replace({ name: 'login' })
  }
}

function handleNavigation(key: string | number) {
  router.push({ name: String(key) })
}

onMounted(() => userStore.load())
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <button class="brand" type="button" aria-label="返回首页" @click="router.push({ name: 'dashboard' })">
        <span class="brand-mark">序</span>
        <span class="brand-copy">
          <strong>知序</strong>
          <small>Study Compass</small>
        </span>
      </button>

      <nav class="main-nav" aria-label="主要导航">
        <button
          v-for="item in navigation"
          :key="item.name"
          type="button"
          :class="['nav-item', { active: activeName === item.name }]"
          @click="router.push({ name: item.name })"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="topbar-actions">
        <n-dropdown class="mobile-navigation" :options="navigationOptions" trigger="click" @select="handleNavigation">
          <n-button quaternary class="mobile-nav-button">选择页面</n-button>
        </n-dropdown>
        <n-dropdown :options="profileOptions" trigger="click" @select="handleProfile">
          <button class="profile-trigger" type="button">
            <n-avatar round :size="34" color="#315c67">{{ userStore.avatarText }}</n-avatar>
            <span class="profile-name">{{ userStore.displayName }}</span>
            <span class="chevron">⌄</span>
          </button>
        </n-dropdown>
      </div>
    </header>

    <main class="page-container">
      <router-view />
    </main>
  </div>
</template>
