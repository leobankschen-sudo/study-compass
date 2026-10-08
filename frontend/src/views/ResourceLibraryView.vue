<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { NButton, NDrawer, NDrawerContent, NEmpty, NForm, NFormItem, NInput, NPopconfirm, NSelect, NSpin, useMessage } from 'naive-ui'
import { createResource, deleteResource, getCurrentUserId, listGoals, listResources, updateResource } from '@/api/study'
import type { LearningGoal, LearningResource, LearningResourcePayload, ResourceType } from '@/types/study'

const message = useMessage()
const loading = ref(true)
const saving = ref(false)
const drawerOpen = ref(false)
const editingId = ref<number | null>(null)
const resources = ref<LearningResource[]>([])
const goals = ref<LearningGoal[]>([])
const search = ref('')
const typeFilter = ref<ResourceType | 'ALL'>('ALL')
const goalFilter = ref<number | null>(null)

const typeOptions = [
  { label: '文档', value: 'DOCUMENT' },
  { label: '文章', value: 'ARTICLE' },
  { label: '视频', value: 'VIDEO' },
  { label: '课程', value: 'COURSE' },
  { label: '其他', value: 'OTHER' },
]
const typeLabels: Record<ResourceType, string> = { DOCUMENT: '文档', ARTICLE: '文章', VIDEO: '视频', COURSE: '课程', OTHER: '其他' }
const typeMarks: Record<ResourceType, string> = { DOCUMENT: '文', ARTICLE: '章', VIDEO: '影', COURSE: '课', OTHER: '录' }
const form = reactive<LearningResourcePayload>({
  userId: 0, goalId: null, title: '', type: 'DOCUMENT', sourceUrl: '', description: '', tags: '',
})

const goalOptions = computed(() => goals.value.map((goal) => ({ label: goal.title, value: goal.id })))
const filterGoalOptions = computed(() => [{ label: '全部目标', value: 0 }, ...goalOptions.value])
const filteredResources = computed(() => {
  const keyword = search.value.trim().toLowerCase()
  return resources.value.filter((resource) => {
    const matchesType = typeFilter.value === 'ALL' || resource.type === typeFilter.value
    const matchesGoal = !goalFilter.value || resource.goalId === goalFilter.value
    const haystack = `${resource.title} ${resource.description ?? ''} ${resource.tags ?? ''}`.toLowerCase()
    return matchesType && matchesGoal && (!keyword || haystack.includes(keyword))
  })
})
const sourcedCount = computed(() => resources.value.filter((resource) => Boolean(resource.sourceUrl)).length)
const linkedGoalCount = computed(() => new Set(resources.value.map((resource) => resource.goalId).filter(Boolean)).size)

function goalName(id: number | null) {
  return id ? goals.value.find((goal) => goal.id === id)?.title ?? '目标已归档' : '通用资料'
}

function tagList(value: string | null) {
  return value?.split(/[,，]/).map((tag) => tag.trim()).filter(Boolean).slice(0, 4) ?? []
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { userId: getCurrentUserId(), goalId: null, title: '', type: 'DOCUMENT', sourceUrl: '', description: '', tags: '' })
}

function openCreate() {
  resetForm()
  drawerOpen.value = true
}

function openEdit(resource: LearningResource) {
  editingId.value = resource.id
  Object.assign(form, {
    userId: resource.userId, goalId: resource.goalId, title: resource.title, type: resource.type,
    sourceUrl: resource.sourceUrl ?? '', description: resource.description ?? '', tags: resource.tags ?? '',
  })
  drawerOpen.value = true
}

function openSource(resource: LearningResource) {
  if (!resource.sourceUrl) return
  try {
    const url = new URL(resource.sourceUrl)
    if (!['http:', 'https:'].includes(url.protocol)) throw new Error()
    window.open(url.toString(), '_blank', 'noopener,noreferrer')
  } catch {
    message.warning('来源地址不是有效的网页链接')
  }
}

