# 实验三：Git 团队协作与持续集成验收清单

## 实验目标

通过规范分支、结构化提交、Pull Request 模板和自动构建流程，建立可追溯、可审查、可验证的团队开发过程。

## 已完成内容

- [x] 将默认稳定分支统一为 `main`。
- [x] 按工作区、后端、前端、实验材料拆分提交历史。
- [x] 采用 Conventional Commits 风格的提交信息。
- [x] 建立 `feat/*`、`fix/*`、`chore/*`、`docs/*` 分支约定。
- [x] 新增 Pull Request 模板，覆盖变更说明、验证结果和敏感信息检查。
- [x] 新增 GitHub Actions CI，分别验证 Spring Boot 后端和 Vue 前端。
- [x] CI 使用最小只读权限，并对同一分支的重复任务启用自动取消。
- [x] `.gitignore` 排除依赖、构建产物、日志和本机环境配置。

## CI 流程

| 任务 | 运行环境 | 验证命令 | 预期结果 |
| --- | --- | --- | --- |
| Backend tests | Ubuntu + Temurin JDK 17 | `./mvnw -pl backend test` | Maven 测试通过 |
| Frontend build | Ubuntu + Node.js 22 | `npm ci`、`npm run build` | 类型检查和 Vite 构建通过 |

触发条件为推送到 `main`，或向 `main` 发起 Pull Request。

## 本地验证记录

- 验证日期：2026-09-24。
- 后端：Maven 测试阶段执行成功，结果为 `BUILD SUCCESS`。
- 前端：TypeScript 类型检查和 Vite 生产构建成功。
- 敏感信息：真实数据库密码未写入版本库。

## 建议截图材料

1. `git log --graph --oneline --all` 的提交历史。
2. GitHub 仓库的分支列表与 `chore/ci-pipeline` 分支。
3. Pull Request 页面及自动载入的检查清单。
4. Actions 页面中 Backend tests、Frontend build 均通过的结果。
5. Pull Request 合并后 `main` 分支的提交记录。

## 远程验收步骤

当前项目只建立本地仓库。创建 GitHub 仓库后，可执行：

```bash
git remote add origin <你的仓库地址>
git push -u origin main
git push -u origin chore/ci-pipeline
```

随后从 `chore/ci-pipeline` 向 `main` 创建 Pull Request，等待两项 CI 检查通过后再合并。真实仓库地址、账号令牌和密码不应写入项目文件或实验报告。
