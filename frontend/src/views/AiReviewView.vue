<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { NButton, NEmpty, NInput, NPopconfirm, NSelect, NSpin, useMessage } from 'naive-ui'
import { createTask, deleteAiReport, generateAiReport, getCurrentUserId, listAiReports, listGoals, listRecords, listTasks } from '@/api/study'
import type { AiAction, AiReport, AiReportType, LearningGoal, LearningTask, StudyRecord } from '@/types/study'

const message = useMessage()
const mode = ref<AiReportType>('REVIEW')
const days = ref(7)
const goalId = ref<number | null>(null)
const note = ref('')
const loading = ref(false)
const pageLoading = ref(true)
const goals = ref<LearningGoal[]>([])
const tasks = ref<LearningTask[]>([])
const records = ref<StudyRecord[]>([])
const reports = ref<AiReport[]>([])
const currentReport = ref<AiReport | null>(null)

const goalOptions = computed(() => goals.value
  .filter((goal) => goal.status === 'ACTIVE')
  .map((goal) => ({ label: goal.title, value: goal.id })))
const periodStart = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() - days.value + 1)
  return date.toISOString().slice(0, 10)
})
const selectedRecords = computed(() => records.value.filter((record) =>
  record.studyDate >= periodStart.value && (!goalId.value || record.goalId === goalId.value),
))
const selectedTasks = computed(() => tasks.value.filter((task) => !goalId.value || task.goalId === goalId.value))
const previewStats = computed(() => ({
  records: selectedRecords.value.length,
  minutes: selectedRecords.value.reduce((sum, record) => sum + record.durationMinutes, 0),
  done: selectedTasks.value.filter((task) => task.status === 'COMPLETED').length,
}))
const modeCopy = computed(() => mode.value === 'REVIEW'
  ? { title: '用证据复盘，而不是凭感觉总结', action: '生成本期复盘', note: '可以补充：最近卡住的知识点、考试反馈或你最想调整的习惯。' }
  : { title: '把目标拆成下一段可执行节奏', action: '生成学习计划', note: '可以补充：课程时间、考试日期或本阶段必须完成的内容。' })

function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric' }).format(new Date(value))
}

function priorityLabel(value: string) {
  return { HIGH: '优先', MEDIUM: '常规', LOW: '可选' }[value] ?? '常规'
}

async function loadPage() {
  pageLoading.value = true
  try {
    const [goalData, taskData, recordData, reportData] = await Promise.all([
      listGoals(), listTasks(), listRecords(), listAiReports(),
    ])
    goals.value = goalData
    tasks.value = taskData
    records.value = recordData
    reports.value = reportData
    currentReport.value = reportData[0] ?? null
  } catch (error) {
    message.error(error instanceof Error ? error.message : '学习数据加载失败')
  } finally {
    pageLoading.value = false
  }
}

async function generate() {
  if (mode.value === 'PLAN' && !goalId.value) {
    message.warning('请先选择要规划的学习目标')
    return
  }
  if (mode.value === 'REVIEW' && selectedRecords.value.length === 0) {
    message.warning('所选周期还没有学习记录，请先补充记录')
    return
  }
  loading.value = true
  try {
    const result = await generateAiReport(mode.value, { goalId: goalId.value, days: days.value, note: note.value.trim() })
    currentReport.value = result
    reports.value = [result, ...reports.value]
    message.success(mode.value === 'REVIEW' ? '复盘已生成并保存' : '学习计划已生成并保存')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '生成失败，请稍后重试', { duration: 5000 })
  } finally {
    loading.value = false
  }
}

