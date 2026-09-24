<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NEmpty, NProgress, NSpin, NTag, useMessage } from 'naive-ui'
import { listGoals, listRecords, listTasks } from '@/api/study'
import type { LearningGoal, LearningTask, StudyRecord } from '@/types/study'

const router = useRouter()
const message = useMessage()
const loading = ref(true)
const goals = ref<LearningGoal[]>([])
const tasks = ref<LearningTask[]>([])
const records = ref<StudyRecord[]>([])
const today = new Date()
const todayKey = toDateKey(today)
const monday = new Date(today)
monday.setDate(today.getDate() - ((today.getDay() + 6) % 7))
monday.setHours(0, 0, 0, 0)

function toDateKey(date: Date) { return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}` }
function goalName(id: number) { return goals.value.find((goal) => goal.id === id)?.title ?? '未命名目标' }
function daysLeft(date: string) { return Math.max(0, Math.ceil((new Date(`${date}T00:00:00`).getTime() - today.getTime()) / 86400000)) }

const dateLabel = computed(() => new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(today))
const todayTasks = computed(() => tasks.value.filter((task) => task.dueDate === todayKey && task.status !== 'COMPLETED'))
const plannedMinutes = computed(() => todayTasks.value.reduce((sum, task) => sum + (task.estimatedMinutes ?? 0), 0))
const completedRate = computed(() => tasks.value.length ? Math.round(tasks.value.filter((task) => task.status === 'COMPLETED').length / tasks.value.length * 100) : 0)
const weekMinutes = computed(() => records.value.filter((record) => new Date(`${record.studyDate}T00:00:00`) >= monday).reduce((sum, record) => sum + record.durationMinutes, 0))
const overdueCount = computed(() => tasks.value.filter((task) => task.status !== 'COMPLETED' && task.dueDate && task.dueDate < todayKey).length)
const activeGoal = computed(() => goals.value.find((goal) => goal.status === 'ACTIVE'))
const activeGoalTasks = computed(() => activeGoal.value ? tasks.value.filter((task) => task.goalId === activeGoal.value?.id) : [])
const activeGoalCompleted = computed(() => activeGoalTasks.value.filter((task) => task.status === 'COMPLETED').length)
const activeGoalProgress = computed(() => activeGoalTasks.value.length ? Math.round(activeGoalCompleted.value / activeGoalTasks.value.length * 100) : 0)
const streakDays = computed(() => {
  const dates = new Set(records.value.map((record) => record.studyDate))
  const cursor = new Date(today)
  if (!dates.has(toDateKey(cursor))) cursor.setDate(cursor.getDate() - 1)
  let streak = 0
  while (dates.has(toDateKey(cursor))) { streak += 1; cursor.setDate(cursor.getDate() - 1) }
  return streak
})
const weekDays = computed(() => {
  const totals = records.value.reduce<Record<string, number>>((map, record) => { map[record.studyDate] = (map[record.studyDate] ?? 0) + record.durationMinutes; return map }, {})
  const maximum = Math.max(60, ...Object.values(totals))
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(monday); date.setDate(monday.getDate() + index)
    const minutes = totals[toDateKey(date)] ?? 0
    return { label: ['一', '二', '三', '四', '五', '六', '日'][index], minutes, value: Math.max(4, minutes / maximum * 100) }
  })
})
const recentTasks = computed(() => todayTasks.value.length ? todayTasks.value.slice(0, 3) : tasks.value.filter((task) => task.status !== 'COMPLETED').slice(0, 3))

async function load() {
  loading.value = true
  try { [goals.value, tasks.value, records.value] = await Promise.all([listGoals(), listTasks(), listRecords()]) } catch (error) { message.error((error as Error).message) } finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <n-spin :show="loading">
    <section class="dashboard">
      <div class="hero-row">
        <div><p class="section-note">{{ dateLabel }}</p><h1>晚上好，林同学</h1><p class="hero-copy">今天还有 {{ todayTasks.length }} 项计划，预计需要 {{ Math.floor(plannedMinutes / 60) }} 小时 {{ plannedMinutes % 60 }} 分钟。</p></div>
        <n-button type="primary" size="large" @click="router.push({ name: 'records' })">记录一次学习</n-button>
      </div>

      <div class="metric-grid">
        <article class="metric-card primary-metric"><span>任务完成率</span><strong>{{ completedRate }}%</strong><small>共 {{ tasks.length }} 项任务</small></article>
        <article class="metric-card"><span>本周投入</span><strong>{{ (weekMinutes / 60).toFixed(1) }}<em>小时</em></strong><small>{{ records.length }} 条学习记录</small></article>
        <article class="metric-card"><span>连续学习</span><strong>{{ streakDays }}<em>天</em></strong><small>依据实际记录计算</small></article>
        <article class="metric-card"><span>逾期任务</span><strong>{{ overdueCount }}<em>项</em></strong><small>{{ overdueCount ? '需要重新安排' : '当前节奏正常' }}</small></article>
      </div>

      <div class="dashboard-grid">
        <article class="panel task-panel">
          <header class="panel-header"><div><span class="panel-kicker">{{ todayTasks.length ? '今天截止' : '下一步' }}</span><h2>近期计划</h2></div><button class="text-link" type="button" @click="router.push({ name: 'tasks' })">查看全部</button></header>
          <div v-if="recentTasks.length" class="task-list"><div v-for="task in recentTasks" :key="task.id" class="task-row"><time>{{ task.dueDate?.slice(5) ?? '待定' }}</time><div class="task-indicator"></div><div class="task-copy"><strong>{{ task.title }}</strong><span>{{ goalName(task.goalId) }} · {{ task.estimatedMinutes ?? 0 }}分钟</span></div><n-tag :bordered="false" round size="small">{{ task.status === 'IN_PROGRESS' ? '进行中' : '待开始' }}</n-tag></div></div>
          <n-empty v-else size="small" description="暂无待处理任务" />
        </article>

        <article class="panel progress-panel">
          <header class="panel-header"><div><span class="panel-kicker">当前主线</span><h2>目标进度</h2></div></header>
          <template v-if="activeGoal"><div class="goal-title"><strong>{{ activeGoal.title }}</strong><span>剩余 {{ daysLeft(activeGoal.targetDate) }} 天</span></div><n-progress type="line" :percentage="activeGoalProgress" :height="8" :show-indicator="false" color="#315c67" rail-color="#e8ebe7" /><div class="goal-stats"><span>已完成 {{ activeGoalCompleted }} / {{ activeGoalTasks.length }} 项任务</span><b>{{ activeGoalProgress }}%</b></div><p class="goal-note">每周计划投入 {{ activeGoal.weeklyHours ?? 0 }} 小时。完成记录后，首页会自动更新节奏数据。</p></template>
          <n-empty v-else size="small" description="暂无进行中的目标" />
        </article>

        <article class="panel chart-panel"><header class="panel-header"><div><span class="panel-kicker">投入分布</span><h2>本周学习节奏</h2></div><span class="chart-unit">分钟</span></header><div class="bar-chart" aria-label="本周学习时长柱状图"><div v-for="day in weekDays" :key="day.label" class="bar-column" :title="`${day.minutes} 分钟`"><div class="bar-track"><div class="bar-value" :style="{ height: `${day.value}%` }"></div></div><span>{{ day.label }}</span></div></div></article>

        <article class="panel review-panel"><header class="panel-header"><div><span class="panel-kicker">数据提示</span><h2>本周观察</h2></div><span class="review-date">实时统计</span></header><blockquote v-if="records.length">本周已累计投入 {{ weekMinutes }} 分钟。继续记录学习内容与专注度，智能复盘将以这些真实数据生成建议。</blockquote><blockquote v-else>完成一次学习后记录时长、内容和专注度，这里会逐步形成你的学习节奏。</blockquote><button class="text-link" type="button" @click="router.push({ name: 'records' })">查看学习记录</button></article>
      </div>
    </section>
  </n-spin>
</template>
