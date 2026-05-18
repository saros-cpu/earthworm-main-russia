# 鹅语菌 · 完整部署文档

> 目标：从一台**全新 Windows 电脑**起步，30 分钟内完成部署，跑出与当前本地环境**完全一致**的鹅语菌服务（25 课程包 / 1 841 节 / 19 103 句词）。
>
> 适用范围：Windows 10 / 11 (64-bit)。Linux / macOS 仅做最小提示，命令需自行适配。

---

## 选两条路中的一条

在开始之前先决定你要哪种部署：

| 路径                            | 包含什么                                                               | 什么不包含                                         | 适用场景                                      |
| ------------------------------- | ---------------------------------------------------------------------- | -------------------------------------------------- | --------------------------------------------- |
| **A. 全新部署**（默认）         | 25 课程包 + 1 841 课时 + 19 103 句词（从 git 仓库里的 JSON 自动 seed） | 以前的用户账号 / 错题本 / 生词本 / 打卡记录 / 留言 | 另一台机器从零跑项目 · CI/微服务器 · 多人开发 |
| **B. 完整迁移**（A + 动态数据） | A 的全部 **加上** 当前本机的账号 / 学习记录 / 留言等运行时数据         | —                                                  | 备份到另一台机器继续用 · 服务器照搬           |

**只记住一句**：**课程包内容不需要备份**（在 git 里），只有 _用户产生的动态数据_ 才需要备份。

- 选了 **A**：跳过下面「【路径 B 前置】在源机器备份数据库」那节，直接从 §1 开始
- 选了 **B**：先走「【路径 B 前置】在源机器备份数据库」产出 `earthworm-dump.sql` 拷贝到新机器，再从 §1 开始。然后在 §3.4 中按「路径 B」恢复

---

## 【路径 B 前置】在源机器备份数据库

### 一键脚本（推荐）

```powershell
# 在**当前这台本地机器**运行
backup-db.ps1
```

产出 `earthworm-dump-YYYYMMDD-HHMMSS.sql`，拷贝到 U 盘 / 云盘 / 资源包中转。

### 手工命令等价写法

```powershell
mysqldump -u root -p `
    --single-transaction `
    --default-character-set=utf8mb4 `
    --routines --triggers `
    earthworm > earthworm-dump.sql
```

> `--single-transaction` 在不锁表的前提下产出一致快照。

### 备份包含什么？

| 表                                                       | 内容                                                      |
| -------------------------------------------------------- | --------------------------------------------------------- |
| `course_packs` / `courses` / `statements`                | 25 包 + 1 841 课时 + 19 103 句词（跳过也行，seed 会重建） |
| `users` / `user_accounts`                                | 所有账号与密码 hash                                       |
| `course_history`                                         | 每人的闯关进度 / 对错统计 / 连击                          |
| `wrong_answer_items` / `vocabulary_words` / `mastered_*` | 错题本 / 生词本 / 掌握列表                                |
| `review_tasks`                                           | SRS 复习安排                                              |
| `study_groups` / `study_group_members`                   | 学习小组                                                  |
| `battle_*`                                               | PK 房间 / 记录                                            |
| `flyway_schema_history`                                  | 迁移历史（恢复后 Flyway 会跳过重应用）                    |

### 不需要备份的东西

| 项                         | 位置                                                               | 为什么                                                |
| -------------------------- | ------------------------------------------------------------------ | ----------------------------------------------------- |
| 课程包 / TORFL 词表 / 例句 | `backend/src/main/resources/torfl/levels/*.json`、`customs/*.json` | 在 git 里，克隆仓库就有                               |
| `/feedback` 留言           | 浏览器 localStorage `gusi-feedback-messages-v1`                    | 在客户端，服务器 / 数据库里不存。如果要迁移需手动导出 |
| Maven / pnpm 本地仓库      | `~/.m2/`、`%LOCALAPPDATA%\pnpm`                                    | 新机器走 `mvn`/`pnpm install` 会重新下载              |

---

## 0. 当前本机环境概览（参照基线）

