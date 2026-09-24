<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { NButton, NDatePicker, NDrawer, NDrawerContent, NEmpty, NForm, NFormItem, NInput, NInputNumber, NPopconfirm, NSelect, NSpin, NTag, useMessage } from 'naive-ui'
import { createTask, deleteTask, getCurrentUserId, listGoals, listTasks, updateTask } from '@/api/study'
import type { LearningGoal, LearningTask, TaskPayload, TaskPriority, TaskStatus } from '@/types/study'

const message = useMessage()
const loading = ref(true)
const saving = ref(false)
const drawerOpen = ref(false)
const editingId = ref<number | null>(null)
const statusFilter = ref<'ALL' | TaskStatus>('ALL')
const tasks = ref<LearningTask[]>([])
const goals = ref<LearningGoal[]>([])

const statusOptions = [{ label: '待开始', value: 'TODO' }, { label: '进行中', value: 'IN_PROGRESS' }, { label: '已完成', value: 'COMPLETED' }]
const filterOptions: { label: string; value: 'ALL' | TaskStatus }[] = [{ label: '全部', value: 'ALL' }, ...statusOptions as { label: string; value: TaskStatus }[]]
const priorityOptions = [{ label: '低', value: 'LOW' }, { label: '中', value: 'MEDIUM' }, { label: '高', value: 'HIGH' }]
const statusLabel: Record<TaskStatus, string> = { TODO: '待开始', IN_PROGRESS: '进行中', COMPLETED: '已完成' }
const priorityLabel: Record<TaskPriority, string> = { LOW: '低优先级', MEDIUM: '中优先级', HIGH: '高优先级' }
const priorityType: Record<TaskPriority, 'default' | 'warning' | 'error'> = { LOW: 'default', MEDIUM: 'warning', HIGH: 'error' }

const form = reactive({ goalId: null as number | null, title: '', description: '', priority: 'MEDIUM' as TaskPriority, status: 'TODO' as TaskStatus, dueDate: null as number | null, estimatedMinutes: 45 })
const filteredTasks = computed(() => statusFilter.value === 'ALL' ? tasks.value : tasks.value.filter((task) => task.status === statusFilter.value))
const goalOptions = computed(() => goals.value.map((goal) => ({ label: goal.title, value: goal.id })))

