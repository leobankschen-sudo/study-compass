<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { NButton, NDatePicker, NDrawer, NDrawerContent, NEmpty, NForm, NFormItem, NInput, NInputNumber, NPopconfirm, NRate, NSelect, NSpin, useMessage } from 'naive-ui'
import { createRecord, deleteRecord, getCurrentUserId, listGoals, listRecords, listTasks, updateRecord } from '@/api/study'
import type { LearningGoal, LearningTask, StudyRecord, StudyRecordPayload } from '@/types/study'

const message = useMessage()
const loading = ref(true)
const saving = ref(false)
const drawerOpen = ref(false)
const editingId = ref<number | null>(null)
const selectedGoalId = ref<number | 'ALL'>('ALL')
const records = ref<StudyRecord[]>([])
const goals = ref<LearningGoal[]>([])
const tasks = ref<LearningTask[]>([])

const form = reactive({ goalId: null as number | null, taskId: null as number | null, studyDate: Date.now(), durationMinutes: 45, content: '', reflection: '', focusScore: 4 })
const goalOptions = computed(() => goals.value.map((goal) => ({ label: goal.title, value: goal.id })))
const taskOptions = computed(() => tasks.value.filter((task) => task.goalId === form.goalId).map((task) => ({ label: task.title, value: task.id })))
const filterOptions = computed(() => [{ label: '全部目标', value: 'ALL' }, ...goalOptions.value])
const visibleRecords = computed(() => selectedGoalId.value === 'ALL' ? records.value : records.value.filter((record) => record.goalId === selectedGoalId.value))

function dateString(timestamp: number) {
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
function dateTimestamp(value: string) { return new Date(`${value}T00:00:00`).getTime() }
function goalName(id: number) { return goals.value.find((goal) => goal.id === id)?.title ?? `目标 #${id}` }
function taskName(id: number | null) { return id ? tasks.value.find((task) => task.id === id)?.title : null }
function resetForm() { editingId.value = null; Object.assign(form, { goalId: goals.value[0]?.id ?? null, taskId: null, studyDate: Date.now(), durationMinutes: 45, content: '', reflection: '', focusScore: 4 }) }
function openCreate() { if (!goals.value.length) return message.warning('请先创建一个学习目标'); resetForm(); drawerOpen.value = true }
function openEdit(record: StudyRecord) { editingId.value = record.id; Object.assign(form, { goalId: record.goalId, taskId: record.taskId, studyDate: dateTimestamp(record.studyDate), durationMinutes: record.durationMinutes, content: record.content, reflection: record.reflection ?? '', focusScore: record.focusScore ?? 4 }); drawerOpen.value = true }

async function load() {
  loading.value = true
  try { [goals.value, tasks.value, records.value] = await Promise.all([listGoals(), listTasks(), listRecords()]) } catch (error) { message.error((error as Error).message) } finally { loading.value = false }
}
async function submit() {
  if (!form.goalId) return message.warning('请选择学习目标')
  if (!form.content.trim()) return message.warning('请填写本次学习内容')
  const payload: StudyRecordPayload = { userId: getCurrentUserId(), goalId: form.goalId, taskId: form.taskId, studyDate: dateString(form.studyDate), durationMinutes: form.durationMinutes, content: form.content.trim(), reflection: form.reflection.trim(), focusScore: form.focusScore }
  saving.value = true
  try { editingId.value ? await updateRecord(editingId.value, payload) : await createRecord(payload); message.success(editingId.value ? '学习记录已更新' : '本次学习已记录'); drawerOpen.value = false; await load() } catch (error) { message.error((error as Error).message) } finally { saving.value = false }
}
async function remove(id: number) { try { await deleteRecord(id); message.success('学习记录已删除'); await load() } catch (error) { message.error((error as Error).message) } }

const weekDays = computed(() => {
  const now = new Date()
  const monday = new Date(now)
  monday.setDate(now.getDate() - ((now.getDay() + 6) % 7))
  const totals = records.value.reduce<Record<string, number>>((map, record) => { map[record.studyDate] = (map[record.studyDate] ?? 0) + record.durationMinutes; return map }, {})
  const maximum = Math.max(60, ...Object.values(totals))
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(monday); date.setDate(monday.getDate() + index)
    const key = dateString(date.getTime())
    return { key, label: ['一', '二', '三', '四', '五', '六', '日'][index], day: date.getDate(), minutes: totals[key] ?? 0, width: Math.max(4, ((totals[key] ?? 0) / maximum) * 100) }
  })
})
const weekMinutes = computed(() => weekDays.value.reduce((sum, day) => sum + day.minutes, 0))
onMounted(load)
</script>