| 组件              | 版本                                         | 端口 |
| ----------------- | -------------------------------------------- | ---- |
| Windows           | 10 / 11 64-bit                               | —    |
| OpenJDK           | Eclipse Temurin **21**                       | —    |
| Maven             | 3.9+                                         | —    |
| Node.js           | **20+** LTS                                  | —    |
| pnpm              | 9.x                                          | —    |
| MySQL             | **8.x**（root / `***REDACTED***` / `earthworm`） | 3306 |
| Spring Boot 后端  | 自带                                         | 8080 |
| Nuxt 3 前端       | 自带                                         | 3000 |
| ngrok（可选公网） | ≥ 3.20.0                                     | —    |

---

## 1. 安装系统软件

### 1.1 一键脚本（推荐）

仓库根目录有 `setup.ps1`，会自动用 `winget` 装好 Git / JDK 21 / Maven / Node / pnpm / MySQL：

```powershell
# 在仓库根目录，PowerShell 普通权限即可
.\setup.ps1
```

> 脚本是幂等的，已装的会跳过；缺什么装什么。

### 1.2 手工安装（如果 winget 不可用）

| 组件        | 下载地址                                            | 安装注意                                             |
| ----------- | --------------------------------------------------- | ---------------------------------------------------- |
| Git         | <https://git-scm.com/download/win>                  | 默认即可                                             |
| JDK 21      | <https://adoptium.net/temurin/releases/?version=21> | 选 `.msi`，**勾上「Add to PATH」+「Set JAVA_HOME」** |
| Maven       | <https://maven.apache.org/download.cgi>             | 解压后把 `bin/` 加 PATH                              |
| Node.js LTS | <https://nodejs.org/>                               | 装 20.x 或 22.x LTS                                  |
| pnpm        | `npm install -g pnpm`                               | 必须 ≥ 9.x                                           |
| MySQL 8     | <https://dev.mysql.com/downloads/installer/>        | 安装时**记下 root 密码**                             |

### 1.3 安装后自检

```powershell
git --version          # >= 2.40
java -version          # openjdk version "21.x"
mvn --version          # Apache Maven 3.9+
node --version         # v20+ 或 v22+
pnpm --version         # 9.x
mysql --version        # mysql Ver 8.x
```

---

## 2. 拉取代码

```powershell
git clone <你的仓库 URL> earthworm-main
cd earthworm-main
pnpm install            # 第一次约 2-4 分钟
```

> 私有仓库：先 `gh auth login` 或配 SSH key。

---

## 3. 数据库

### 3.1 新建空库（必做）

```powershell
mysql -u root -p
```

进入 MySQL Shell 后：

```sql
CREATE DATABASE earthworm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### 3.2 修改连接信息（如果你的 root 密码不是默认值）

打开 `backend/src/main/resources/application.yml`，改这两行：

```yaml
spring:
  datasource:
    username: root # ← 你的 MySQL 用户名
    password: ***REDACTED*** # ← 你的 MySQL 密码