function goalName(id: number) { return goals.value.find((goal) => goal.id === id)?.title ?? `目标 #${id}` }
function setStatusFilter(value: 'ALL' | TaskStatus) { statusFilter.value = value }
function isoDate(timestamp: number | null) {
  if (!timestamp) return null
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
function localTimestamp(value: string | null) { return value ? new Date(`${value}T00:00:00`).getTime() : null }
function resetForm() { editingId.value = null; Object.assign(form, { goalId: goals.value[0]?.id ?? null, title: '', description: '', priority: 'MEDIUM', status: 'TODO', dueDate: null, estimatedMinutes: 45 }) }
function openCreate() { if (!goals.value.length) return message.warning('请先创建一个学习目标'); resetForm(); drawerOpen.value = true }
function openEdit(task: LearningTask) { editingId.value = task.id; Object.assign(form, { goalId: task.goalId, title: task.title, description: task.description ?? '', priority: task.priority, status: task.status, dueDate: localTimestamp(task.dueDate), estimatedMinutes: task.estimatedMinutes ?? 45 }); drawerOpen.value = true }

async function load() {
  loading.value = true
  try { [goals.value, tasks.value] = await Promise.all([listGoals(), listTasks()]) } catch (error) { message.error((error as Error).message) } finally { loading.value = false }
}
async function submit() {
  if (!form.goalId) return message.warning('请选择所属目标')
  if (!form.title.trim()) return message.warning('请填写任务名称')
  const payload: TaskPayload = { goalId: form.goalId, userId: getCurrentUserId(), title: form.title.trim(), description: form.description.trim(), priority: form.priority, status: form.status, dueDate: isoDate(form.dueDate), estimatedMinutes: form.estimatedMinutes, sortOrder: 0, aiGenerated: false }
  saving.value = true
  try { editingId.value ? await updateTask(editingId.value, payload) : await createTask(payload); message.success(editingId.value ? '任务已更新' : '任务已创建'); drawerOpen.value = false; await load() } catch (error) { message.error((error as Error).message) } finally { saving.value = false }
}
async function changeStatus(task: LearningTask, status: TaskStatus) {
  const payload: TaskPayload = { goalId: task.goalId, userId: task.userId, title: task.title, description: task.description ?? '', priority: task.priority, status, dueDate: task.dueDate, estimatedMinutes: task.estimatedMinutes ?? 45, sortOrder: task.sortOrder ?? 0, aiGenerated: task.aiGenerated }
  try { await updateTask(task.id, payload); await load() } catch (error) { message.error((error as Error).message) }
}
async function remove(id: number) { try { await deleteTask(id); message.success('任务已删除'); await load() } catch (error) { message.error((error as Error).message) } }
onMounted(load)
</script>

<template>
  <section>
    <div class="hero-row compact">
      <div><p class="section-note">执行清单</p><h1>任务计划</h1><p class="hero-copy">拆分目标，安排下一步，不让计划停留在一句话里。</p></div>
      <n-button type="primary" size="large" @click="openCreate">新建任务</n-button>
    </div>
    <div class="task-toolbar">
      <div class="filter-tabs" aria-label="任务状态筛选"><button v-for="item in filterOptions" :key="item.value" type="button" :class="{ active: statusFilter === item.value }" @click="setStatusFilter(item.value)">{{ item.label }}<span>{{ item.value === 'ALL' ? tasks.length : tasks.filter(task => task.status === item.value).length }}</span></button></div>
      <p>{{ filteredTasks.reduce((sum, task) => sum + (task.estimatedMinutes ?? 0), 0) }} 分钟计划量</p>
    </div>
    <n-spin :show="loading">
      <div v-if="filteredTasks.length" class="ledger-list">
        <article v-for="task in filteredTasks" :key="task.id" :class="['ledger-row', 'task-ledger', { done: task.status === 'COMPLETED' }]">
          <button class="status-dot" type="button" :aria-label="task.status === 'COMPLETED' ? '标记为待开始' : '标记为已完成'" @click="changeStatus(task, task.status === 'COMPLETED' ? 'TODO' : 'COMPLETED')"><span>✓</span></button>
          <div class="ledger-main"><div class="ledger-title"><h2>{{ task.title }}</h2><n-tag :type="priorityType[task.priority]" :bordered="false" size="small">{{ priorityLabel[task.priority] }}</n-tag></div><p>{{ task.description || goalName(task.goalId) }}</p><div class="ledger-meta"><span>{{ goalName(task.goalId) }}</span><span>{{ statusLabel[task.status] }}</span><span>{{ task.estimatedMinutes ?? 0 }} 分钟</span><span>{{ task.dueDate ? `截止 ${task.dueDate}` : '未设截止日期' }}</span></div></div>
          <div class="row-actions"><n-button quaternary size="small" @click="openEdit(task)">编辑</n-button><n-popconfirm @positive-click="remove(task.id)"><template #trigger><n-button quaternary size="small">删除</n-button></template>确认删除这项任务？</n-popconfirm></div>
        </article>
      </div>
      <div v-else-if="!loading" class="panel empty-panel"><n-empty description="当前筛选下没有任务"><template #extra><n-button type="primary" @click="openCreate">添加一项任务</n-button></template></n-empty></div>
    </n-spin>

    <n-drawer v-model:show="drawerOpen" :width="480" placement="right"><n-drawer-content :title="editingId ? '编辑任务' : '新建任务'" closable><n-form label-placement="top"><n-form-item label="所属目标" required><n-select v-model:value="form.goalId" :options="goalOptions" /></n-form-item><n-form-item label="任务名称" required><n-input v-model:value="form.title" maxlength="120" show-count placeholder="描述一个可以明确完成的动作" /></n-form-item><n-form-item label="任务说明"><n-input v-model:value="form.description" type="textarea" :rows="3" maxlength="1000" show-count /></n-form-item><div class="form-grid"><n-form-item label="优先级"><n-select v-model:value="form.priority" :options="priorityOptions" /></n-form-item><n-form-item label="状态"><n-select v-model:value="form.status" :options="statusOptions" /></n-form-item></div><div class="form-grid"><n-form-item label="截止日期"><n-date-picker v-model:value="form.dueDate" type="date" clearable /></n-form-item><n-form-item label="预计用时"><n-input-number v-model:value="form.estimatedMinutes" :min="5" :max="1440"><template #suffix>分钟</template></n-input-number></n-form-item></div></n-form><template #footer><div class="drawer-actions"><n-button @click="drawerOpen = false">取消</n-button><n-button type="primary" :loading="saving" @click="submit">保存任务</n-button></div></template></n-drawer-content></n-drawer>
  </section>
</template>