async function removeReport(id: number) {
  try {
    await deleteAiReport(id)
    reports.value = reports.value.filter((report) => report.id !== id)
    if (currentReport.value?.id === id) currentReport.value = reports.value[0] ?? null
    message.success('历史报告已删除')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

async function addActionToTasks(action: AiAction) {
  if (!currentReport.value?.goalId) {
    message.warning('汇总复盘没有绑定单一目标，请先生成某个目标的计划')
    return
  }
  const dueDate = new Date()
  dueDate.setDate(dueDate.getDate() + 7)
  try {
    await createTask({
      goalId: currentReport.value.goalId,
      userId: getCurrentUserId(),
      title: action.title,
      description: action.rationale,
      priority: action.priority,
      status: 'TODO',
      dueDate: dueDate.toISOString().slice(0, 10),
      estimatedMinutes: action.durationMinutes,
      sortOrder: 0,
      aiGenerated: true,
    })
    tasks.value = await listTasks()
    message.success('已加入任务计划')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加入任务失败')
  }
}

watch(mode, (next) => {
  if (next === 'PLAN' && goalId.value === null) goalId.value = goals.value.find((goal) => goal.status === 'ACTIVE')?.id ?? null
})

onMounted(loadPage)
</script>

<template>
  <section class="coach-page">
    <header class="coach-heading">
      <div>
        <p class="section-note">复盘与下一步</p>
        <h1>学习教练</h1>
        <p class="hero-copy">读取你的目标、任务与学习记录，给出有依据的调整建议。</p>
      </div>
      <div class="coach-mode" role="tablist" aria-label="生成类型">
        <button :class="{ active: mode === 'REVIEW' }" type="button" role="tab" @click="mode = 'REVIEW'">阶段复盘</button>
        <button :class="{ active: mode === 'PLAN' }" type="button" role="tab" @click="mode = 'PLAN'">学习计划</button>
      </div>
    </header>

    <n-spin :show="pageLoading">
      <div class="coach-workbench">
        <aside class="coach-evidence">
          <div class="evidence-heading">
            <span>生成条件</span>
            <strong>{{ modeCopy.title }}</strong>
          </div>

          <label class="coach-field">
            <span>学习目标</span>
            <n-select v-model:value="goalId" :options="goalOptions" :clearable="mode === 'REVIEW'" :placeholder="mode === 'PLAN' ? '选择一个目标' : '全部学习目标'" />
          </label>
          <label class="coach-field">
            <span>分析周期</span>
            <n-select v-model:value="days" :options="[
              { label: '最近 7 天', value: 7 },
              { label: '最近 14 天', value: 14 },
              { label: '最近 30 天', value: 30 },
            ]" />
          </label>
          <label class="coach-field">
            <span>补充说明 <small>选填</small></span>
            <n-input v-model:value="note" type="textarea" :maxlength="300" show-count :autosize="{ minRows: 4, maxRows: 7 }" :placeholder="modeCopy.note" />
          </label>

          <div class="evidence-ledger" aria-label="本次使用的数据">
            <p>本次将使用</p>
            <dl>
              <div><dt>学习记录</dt><dd>{{ previewStats.records }} 条</dd></div>
              <div><dt>有效时长</dt><dd>{{ previewStats.minutes }} 分钟</dd></div>
              <div><dt>已完成任务</dt><dd>{{ previewStats.done }} 项</dd></div>
            </dl>
          </div>

          <n-button type="primary" size="large" block :loading="loading" @click="generate">
            {{ modeCopy.action }}
          </n-button>
          <p class="privacy-note">只发送学习内容和统计数据，不发送用户名、邮箱或密码。生成内容需由你确认。</p>
        </aside>

        <article class="coach-report" :class="{ empty: !currentReport }">
          <template v-if="currentReport">
            <header class="report-heading">
              <div>
                <span>{{ currentReport.type === 'REVIEW' ? '阶段复盘' : '学习计划' }}</span>
                <h2>{{ currentReport.content.title }}</h2>
              </div>
              <time>{{ formatDate(currentReport.createdAt) }}</time>
            </header>

            <p class="report-summary">{{ currentReport.content.summary }}</p>

            <section class="report-evidence">
              <h3>判断依据</h3>
              <ul>
                <li v-for="item in currentReport.content.evidence" :key="item">{{ item }}</li>
              </ul>
            </section>

            <section class="reasoning-track">
              <h3>观察与判断</h3>
              <div v-for="(insight, index) in currentReport.content.insights" :key="`${insight.title}-${index}`" class="reasoning-step">
                <span>{{ index + 1 }}</span>
                <div><strong>{{ insight.title }}</strong><p>{{ insight.detail }}</p></div>
              </div>
            </section>

            <section class="action-list">
              <h3>下一步行动</h3>
              <div v-for="action in currentReport.content.actions" :key="action.title" class="action-row">
              <div><strong>{{ action.title }}</strong><p>{{ action.rationale }}</p></div>
              <span>{{ priorityLabel(action.priority) }}</span>
              <b>{{ action.durationMinutes }} 分钟</b>
              <button v-if="currentReport.goalId" type="button" @click="addActionToTasks(action)">加入任务</button>
            </div>
            </section>

            <footer class="report-meta">
              <span>{{ currentReport.periodStart }} 至 {{ currentReport.periodEnd }}</span>
              <span>{{ currentReport.sourceStats.recordCount }} 条记录 / {{ currentReport.sourceStats.totalMinutes }} 分钟</span>
              <span>模型 {{ currentReport.model }}</span>
            </footer>
          </template>
          <n-empty v-else description="还没有生成过复盘或计划">
            <template #extra><span class="muted-copy">左侧确认学习证据后，生成第一份教练报告</span></template>
          </n-empty>
        </article>
      </div>
    </n-spin>

    <section v-if="reports.length" class="report-history">
      <header><h2>历史报告</h2><span>最近 {{ reports.length }} 份</span></header>
      <div class="history-list">
        <div v-for="report in reports" :key="report.id" :class="['history-row', { active: currentReport?.id === report.id }]">
          <button type="button" @click="currentReport = report">
            <span>{{ report.type === 'REVIEW' ? '复盘' : '计划' }}</span>
            <strong>{{ report.content.title }}</strong>
            <time>{{ formatDate(report.createdAt) }}</time>
          </button>
          <n-popconfirm @positive-click.stop="removeReport(report.id)">
            <template #trigger><button class="history-delete" type="button" aria-label="删除历史报告">删除</button></template>
            确定删除这份历史报告吗？
          </n-popconfirm>
        </div>
      </div>
    </section>
  </section>
</template>