<template>
  <section>
    <div class="hero-row compact">
      <div><p class="section-note">真实投入</p><h1>学习记录</h1><p class="hero-copy">记下学了什么、用了多久，以及下一次要改进什么。</p></div>
      <n-button type="primary" size="large" @click="openCreate">记录一次学习</n-button>
    </div>

    <div class="week-ribbon">
      <header><div><span>本周累计</span><strong>{{ Math.floor(weekMinutes / 60) }}<small>小时</small> {{ weekMinutes % 60 }}<small>分钟</small></strong></div><p>长度代表每天的实际投入</p></header>
      <div class="week-lines">
        <div v-for="day in weekDays" :key="day.key" class="week-line"><span>周{{ day.label }}<small>{{ day.day }}日</small></span><div><i :style="{ width: `${day.width}%` }"></i></div><b>{{ day.minutes }} 分钟</b></div>
      </div>
    </div>

    <div class="record-toolbar"><n-select v-model:value="selectedGoalId" :options="filterOptions" aria-label="按目标筛选" /><span>共 {{ visibleRecords.length }} 条记录</span></div>
    <n-spin :show="loading">
      <div v-if="visibleRecords.length" class="record-stream">
        <article v-for="record in visibleRecords" :key="record.id" class="record-entry">
          <time><strong>{{ record.studyDate.slice(8) }}</strong><span>{{ record.studyDate.slice(0, 7) }}</span></time>
          <div class="record-copy"><div class="ledger-title"><h2>{{ record.content }}</h2><span class="focus-score">专注度 {{ record.focusScore ?? '-' }}/5</span></div><p v-if="record.reflection">{{ record.reflection }}</p><div class="ledger-meta"><span>{{ goalName(record.goalId) }}</span><span v-if="taskName(record.taskId)">{{ taskName(record.taskId) }}</span></div></div>
          <div class="record-duration"><strong>{{ record.durationMinutes }}</strong><span>分钟</span></div>
          <div class="row-actions"><n-button quaternary size="small" @click="openEdit(record)">编辑</n-button><n-popconfirm @positive-click="remove(record.id)"><template #trigger><n-button quaternary size="small">删除</n-button></template>确认删除这条学习记录？</n-popconfirm></div>
        </article>
      </div>
      <div v-else-if="!loading" class="panel empty-panel"><n-empty description="还没有符合条件的学习记录"><template #extra><n-button type="primary" @click="openCreate">记录第一次学习</n-button></template></n-empty></div>
    </n-spin>

    <n-drawer v-model:show="drawerOpen" :width="500" placement="right"><n-drawer-content :title="editingId ? '编辑学习记录' : '记录一次学习'" closable><n-form label-placement="top"><div class="form-grid"><n-form-item label="学习目标" required><n-select v-model:value="form.goalId" :options="goalOptions" @update:value="form.taskId = null" /></n-form-item><n-form-item label="关联任务"><n-select v-model:value="form.taskId" :options="taskOptions" clearable placeholder="可以不关联任务" /></n-form-item></div><div class="form-grid"><n-form-item label="学习日期" required><n-date-picker v-model:value="form.studyDate" type="date" :is-date-disabled="(timestamp: number) => timestamp > Date.now()" /></n-form-item><n-form-item label="学习时长" required><n-input-number v-model:value="form.durationMinutes" :min="5" :max="1440"><template #suffix>分钟</template></n-input-number></n-form-item></div><n-form-item label="学习内容" required><n-input v-model:value="form.content" maxlength="500" show-count placeholder="具体完成了什么？" /></n-form-item><n-form-item label="学习反思"><n-input v-model:value="form.reflection" type="textarea" :rows="4" maxlength="1000" show-count placeholder="卡在哪里？下一次准备怎么调整？" /></n-form-item><n-form-item label="专注度"><n-rate v-model:value="form.focusScore" :count="5" color="#c59a5f" /></n-form-item></n-form><template #footer><div class="drawer-actions"><n-button @click="drawerOpen = false">取消</n-button><n-button type="primary" :loading="saving" @click="submit">保存记录</n-button></div></template></n-drawer-content></n-drawer>
  </section>
</template>
