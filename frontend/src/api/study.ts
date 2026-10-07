import { http } from './http'
import type { AiGeneratePayload, AiReport, AiReportType, ApiResponse, AuthResponse, GoalPayload, LearningGoal, LearningTask, LoginPayload, RegisterPayload, StudyRecord, StudyRecordPayload, TaskPayload, TaskStatus, UserPayload, UserPreference, UserPreferencePayload, UserProfile } from '@/types/study'

export function getCurrentUserId() {
  const stored = localStorage.getItem('study_compass_user')
  if (!stored) throw new Error('登录状态已失效，请重新登录')
  return (JSON.parse(stored) as UserProfile).id
}

export async function login(payload: LoginPayload) {
  const { data } = await http.post<ApiResponse<AuthResponse>>('/auth/login', payload)
  return data.data
}

export async function register(payload: RegisterPayload) {
  const { data } = await http.post<ApiResponse<AuthResponse>>('/auth/register', payload)
  return data.data
}

export async function getMe() {
  const { data } = await http.get<ApiResponse<UserProfile>>('/auth/me')
  return data.data
}

export async function getUser(id = getCurrentUserId()) {
  const { data } = await http.get<ApiResponse<UserProfile>>(`/users/${id}`)
  return data.data
}

export async function updateUser(id: number, payload: UserPayload) {
  const { data } = await http.put<ApiResponse<UserProfile>>(`/users/${id}`, payload)
  return data.data
}

export async function getPreference(userId = getCurrentUserId()) {
  const { data } = await http.get<ApiResponse<UserPreference>>(`/preferences/${userId}`)
  return data.data
}

export async function updatePreference(userId: number, payload: UserPreferencePayload) {
  const { data } = await http.put<ApiResponse<UserPreference>>(`/preferences/${userId}`, payload)
  return data.data
}

export async function listGoals(userId = getCurrentUserId()) {
  const { data } = await http.get<ApiResponse<LearningGoal[]>>('/goals', { params: { userId } })
  return data.data
}

export async function createGoal(payload: GoalPayload) {
  const { data } = await http.post<ApiResponse<LearningGoal>>('/goals', payload)
  return data
}

export async function updateGoal(id: number, payload: GoalPayload) {
  const { data } = await http.put<ApiResponse<LearningGoal>>(`/goals/${id}`, payload)
  return data
}

export async function deleteGoal(id: number) {
  await http.delete(`/goals/${id}`)
}

export async function listTasks(userId = getCurrentUserId(), status?: TaskStatus) {
  const { data } = await http.get<ApiResponse<LearningTask[]>>('/tasks', { params: { userId, status } })
  return data.data
}

export async function createTask(payload: TaskPayload) {
  const { data } = await http.post<ApiResponse<LearningTask>>('/tasks', payload)
  return data
}

export async function updateTask(id: number, payload: TaskPayload) {
  const { data } = await http.put<ApiResponse<LearningTask>>(`/tasks/${id}`, payload)
  return data
}

export async function deleteTask(id: number) {
  await http.delete(`/tasks/${id}`)
}

export async function listRecords(userId = getCurrentUserId(), startDate?: string, endDate?: string) {
  const { data } = await http.get<ApiResponse<StudyRecord[]>>('/records', { params: { userId, startDate, endDate } })
  return data.data
}

export async function createRecord(payload: StudyRecordPayload) {
  const { data } = await http.post<ApiResponse<StudyRecord>>('/records', payload)
  return data
}

export async function updateRecord(id: number, payload: StudyRecordPayload) {
  const { data } = await http.put<ApiResponse<StudyRecord>>(`/records/${id}`, payload)
  return data
}

export async function deleteRecord(id: number) {
  await http.delete(`/records/${id}`)
}

export async function listAiReports(type?: AiReportType) {
  const { data } = await http.get<ApiResponse<AiReport[]>>('/ai/reports', { params: { type } })
  return data.data
}

export async function generateAiReport(type: AiReportType, payload: AiGeneratePayload) {
  const { data } = await http.post<ApiResponse<AiReport>>(`/ai/reports/${type}`, payload, { timeout: 60000 })
  return data.data
}

export async function deleteAiReport(id: number) {
  await http.delete(`/ai/reports/${id}`)
}
