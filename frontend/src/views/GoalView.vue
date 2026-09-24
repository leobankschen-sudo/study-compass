<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { NButton, NDatePicker, NDrawer, NDrawerContent, NEmpty, NForm, NFormItem, NInput, NInputNumber, NPopconfirm, NSelect, NSpin, NTag, useMessage } from 'naive-ui'
import { createGoal, deleteGoal, getCurrentUserId, listGoals, updateGoal } from '@/api/study'
import type { GoalPayload, GoalStatus, LearningGoal } from '@/types/study'

const message = useMessage()
const loading = ref(true)
const saving = ref(false)
const drawerOpen = ref(false)
const editingId = ref<number | null>(null)
const goals = ref<LearningGoal[]>([])
const today = new Date()

const statusOptions = [
  { label: '进行中', value: 'ACTIVE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已归档', value: 'ARCHIVED' },
]
const statusLabel: Record<GoalStatus, string> = { ACTIVE: '进行中', COMPLETED: '已完成', ARCHIVED: '已归档' }
const statusType: Record<GoalStatus, 'info' | 'success' | 'default'> = { ACTIVE: 'info', COMPLETED: 'success', ARCHIVED: 'default' }

const form = reactive({
  title: '', description: '', startDate: Date.now(), targetDate: Date.now() + 30 * 86400000,
  weeklyHours: 8, currentLevel: '', status: 'ACTIVE' as GoalStatus,
})

const activeCount = computed(() => goals.value.filter((goal) => goal.status === 'ACTIVE').length)
const totalHours = computed(() => goals.value.filter((goal) => goal.status === 'ACTIVE').reduce((sum, goal) => sum + (goal.weeklyHours ?? 0), 0))

function isoDate(timestamp: number) {
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function localTimestamp(value: string) {
  return new Date(`${value}T00:00:00`).getTime()
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { title: '', description: '', startDate: Date.now(), targetDate: Date.now() + 30 * 86400000, weeklyHours: 8, currentLevel: '', status: 'ACTIVE' })
}

function openCreate() {
  resetForm()
  drawerOpen.value = true
}

function openEdit(goal: LearningGoal) {
  editingId.value = goal.id
  Object.assign(form, {
    title: goal.title, description: goal.description ?? '', startDate: localTimestamp(goal.startDate),
    targetDate: localTimestamp(goal.targetDate), weeklyHours: goal.weeklyHours ?? 8,
    currentLevel: goal.currentLevel ?? '', status: goal.status,
  })
  drawerOpen.value = true
}

async function load() {
  loading.value = true
  try { goals.value = await listGoals() } catch (error) { message.error((error as Error).message) } finally { loading.value = false }
}

async function submit() {
  if (!form.title.trim()) return message.warning('请填写目标名称')
  if (form.targetDate < form.startDate) return message.warning('截止日期不能早于开始日期')
  const payload: GoalPayload = {
    userId: getCurrentUserId(), title: form.title.trim(), description: form.description.trim(),
    startDate: isoDate(form.startDate), targetDate: isoDate(form.targetDate), weeklyHours: form.weeklyHours,
    currentLevel: form.currentLevel.trim(), status: form.status,
  }
  saving.value = true
  try {
    editingId.value ? await updateGoal(editingId.value, payload) : await createGoal(payload)
    message.success(editingId.value ? '目标已更新' : '目标已创建')
    drawerOpen.value = false
    await load()
  } catch (error) { message.error((error as Error).message) } finally { saving.value = false }
}

async function remove(id: number) {
  try { await deleteGoal(id); message.success('目标已删除'); await load() } catch (error) { message.error((error as Error).message) }
}

function daysLeft(date: string) {
  return Math.max(0, Math.ceil((localTimestamp(date) - today.getTime()) / 86400000))
}

onMounted(load)
</script>

<template>
  <section>
    <div class="hero-row compact">
      <div><p class="section-note">学习方向</p><h1>学习目标</h1><p class="hero-copy">把想学的内容变成有边界、有周期的阶段计划。</p></div>
      <n-button type="primary" size="large" @click="openCreate">新建目标</n-button>
    </div>

    <div class="summary-strip">
      <div><span>进行中的目标</span><strong>{{ activeCount }}</strong></div>
      <div><span>每周计划投入</span><strong>{{ totalHours }}<small>小时</small></strong></div>
      <p>目标不需要很多。保持一到两个主线，更容易形成稳定节奏。</p>
    </div>

    <n-spin :show="loading">
      <div v-if="goals.length" class="ledger-list">
        <article v-for="goal in goals" :key="goal.id" class="ledger-row goal-ledger">
          <div class="ledger-index">{{ String(goal.id).padStart(2, '0') }}</div>
          <div class="ledger-main">
            <div class="ledger-title"><h2>{{ goal.title }}</h2><n-tag :type="statusType[goal.status]" :bordered="false" size="small">{{ statusLabel[goal.status] }}</n-tag></div>
            <p>{{ goal.description || '暂未补充目标说明' }}</p>
            <div class="ledger-meta"><span>{{ goal.startDate }} — {{ goal.targetDate }}</span><span>每周 {{ goal.weeklyHours ?? 0 }} 小时</span><span v-if="goal.status === 'ACTIVE'">剩余 {{ daysLeft(goal.targetDate) }} 天</span></div>
          </div>
          <div class="row-actions">
            <n-button quaternary size="small" @click="openEdit(goal)">编辑</n-button>
            <n-popconfirm @positive-click="remove(goal.id)"><template #trigger><n-button quaternary size="small">删除</n-button></template>目标下存在任务时不能删除，仍要继续吗？</n-popconfirm>
          </div>
        </article>
      </div>
      <div v-else-if="!loading" class="panel empty-panel"><n-empty description="还没有学习目标"><template #extra><n-button type="primary" @click="openCreate">建立第一个目标</n-button></template></n-empty></div>
    </n-spin>

    <n-drawer v-model:show="drawerOpen" :width="480" placement="right">
      <n-drawer-content :title="editingId ? '编辑学习目标' : '新建学习目标'" closable>
        <n-form label-placement="top">
          <n-form-item label="目标名称" required><n-input v-model:value="form.title" maxlength="80" show-count placeholder="例如：系统掌握 Java Web 开发" /></n-form-item>
          <n-form-item label="目标说明"><n-input v-model:value="form.description" type="textarea" :rows="4" maxlength="1000" show-count placeholder="希望达到什么程度？为什么要学？" /></n-form-item>
          <div class="form-grid"><n-form-item label="开始日期" required><n-date-picker v-model:value="form.startDate" type="date" clearable /></n-form-item><n-form-item label="截止日期" required><n-date-picker v-model:value="form.targetDate" type="date" clearable /></n-form-item></div>
          <div class="form-grid"><n-form-item label="每周投入"><n-input-number v-model:value="form.weeklyHours" :min="1" :max="80"><template #suffix>小时</template></n-input-number></n-form-item><n-form-item label="状态"><n-select v-model:value="form.status" :options="statusOptions" /></n-form-item></div>
          <n-form-item label="当前基础"><n-input v-model:value="form.currentLevel" maxlength="200" placeholder="简要描述已有知识和薄弱点" /></n-form-item>
        </n-form>
        <template #footer><div class="drawer-actions"><n-button @click="drawerOpen = false">取消</n-button><n-button type="primary" :loading="saving" @click="submit">保存目标</n-button></div></template>
      </n-drawer-content>
    </n-drawer>
  </section>
</template>