```

> ⚠️ 不建议直接改 yml；推荐用环境变量覆盖（见 §4），避免密码进 git。

### 3.3 自动建表 + 自动 seed（无需手工导入）

后端**首次启动**时会做两件事：

1. **Flyway** 自动跑 `backend/src/main/resources/db/migration/V1~V5.sql`，建出全部表结构
2. **TorflPackService + CustomCoursePackService** 自动从 `resources/torfl/levels/*.json` 和 `resources/customs/*.json` seed 出 25 个课程包

完成后数据库内容应是：

| 表             | 行数   |
| -------------- | ------ |
| `course_packs` | 25     |
| `courses`      | 1 841  |
| `statements`   | 19 103 |

### 3.4 【路径 B】从 `earthworm-dump.sql` 恢复动态数据

> 如果你选的是路径 A（全新部署），跳过本小节，直接看 §4。

把从源机器那边拷过来的 `earthworm-dump.sql` 放在仓库根目录，然后在目标机器上运行：

```powershell
mysql -u root -p earthworm < earthworm-dump.sql
```

验证表已填充：

```powershell
mysql -u root -p earthworm -e "SELECT COUNT(*) AS packs FROM course_packs; SELECT COUNT(*) AS courses FROM courses; SELECT COUNT(*) AS statements FROM statements;"
```

应该看到同样的 25 / 1 841 / 19 103，加上你之前产生的用户 / 学习 / 错题 表行数。

**重点**：后端首次启动时会检测到表里已经有 25 个包 → 自动跳过 seed，你从源机器带过来的所有动态数据不会被覆盖。

---

## 4. 环境变量

只有 AI 相关变量是**必填**（没有的话 AI 助手会显示「AI not configured」，但其它功能不受影响）。

```powershell
# OpenRouter API（推荐，每月 1 美元额度免费）
[Environment]::SetEnvironmentVariable("OPENROUTER_API_KEY", "sk-or-v1-...", "User")
[Environment]::SetEnvironmentVariable("OPENROUTER_MODEL",   "openai/gpt-4o-mini", "User")

# 〔可选〕指向本地 OneAPI / vLLM
# [Environment]::SetEnvironmentVariable("OPENROUTER_BASE_URL", "http://localhost:3000/v1", "User")

# 〔可选〕用环境变量覆盖数据库密码（推荐，避免改 yml）
# [Environment]::SetEnvironmentVariable("SPRING_DATASOURCE_PASSWORD", "你的密码", "User")
```

设完**新开 PowerShell** 才生效。

---

## 5. 第一次启动（最关键的一步）

### 5.1 启动后端（前台跑，方便看 Flyway / seed 日志）

```powershell
mvn -f backend/pom.xml spring-boot:run
```

观察日志依次出现：

```
... Flyway Community Edition ...
... Successfully applied 5 migrations ...
... TorflPackService: seeding 6 TORFL packs ...
... CustomCoursePackService: seeding 2 custom packs ...
... Started EarthwormApplication in 12.345 seconds ...
... Tomcat started on port(s): 8080 (http) ...
```

如果停在「Successfully applied N migrations」之后没有 seed 日志，可能 seed 已被跳过（表里已经有数据）— 正常。

### 5.2 验证后端

新开一个 PowerShell：

```powershell
(Invoke-RestMethod http://localhost:8080/admin/stats).totals | ConvertTo-Json
```

应输出：

```json
{
  "packs": 25,
  "courses": 1841,
  "statements": 19103
}
```

数字不对？检查 `backend-spring.err.log` 里有没有 SQL 报错。

### 5.3 启动前端

```powershell
pnpm --filter client dev
```

第一次启动 Nuxt 大约 30-60 秒（要构建 Vite 缓存）。

### 5.4 浏览器访问

- 主页：<http://localhost:3000>
- 课程库：<http://localhost:3000/course-pack>
- 后台总览：<http://localhost:3000/dashboard>（会显示 25 / 1841 / 19103）

---

## 6. 一键启动脚本（之后日常使用）

仓库根目录已经有：

```powershell
.\run-local.ps1     # 干净重启 backend (8080) + frontend (3000)，日志写到根目录
```

执行结束后会有 `Local app is running.` 提示。

停止：

```powershell
Get-NetTCPConnection -LocalPort 8080,3000 -State Listen | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force
}
```

---

## 7. 〔可选〕公网访问 ngrok

如果只是在内网用 / 演示给在身边的人看，**跳过本节**。

### 7.1 安装

```powershell
winget install --id=ngrok.ngrok --silent
ngrok update                # 必须 ≥ 3.20.0
ngrok --version
```

### 7.2 写入 token

```powershell
ngrok config add-authtoken <你的 token>
```

token 在 <https://dashboard.ngrok.com/get-started/your-authtoken> 拿（免费版即可）。

### 7.3 改写配置文件（单 tunnel + 反向代理）

把 `%LOCALAPPDATA%\ngrok\ngrok.yml` 改成：

```yaml
version: "2"
authtoken: <你的 token>
tunnels:
  app:
    proto: http
    addr: 3000
```

只暴露前端 :3000；后端 :8080 由 **Nuxt 内置的 routeRules 反向代理** 转发，配置已经在 `apps/client/nuxt.config.ts`：

```ts
routeRules: {
  "/api/backend/**": { proxy: "http://localhost:8080/**" },
}
```

### 7.4 启动公网

```powershell
ngrok start --all
```

控制台会打印类似：

```
Forwarding   https://undoing-whiff-crabmeat.ngrok-free.dev -> http://localhost:3000
```

把这个 https URL 发给任何人，他们在浏览器打开就能用。第一次会弹 ngrok 拦截页，点 Visit Site 即可。

> 免费版的域名固定绑你的账号，关掉 ngrok 再起还是同一个 URL。

---

## 8. 数据再生（可选，更新词表 / 例句）

如果只是部署不打算改数据，**跳过本节**。

### 8.1 Python 工具链准备

```powershell
# 推荐 Python 3.11+
pip install pymorphy3 ujson tqdm openai pandas openpyxl
```

### 8.2 工作目录

```
scripts/
├── dsl-import/      BKRS DSL 词典 → TORFL A1-C2 词表
├── sentence-import/ Tatoeba + AI → 例句
└── custom-import/   xlsx/csv → 专业课程包 JSON
```

### 8.3 命令速查

```powershell
# 重新生成 TORFL 词表（需先放 BKRS DSL 到 scripts/dsl-import/dictionaries/）
python scripts/dsl-import/parse_dsl.py
python scripts/dsl-import/build_levels.py
python scripts/dsl-import/quality_report.py

# 重新拉 Tatoeba 例句
python scripts/sentence-import/fetch_tatoeba.py
python scripts/sentence-import/add_sentences.py

# AI 例句生成（需 OPENROUTER_API_KEY）
python scripts/sentence-import/build_term_lists.py
python scripts/sentence-import/ai_generate.py --batch 10 --resume
python scripts/sentence-import/inject_ai_sentences.py

# 重新生成专业包 JSON
python scripts/custom-import/build_baby_care.py
python scripts/custom-import/build_oil_engineering.py
```

生成完调一次后台 reseed：

```powershell
Invoke-RestMethod -Method POST http://localhost:8080/admin/torfl-pack/reseed
Invoke-RestMethod -Method POST http://localhost:8080/admin/custom-pack/reseed
```

---

## 9. 排错速查

### Q: 后端启动时 Flyway 校验失败

```
Validate failed: Migration checksum mismatch ...
```

**A**：你修改了已经执行过的 migration 文件。要么改回原内容，要么手动执行：

```sql
DELETE FROM earthworm.flyway_schema_history WHERE version = '<出错的版本号>';
```

然后重新启动。**生产环境绝不能这么干**。

### Q: 端口被占用

```powershell
# 找进程
Get-NetTCPConnection -LocalPort 8080,3000 -State Listen
# 杀进程
Stop-Process -Id <PID> -Force
```

### Q: pnpm install 报「ENOENT lockfile」

```powershell
Remove-Item -Recurse -Force node_modules, .pnpm-store
pnpm install
```

### Q: 后端启动后 stats 显示 packs=0

说明 seed 没跑。检查日志里有没有 `TorflPackService: seeding`，如果没有，可能是 `application.yml` 的 datasource 连不上数据库。先用 `mysql -u root -p` 手动连一次确认账密。

### Q: 前端显示「Failed to fetch / ERR_CONNECTION_REFUSED」

后端没起或 :8080 没起来。先 `Invoke-RestMethod http://localhost:8080/admin/stats` 自检后端。

### Q: ngrok 报「ngrok-agent version too old」

```powershell
ngrok update
```

ngrok 账号要求最低版本 3.20.0+。

### Q: 公网访问者看到 ngrok 警告页

免费版默认有，点「Visit Site」跳过即可。要想去掉得升级到付费 plan。

---

## 10. 验收清单

部署完后逐项打勾：

- [ ] `mvn -f backend/pom.xml spring-boot:run` 启动成功，日志无 ERROR
- [ ] `Invoke-RestMethod http://localhost:8080/admin/stats` 返回 `{packs:25, courses:1841, statements:19103}`
- [ ] `pnpm --filter client dev` 启动成功
- [ ] 浏览器打开 <http://localhost:3000> 能看到「鹅语菌 · Джунда русский」首页
- [ ] <http://localhost:3000/course-pack> 能看到 25 个课程包，分 6 个系列 tab
- [ ] <http://localhost:3000/dashboard> 显示 25 / 1841 / 19103 三大计数 + 系列分布进度条
- [ ] <http://localhost:3000/help> 能看到帮助文档，4 个分区卡 + 快捷键速查
- [ ] <http://localhost:3000/feedback> 能写留言、提交后出现在「我的留言」列表
- [ ] 〔可选〕`ngrok start --all` 启动后浏览器开公网 URL 也能正常使用

全部勾上 = 部署完成。✅
