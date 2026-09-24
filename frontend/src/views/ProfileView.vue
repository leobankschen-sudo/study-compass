<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { NButton, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpin, NSwitch, useMessage } from 'naive-ui'
import { getCurrentUserId, getPreference, getUser, updatePreference, updateUser } from '@/api/study'
import { useUserStore } from '@/stores/user'
import type { StudyPeriod, UserPreferencePayload, UserProfile } from '@/types/study'

const message = useMessage()
const userStore = useUserStore()
const loading = ref(true)
const savingProfile = ref(false)
const savingPreference = ref(false)
const profile = ref<UserProfile | null>(null)
const profileForm = reactive({ username: '', nickname: '', email: '' })
const preferenceForm = reactive({ weeklyTargetHours: 10, preferredSessionMinutes: 45, preferredStudyPeriod: 'EVENING' as StudyPeriod, aiPersonalization: true })
const periodOptions = [{ label: '上午', value: 'MORNING' }, { label: '下午', value: 'AFTERNOON' }, { label: '晚上', value: 'EVENING' }, { label: '灵活安排', value: 'FLEXIBLE' }]
const joinedAt = computed(() => profile.value ? new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(profile.value.createdAt)) : '-')
const avatarText = computed(() => profileForm.nickname.slice(0, 1) || '学')

async function load() {
  loading.value = true
  try {
    const [user, preference] = await Promise.all([getUser(), getPreference()])
    profile.value = user
    Object.assign(profileForm, { username: user.username, nickname: user.nickname, email: user.email })
    Object.assign(preferenceForm, preference)
  } catch (error) { message.error((error as Error).message) } finally { loading.value = false }
}

async function saveProfile() {
  if (!profile.value) return
  if (!profileForm.nickname.trim()) return message.warning('请填写昵称')
  if (!profileForm.email.includes('@')) return message.warning('请填写正确的邮箱')
  savingProfile.value = true
  try {
    const updated = await updateUser(getCurrentUserId(), { username: profileForm.username, nickname: profileForm.nickname.trim(), email: profileForm.email.trim(), role: profile.value.role, enabled: profile.value.enabled })
    profile.value = updated
    userStore.setProfile(updated)
    message.success('基本资料已保存')
  } catch (error) { message.error((error as Error).message) } finally { savingProfile.value = false }
}

async function savePreference() {
  savingPreference.value = true
  try { await updatePreference(getCurrentUserId(), preferenceForm as UserPreferencePayload); message.success('学习偏好已保存') } catch (error) { message.error((error as Error).message) } finally { savingPreference.value = false }
}
onMounted(load)
</script>

<template>
  <n-spin :show="loading">
    <section class="profile-page">
      <div class="hero-row compact"><div><p class="section-note">账号与偏好</p><h1>个人中心</h1><p class="hero-copy">这些信息将用于安排学习节奏和生成更贴合你的建议。</p></div></div>
      <div class="profile-layout">
        <aside class="identity-band">
          <div class="large-avatar">{{ avatarText }}</div>
          <h2>{{ profileForm.nickname || '学习者' }}</h2>
          <p>@{{ profileForm.username }}</p>
          <dl><div><dt>身份</dt><dd>{{ profile?.role === 'ADMIN' ? '管理员' : '学习者' }}</dd></div><div><dt>加入知序</dt><dd>{{ joinedAt }}</dd></div><div><dt>账号状态</dt><dd>{{ profile?.enabled ? '正常' : '已停用' }}</dd></div></dl>
        </aside>

        <div class="profile-settings">
          <section class="settings-section"><header><div><h2>基本资料</h2><p>用于账号识别和页面展示。</p></div><n-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</n-button></header><n-form label-placement="top"><div class="form-grid"><n-form-item label="用户名"><n-input v-model:value="profileForm.username" maxlength="30" /></n-form-item><n-form-item label="昵称"><n-input v-model:value="profileForm.nickname" maxlength="40" /></n-form-item></div><n-form-item label="邮箱"><n-input v-model:value="profileForm.email" type="text" /></n-form-item></n-form></section>

          <section class="settings-section"><header><div><h2>学习偏好</h2><p>生成计划时优先遵循这些节奏。</p></div><n-button type="primary" :loading="savingPreference" @click="savePreference">保存偏好</n-button></header><n-form label-placement="top"><div class="form-grid"><n-form-item label="每周目标"><n-input-number v-model:value="preferenceForm.weeklyTargetHours" :min="1" :max="80"><template #suffix>小时</template></n-input-number></n-form-item><n-form-item label="单次学习时长"><n-input-number v-model:value="preferenceForm.preferredSessionMinutes" :min="10" :max="240"><template #suffix>分钟</template></n-input-number></n-form-item></div><n-form-item label="偏好学习时段"><n-select v-model:value="preferenceForm.preferredStudyPeriod" :options="periodOptions" /></n-form-item><div class="preference-switch"><div><strong>允许 AI 使用学习数据进行个性化分析</strong><span>仅使用本项目中的目标、任务与学习记录。</span></div><n-switch v-model:value="preferenceForm.aiPersonalization" /></div></n-form></section>

          <section class="settings-section security-preview"><header><div><h2>账号安全</h2><p>密码修改和登录设备管理将在权限实验中接入。</p></div><n-button disabled>实验四开放</n-button></header></section>
        </div>
      </div>
    </section>
  </n-spin>
</template>
