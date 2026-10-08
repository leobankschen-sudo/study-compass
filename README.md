# 知序

基于大模型的个性化学习进度与智能复盘系统。

## 项目结构

- `backend`：Spring Boot 3 + MyBatis + MySQL REST API
- `frontend`：Vue 3 + TypeScript + Pinia + Naive UI
- `docs`：课程实验过程材料与接口约定

## 当前阶段

五个实验对应的核心功能均已实现：账号认证、学习目标、任务管理、学习记录、数据看板、AI 智能复盘与计划、资料库及个人设置。DeepSeek 只通过后端调用，可基于当前用户的真实目标、任务与学习记录生成结构化报告，并把建议一键转成后续任务。

教师演示流程、项目亮点及常见答辩问题见 [`docs/teacher-demo-guide.md`](docs/teacher-demo-guide.md)。

## 本地环境

- JDK 17 或更高版本
- MySQL 8
- Node.js 20 或更高版本
- Maven 3.9，或直接使用项目自带的 Maven Wrapper

## 数据库

1. 在 MySQL 中创建数据库：`study_compass`
2. 执行 `backend/src/main/resources/db/schema.sql`
3. 根据需要执行 `backend/src/main/resources/db/sample-data.sql`
4. 通过环境变量配置连接信息：
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`

PowerShell 示例（请替换为本机账号信息，不要把真实密码提交到仓库）：

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的本机 MySQL 密码"
.\mvnw.cmd -pl backend spring-boot:run
```

## 开发端口

- 后端：`8080`
- 前端：`5173`

## DeepSeek 配置

后端通过环境变量读取密钥，前端不会接触 API Key：

```powershell
$env:DEEPSEEK_API_KEY = "请在本机填写，不要提交到 Git"
```

推荐运行 `scripts/start-local.ps1`，脚本会用隐藏输入分别读取 MySQL 密码和 DeepSeek API Key，并在后端进程结束后清理环境变量。默认模型为 `deepseek-flash`，可通过 `DEEPSEEK_MODEL` 覆盖。

## 本地演示账号

- 用户名：`demo_user`
- 密码：`Study2026`

该密码仅用于本地课程演示。部署时设置 `DEMO_ACCOUNT_ENABLED=false`，并通过 `JWT_SECRET` 提供独立的高强度令牌密钥。

## 验证项目

在项目根目录运行：

```powershell
.\mvnw.cmd test
cd frontend
npm install
npm run build
```

提交与分支约定见 `CONTRIBUTING.md`，各阶段验收材料见 `docs` 目录。

## 当前交付边界

本项目定位为可在本机完整演示的课程实训作品，暂未包含公网部署、微信登录、验证码、社交排行、多人共享、OCR、复杂日历拖拽、语音对话和手机 App。这些属于主动的范围控制，不影响五个实验的核心验收。
