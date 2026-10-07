# 协作开发约定

## 分支约定

- `main`：稳定主分支，只接收已验证的合并请求。
- `feat/<name>`：功能开发，例如 `feat/study-review`。
- `fix/<name>`：缺陷修复，例如 `fix/task-deadline`。
- `chore/<name>`：工程配置，例如 `chore/ci-pipeline`。
- `docs/<name>`：文档整理。

每个分支只处理一个明确主题，完成后通过 Pull Request 合并到 `main`。

## 提交约定

提交信息采用 `<type>(<scope>): <summary>` 形式，`scope` 可省略：

- `feat`：新增功能；
- `fix`：修复缺陷；
- `docs`：仅文档变更；
- `test`：测试相关；
- `refactor`：不改变外部行为的重构；
- `ci`：持续集成配置；
- `chore`：构建或辅助配置。

示例：

```text
feat(api): add JWT authentication
fix(web): preserve selected task filter
ci: verify backend and frontend builds
```

## 合并请求检查

1. 分支基于最新 `main`。
2. 后端测试通过：`./mvnw -pl backend test`。
3. 前端类型检查与构建通过：在 `frontend` 目录执行 `npm ci` 和 `npm run build`。
4. 不提交真实密码、令牌、构建产物或本机配置。
5. 在 Pull Request 中说明变更、验证方式和界面影响。

