<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NForm, NFormItem, NInput, useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'

const props = defineProps<{ mode: 'login' | 'register' }>()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', email: '', password: '', confirmPassword: '' })
const isLogin = computed(() => props.mode === 'login')

async function submit() {
  if (!form.username.trim() || !form.password) return message.warning('请填写用户名和密码')
  if (!isLogin.value && (!form.nickname.trim() || !form.email.trim())) return message.warning('请完整填写注册信息')
  if (!isLogin.value && form.password !== form.confirmPassword) return message.warning('两次输入的密码不一致')
  if (!isLogin.value && (!/[A-Za-z]/.test(form.password) || !/\d/.test(form.password) || form.password.length < 8)) return message.warning('密码至少8位，且必须包含字母和数字')
  loading.value = true
  try {
    if (isLogin.value) await userStore.login({ username: form.username.trim(), password: form.password })
    else await userStore.register({ username: form.username.trim(), nickname: form.nickname.trim(), email: form.email.trim(), password: form.password })
    message.success(isLogin.value ? '欢迎回来' : '账号创建成功')
    await router.replace({ name: 'dashboard' })
  } catch (error) { message.error((error as Error).message) } finally { loading.value = false }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-story">
      <button class="auth-brand" type="button" @click="router.push('/login')"><span>序</span><strong>知序</strong></button>
      <div class="auth-statement"><p>把学习变成一件<br>有迹可循的事。</p><span>目标、任务、投入和复盘，都按你的真实节奏积累。</span></div>
      <div class="auth-footnote"><i></i><p>计划不是越满越好。知序帮助你看见执行与预期之间的差距，再决定下一步。</p></div>
    </section>

    <section class="auth-form-side">
      <div class="auth-form-wrap">
        <p class="section-note">{{ isLogin ? '继续你的学习进度' : '建立个人学习档案' }}</p>
        <h1>{{ isLogin ? '登录知序' : '创建账号' }}</h1>
        <p class="auth-intro">{{ isLogin ? '输入账号信息，回到你的学习台账。' : '只需要基础信息，偏好可以稍后调整。' }}</p>
        <n-form label-placement="top" @submit.prevent="submit">
          <n-form-item label="用户名"><n-input v-model:value="form.username" maxlength="30" autocomplete="username" placeholder="4—30位字母、数字或下划线" /></n-form-item>
          <template v-if="!isLogin"><div class="form-grid"><n-form-item label="昵称"><n-input v-model:value="form.nickname" maxlength="40" autocomplete="name" /></n-form-item><n-form-item label="邮箱"><n-input v-model:value="form.email" autocomplete="email" /></n-form-item></div></template>
          <n-form-item label="密码"><n-input v-model:value="form.password" type="password" show-password-on="mousedown" :autocomplete="isLogin ? 'current-password' : 'new-password'" @keyup.enter="submit" /></n-form-item>
          <n-form-item v-if="!isLogin" label="确认密码"><n-input v-model:value="form.confirmPassword" type="password" show-password-on="mousedown" autocomplete="new-password" @keyup.enter="submit" /></n-form-item>
          <p v-if="!isLogin" class="password-hint">至少8位，同时包含字母和数字。密码只以 BCrypt 密文保存。</p>
          <n-button type="primary" size="large" block :loading="loading" @click="submit">{{ isLogin ? '进入知序' : '创建并进入' }}</n-button>
        </n-form>
        <p class="auth-switch">{{ isLogin ? '还没有账号？' : '已经有账号？' }}<button type="button" @click="router.push(isLogin ? '/register' : '/login')">{{ isLogin ? '创建账号' : '返回登录' }}</button></p>
      </div>
    </section>
  </main>
</template>
