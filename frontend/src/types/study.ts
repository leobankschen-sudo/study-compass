export type GoalStatus = 'ACTIVE' | 'COMPLETED' | 'ARCHIVED'
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string
  timestamp: string
}

export interface UserProfile {
  id: number
  username: string
  nickname: string
  email: string
  role: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface UserPayload {
  username: string
  nickname: string
  email: string
  role: string
  enabled: boolean
}

export type StudyPeriod = 'MORNING' | 'AFTERNOON' | 'EVENING' | 'FLEXIBLE'

export interface UserPreference {
  userId: number
  weeklyTargetHours: number
  preferredSessionMinutes: number
  preferredStudyPeriod: StudyPeriod
  aiPersonalization: boolean
  createdAt: string
  updatedAt: string
}

export interface UserPreferencePayload {
  weeklyTargetHours: number
  preferredSessionMinutes: number
  preferredStudyPeriod: StudyPeriod
  aiPersonalization: boolean
}

export interface AuthResponse {
  token: string
  expiresAt: string
  user: UserProfile
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  nickname: string
  email: string
  password: string
}

export interface LearningGoal {
  id: number
  userId: number
  title: string
  description: string | null
  startDate: string
  targetDate: string
  weeklyHours: number | null
  currentLevel: string | null
  status: GoalStatus
  createdAt: string
  updatedAt: string
}

export interface GoalPayload {
  userId: number
  title: string
  description: string
  startDate: string
  targetDate: string
  weeklyHours: number
  currentLevel: string
  status: GoalStatus
}

export interface LearningTask {
  id: number
  goalId: number
  userId: number
  title: string
  description: string | null
  priority: TaskPriority
  status: TaskStatus
  dueDate: string | null
  estimatedMinutes: number | null
  sortOrder: number | null
  aiGenerated: boolean
  completedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface TaskPayload {
  goalId: number
  userId: number
  title: string
  description: string
  priority: TaskPriority
  status: TaskStatus
  dueDate: string | null
  estimatedMinutes: number
  sortOrder: number
  aiGenerated: boolean
}

export interface StudyRecord {
  id: number
  userId: number
  goalId: number
  taskId: number | null
  studyDate: string
  durationMinutes: number
  content: string
  reflection: string | null
  focusScore: number | null
  createdAt: string
  updatedAt: string
}

export interface StudyRecordPayload {
  userId: number
  goalId: number
  taskId: number | null
  studyDate: string
  durationMinutes: number
  content: string
  reflection: string
  focusScore: number
}
