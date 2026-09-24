<script setup lang="ts">
import { NButton, NEmpty } from 'naive-ui'

defineProps<{
  eyebrow: string
  title: string
  description: string
  action: string
}>()
</script>

<template>
  <section class="section-view">
    <div class="hero-row compact">
      <div>
        <p class="section-note">{{ eyebrow }}</p>
        <h1>{{ title }}</h1>
        <p class="hero-copy">{{ description }}</p>
      </div>
      <n-button type="primary" size="large">{{ action }}</n-button>
    </div>
    <div class="panel empty-panel">
      <n-empty description="该模块将在对应实验阶段接入真实数据">
        <template #extra>
          <span class="muted-copy">页面结构与统一视觉规范已经就绪</span>
        </template>
      </n-empty>
    </div>
  </section>
</template>