async function load() {
  loading.value = true
  try {
    const [goalData, resourceData] = await Promise.all([listGoals(), listResources()])
    goals.value = goalData
    resources.value = resourceData
  } catch (error) {
    message.error(error instanceof Error ? error.message : '资料加载失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.title.trim()) return message.warning('请填写资料标题')
  saving.value = true
  const payload = { ...form, title: form.title.trim(), sourceUrl: form.sourceUrl.trim(), description: form.description.trim(), tags: form.tags.trim() }
  try {
    editingId.value ? await updateResource(editingId.value, payload) : await createResource(payload)
    message.success(editingId.value ? '资料已更新' : '资料已收录')
    drawerOpen.value = false
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(id: number) {
  try {
    await deleteResource(id)
    message.success('资料已删除')
    await load()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <section class="library-page">
    <div class="hero-row compact">
      <div>
        <p class="section-note">来源与知识线索</p>
        <h1>课程资料库</h1>
        <p class="hero-copy">保存可靠来源、课程笔记与视频，并关联到具体学习目标。</p>
      </div>
      <n-button type="primary" size="large" @click="openCreate">收录资料</n-button>
    </div>

    <div class="library-summary">
      <div><span>已收录</span><strong>{{ resources.length }}</strong><small>份资料</small></div>
      <div><span>有来源链接</span><strong>{{ sourcedCount }}</strong><small>份可追溯</small></div>
      <div><span>关联目标</span><strong>{{ linkedGoalCount }}</strong><small>条学习主线</small></div>
      <p>资料不是收藏得越多越好。保留可信来源，并写下它解决了什么问题。</p>
    </div>

    <div class="library-toolbar">
      <n-input v-model:value="search" clearable placeholder="搜索标题、备注或标签" />
      <n-select v-model:value="typeFilter" :options="[{ label: '全部类型', value: 'ALL' }, ...typeOptions]" />
      <n-select v-model:value="goalFilter" clearable :options="filterGoalOptions" placeholder="全部目标" @update:value="(value) => goalFilter = value || null" />
    </div>

    <n-spin :show="loading">
      <div v-if="filteredResources.length" class="resource-shelf">
        <article v-for="resource in filteredResources" :key="resource.id" class="resource-row">
          <div :class="['resource-spine', `type-${resource.type.toLowerCase()}`]">{{ typeMarks[resource.type] }}</div>
          <div class="resource-copy">
            <div class="resource-title">
              <h2>{{ resource.title }}</h2>
              <span>{{ typeLabels[resource.type] }}</span>
            </div>
            <p>{{ resource.description || '暂未补充这份资料的用途说明。' }}</p>
            <div class="resource-meta">
              <span>{{ goalName(resource.goalId) }}</span>
              <i v-for="tag in tagList(resource.tags)" :key="tag">{{ tag }}</i>
            </div>
          </div>
          <div class="resource-source">
            <button v-if="resource.sourceUrl" type="button" @click="openSource(resource)">打开来源</button>
            <span v-else>个人笔记</span>
          </div>
          <div class="row-actions">
            <n-button quaternary size="small" @click="openEdit(resource)">编辑</n-button>
            <n-popconfirm @positive-click="remove(resource.id)">
              <template #trigger><n-button quaternary size="small">删除</n-button></template>
              确定删除这份资料吗？
            </n-popconfirm>
          </div>
        </article>
      </div>
      <div v-else-if="!loading" class="panel empty-panel">
        <n-empty :description="resources.length ? '没有符合筛选条件的资料' : '资料库还是空的'">
          <template #extra><n-button v-if="!resources.length" type="primary" @click="openCreate">收录第一份资料</n-button></template>
        </n-empty>
      </div>
    </n-spin>

    <n-drawer v-model:show="drawerOpen" :width="500" placement="right">
      <n-drawer-content :title="editingId ? '编辑学习资料' : '收录学习资料'" closable>
        <n-form label-placement="top">
          <n-form-item label="资料标题" required><n-input v-model:value="form.title" maxlength="120" show-count placeholder="例如：Spring Boot 官方参考文档" /></n-form-item>
          <div class="form-grid">
            <n-form-item label="资料类型" required><n-select v-model:value="form.type" :options="typeOptions" /></n-form-item>
            <n-form-item label="关联目标"><n-select v-model:value="form.goalId" clearable :options="goalOptions" placeholder="通用资料" /></n-form-item>
          </div>
          <n-form-item label="来源地址"><n-input v-model:value="form.sourceUrl" maxlength="500" placeholder="https://...；个人笔记可留空" /></n-form-item>
          <n-form-item label="用途说明"><n-input v-model:value="form.description" type="textarea" :autosize="{ minRows: 4, maxRows: 8 }" maxlength="2000" show-count placeholder="这份资料解决什么问题？哪些章节最重要？" /></n-form-item>
          <n-form-item label="标签"><n-input v-model:value="form.tags" maxlength="200" placeholder="使用逗号分隔，例如：Spring Boot, 官方文档" /></n-form-item>
        </n-form>
        <template #footer><div class="drawer-actions"><n-button @click="drawerOpen = false">取消</n-button><n-button type="primary" :loading="saving" @click="submit">保存资料</n-button></div></template>
      </n-drawer-content>
    </n-drawer>
  </section>
</template>
