# 知序

基于大模型的个性化学习进度与智能复盘系统。

## 项目结构

- `backend`：Spring Boot 3 + MyBatis + MySQL REST API
- `frontend`：Vue 3 + TypeScript + Pinia + Naive UI
- `docs`：课程实验过程材料与接口约定

## 当前阶段

实验一的后端基础与 CRUD、实验二的前后端分离页面及账号鉴权均已完成。实验三已建立规范分支、结构化提交、Pull Request 模板和自动构建流程。目前学习目标、学习任务、学习记录和首页统计均已接入真实 MySQL 数据。

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
