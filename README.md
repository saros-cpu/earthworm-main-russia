<div align="center">
  <img alt="中大俄语" width="120" height="120" src="./apps/client/public/logo-circle.png">
  <h1>中大俄语 · Джунда русский</h1>
  <p>面向中文母语者的俄语「输入打字 + 听读 + 即时反馈」练习平台</p>
  <p>
    <strong>中文</strong> · <a href="./README.zh-CN.md">English</a>
  </p>
</div>

---

> 本仓库源自 [cuixueshe/earthworm](https://github.com/cuixueshe/earthworm)（英语连词成句训练）的深度改造分支。**整套后端 / 数据库 / 课程数据已全部重写为俄语版**，品牌已全面替换为「中大俄语」。

## 一、当前状态（v2026.05）

| 指标 | 数字 |
|------|------|
| 课程包 | **94 个** |
| 课时 | **2 400+ 节**（含视频/音频媒体课程） |
| 练习句词 | **19 103+ 条** |
| 覆盖 | 俄语字母 → 入门 → TORFL A1-C2 → 走遍俄罗斯 1-4 册 → 婴幼儿护理 + 加油站/石油/工程 |

按系列分布：

| 系列 | 包 | 课时 | 练习项 |
|---|---|---|---|
| **专业领域**（婴幼儿护理 + 加油站·石油·工程） | 2 | 750 | 7 410 |
| **TORFL 等级备考**（A1-C2） | 6 | 687 | 6 815 |
| 走遍俄罗斯 · 教材正本 1-10 | 10 | 271 | 3 211 |
| 走遍俄罗斯 · 自学辅导 1-4 | 4 | 72 | 844 |
| 词汇专项 | 2 | 51 | 732 |
| 入门基础 | 1 | 4 | 28 |

## 二、技术栈

| 层 | 选型 |
|---|---|---|
| 前端 | **Nuxt 3.21** + Vue 3.5 + TypeScript 6 + Pinia + Tailwind + DaisyUI + Nuxt UI v2 |
| 后端 | **Spring Boot 3.4** + **Java 21** + JPA/Hibernate + Flyway |
| 数据库 | **MySQL 8** |
| AI | OpenRouter（默认 `openai/gpt-4o-mini`，可换） |
| 俄语 NLP | pymorphy3（形态学还原） |
| 句库 | Tatoeba 俄汉平行语料（11 301 对母语句对） |
| 词典 | BKRS 大俄汉词典（DSL 格式） |
| 频率表 | Leeds Russian Frequency List |
| 测试 | Vitest 1 + Cypress 13 + Spring Boot Test |
| CI/CD | GitHub Actions（lint + build + test + CodeQL） |

## 三、快速启动

### 准备

```powershell
# 先决条件：JDK 21、Node 20+、MySQL 8、pnpm 8+
pnpm install

# 创建数据库
mysql -e "CREATE DATABASE earthworm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 通过环境变量提供数据库凭据，勿将密码写入仓库
$env:SPRING_DATASOURCE_USERNAME = "reader"
$env:SPRING_DATASOURCE_PASSWORD = "你的数据库密码"
$env:JWT_SECRET = "请生成随机长密钥"
# 仅本地 HTTP 开发需要；HTTPS 部署不要设置为 false
$env:AUTH_SESSION_COOKIE_SECURE = "false"

# 可选：开启 AI 课程生成和问答
$env:OPENROUTER_API_KEY = "your_openrouter_key_here"
```

### 开发模式（支持热更新）

```powershell
# 一键启动前后端
.\start.bat
```

或分别启动：

```powershell
# 终端 1 — 后端（端口 8080）
mvn -f backend\pom.xml spring-boot:run

# 终端 2 — 前端（端口 3000）
cd apps\client
pnpm dev --port 3000 --host 0.0.0.0
```

### 生产模式（对外网访问更友好）

```powershell
# 一键构建
.\prod-build.ps1

# 一键启动
.\prod-start.ps1

# 查看状态
.\prod-status.ps1

# 停止服务
.\prod-stop.ps1
```

`prod-build.ps1` 执行：
1. `mvn -DskipTests package` — 构建 Spring Boot JAR
2. `pnpm --filter client build` — 构建 Nuxt SSR 服务产物与静态资源

`prod-start.ps1` 执行：
1. 仅停止由上一次生产启动记录的进程；若端口被其他程序占用则中止启动，不会误停
2. 启动后端 JAR（端口 8080）
3. 启动 Nuxt Nitro 服务（`apps/client/.output/server/index.mjs`，端口 3000），自动代理 `/api/backend/*` 到后端

### 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端 UI |
| http://localhost:8080 | 后端 API |
| http://IP:3000 | 局域网/公网访问（如 `http://109.71.228.50:3000`） |
| 后台管理 | http://localhost:3000/admin（仅 ADMIN 角色可见） |

项目不提供默认管理员密码。请创建专用账号、设置强密码后，再以受控方式授予 `ADMIN` 角色。

### 环境变量

| 变量 | 必填 | 说明 |
|------|------|------|
| `JWT_SECRET` | **是** | 生成：`node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"`。**如缺失，后端会拒绝启动** |
| `SPRING_DATASOURCE_URL` | 否 | 默认连接 `jdbc:mysql://127.0.0.1:3306/earthworm`，可按部署环境覆盖 |
| `SPRING_DATASOURCE_USERNAME` | 否 | 默认 `reader` |
| `SPRING_DATASOURCE_PASSWORD` | **是** | MySQL 数据库密码 |
| `CORS_ALLOWED_ORIGINS` | 否 | 默认 `http://localhost:3000,http://localhost:3001` |
| `SEED_WRITE_ON_STARTUP` | 否 | 默认 `false`；仅在空库首次装载内置课程时临时设为 `true` |
| `AUTH_SESSION_COOKIE_SECURE` | 否 | 默认 `true`；仅本地 HTTP 开发设为 `false`，生产部署必须使用 HTTPS 并保持 `true` |
| `OPENROUTER_API_KEY` | 否 | AI 助手 / 课程生成 |
| `OPENAI_ENABLED` | 否 | 默认为 true，设 false 禁用 AI |
| `AI_DAILY_MAX_RESERVED_OUTPUT_TOKENS` | 否 | 默认 `100000`；按日限制 AI 功能预留输出 token 总量，防止异常费用增长 |

### 安全说明

| 项目 | 状态 |
|------|------|
| JWT 密钥 | 从 `JWT_SECRET` 环境变量读取，缺失则启动失败 |
| 数据库密码 | 从 `SPRING_DATASOURCE_PASSWORD` 环境变量读取，**不硬编码在仓库中** |
| CORS | 通过 `CORS_ALLOWED_ORIGINS` 环境变量配置，支持多来源逗号分隔 |
| 浏览器会话 | JWT 仅写入 `HttpOnly` Cookie；写操作启用 CSRF 校验，生产 Cookie 仅通过 HTTPS 发送 |
| Spring Security | 路径级授权已配置（公开路径、GET 课程包、管理后台需认证） |
| JWT 过期 | 7 天，可在 `application.yml` 中调整 `jwt.expirationMs` |
| 种子课程 | 普通启动不写课程数据；空库初始化须显式设置 `SEED_WRITE_ON_STARTUP=true` |
| 管理操作 | 角色、资料修改和课程内容归档会记录审计日志；账号物理删除入口仍停用 |
| AI 用量 | 调用前按日持久化预留输出 token 额度，超限直接拒绝请求 |
| 媒体流 | 路径遍历已防护（`resolveFile()` 做归一化 + 前缀检查） |
| 日志 | 敏感信息不写入日志 |

## 四、权限体系

| 角色 | 权限 |
|------|------|
| **游客**（未登录） | 仅可查看「入门基础」课程包，点击「先玩一课」进入课程包页 |
| **普通用户**（USER） | 查看全部课程包、正常练习 |
| **管理员**（ADMIN） | 额外可见「课程编辑器」菜单，可访问后台管理页面 |

> 新增用户默认角色为 `USER`，需手动修改数据库 `users.role` 字段提升为 `ADMIN`。

## 五、课程包管理

### Logo 素材

项目根目录 `Logo/` 存放品牌素材：

| 文件 | 用途 |
|---|---|
| `Logo/中亚能源logo（双语）.png` | 导航栏左上角 Logo（含品牌名+圆形图标，263×48） |
| `apps/client/public/logo-circle.png` | 其他位置使用的圆形黑红无文字 Logo（256×256） |

### 数据生成管线

```bash
# 生成 TORFL 词汇包（A1-C2 六个等级）
# 走遍俄罗斯教材课程包
# PDF 自动导入课程包（暂未启用）
```

### 2. 数据库

```sql
CREATE DATABASE earthworm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

设置 `SPRING_DATASOURCE_USERNAME` 与 `SPRING_DATASOURCE_PASSWORD` 环境变量连接 MySQL；不要将数据库密码写入 `application.yml`。

Flyway 会在第一次启动时自动建表 + 执行所有 migration。

### 3. 环境变量

```powershell
$env:OPENROUTER_API_KEY = "your_openrouter_key_here"   # 可选：开启 AI 助手 / AI 课程生成
$env:OPENROUTER_MODEL   = "openai/gpt-4o-mini"
$env:AI_DAILY_MAX_RESERVED_OUTPUT_TOKENS = "100000" # 可选：AI 每日用量保护上限
```

### 4. 启动

```powershell
# 后端（端口 8080）
mvn -f backend/pom.xml spring-boot:run

# 前端（端口 3000）
pnpm --filter client dev
```

启动后访问：

- 主页：<http://localhost:3000>
- 课程库：<http://localhost:3000/course-pack>
- 帮助文档：<http://localhost:3000/help>（在网页内置）
- 建议反馈：<http://localhost:3000/feedback>（在网页留言，本地存储）
- 后台总览：<http://localhost:3000/dashboard>
- 后台管理 / 课程编辑器：<http://localhost:3000/admin>

空数据库首次装载内置课程时，先临时设置 `$env:SEED_WRITE_ON_STARTUP = "true"` 再启动后端；初始化确认完成后移除该变量或改回 `false`。普通启动默认不会改写课程数据。

## 六、目录结构

```
earthworm-main/
├── apps/client/                 Nuxt 前端（SPA 模式）
│   ├── pages/                   28 个页面路由
│   │   ├── index.vue            登录前 Landing / 登录后 Home
│   │   ├── course-pack/         课程包列表 + 详情（含 i18n 中俄双语）
│   │   ├── game/                学习游戏主界面（含多种答题模式）
│   │   ├── media-course/
│   │   │   ├── index.vue             媒体课程列表（按系列分组）
│   │   │   ├── [coursePackId].vue    单个媒体课程包详情
│   │   │   └── detail/               媒体播放页（播放器 / 播放+答题 / 音频+字幕三种模式）
│   │   ├── dashboard.vue        ★ 课程库总览（实时统计，重灌已停用）
│   │   ├── stats.vue            用户学习统计（日历热力图 + 趋势图）
│   │   ├── vocabulary.vue       用户词本（生词管理）
│   │   ├── review.vue           复习页面
│   │   ├── groups.vue           学习小组
│   │   ├── battle.vue           多人对战
│   │   └── admin/               后台管理（PDF 上传 / 词条编辑 / AI 生成 / 用户管理）
│   ├── components/
│   │   ├── media/               媒体组件
│   │   │   ├── VideoPlayer.vue   视频/音频播放器（含转码重试、变速播放）
│   │   │   ├── VideoQuiz.vue     视频 + 答题模式
│   │   │   └── AudioLrcPlayer.vue 音频 + 字幕模式（支持 LRC 时间轴同步、点击跳转）
│   │   ├── main/                学习游戏核心组件
│   │   │   ├── MainGame.vue      游戏主组件
│   │   │   ├── Summary.vue       学习摘要/总结
│   │   │   └── AiAssistant.vue   AI 语法助手浮窗
│   │   ├── mode/                学习模式组件（中译俄 / 听写 / 组词 / 语音评测等）
│   │   ├── Home/                首页组件（最近课程包、日历热力图）
│   │   └── Landing/             落地页组件（Banner / Features / FAQ）
│   ├── locales/                 i18n 语言包（ru.json + zh.json）
│   ├── store/                   Pinia 状态管理
│   └── composables/             可复用组合式函数
├── packages/                    共享包
│   ├── game-data-sdk/           游戏数据 SDK
│   └── xingrong-courses/         Xingrong 课程数据
├── backend/                     Spring Boot 后端
│   └── src/main/
│       ├── java/com/earthworm/
│       │   ├── config/          安全配置（JWT、CORS、SecurityConfig）
│   │   ├── service/         业务逻辑
│   │   │   ├── MediaService.java         媒体文件解析、ffmpeg 转码
│   │   │   ├── TorflPackService.java       TORFL 6 包种子加载
│   │   │   ├── CustomCoursePackService.java 专业包种子加载
│       │   │   ├── AdminCourseService.java     stats / pack 管理
│       │   │   ├── CourseGenerationService.java AI 单课程生成
│       │   │   └── AiAssistantService.java     AI 语法助手
│       │   └── repository/
│       └── resources/
│           ├── torfl/levels/    A1-C2 词表 + 例句 JSON
│           ├── customs/         baby_care.json / oil_engineering.json
│           └── db/migration/    Flyway SQL
├── scripts/                     ★ 数据生成管线
│   ├── dsl-import/              BKRS → TORFL 词表 + HSK 质量报告
│   ├── custom-import/           xlsx/csv → 专业课程包 JSON
│   └── sentence-import/         Tatoeba + AI 例句生成
└── .tools/                      Playwright 端到端 diag 脚本
```

## 七、数据管线

### 5.1 TORFL A1-C2 词表生成（BKRS + Leeds + HSK）

```powershell
# 一次性下载 ~250 MB BKRS DSL 词典到 scripts/dsl-import/dictionaries/
python scripts/dsl-import/parse_dsl.py        # 解析 DSL → bkrs_pairs.jsonl
python scripts/dsl-import/build_levels.py     # 按 Leeds 频率切到 A1-C2 + 手工词合并
python scripts/dsl-import/quality_report.py   # 输出 A1-C2 质量审计 CSV
```

质量报告位于 `scripts/dsl-import/output/reports/`，每行包含 `russian / chinese / pos / source / hsk_band / confidence / action / notes`，可直接 Excel 排序快速人工审校。

### 5.2 例句自动补齐（Tatoeba 母语句对）

```powershell
python scripts/sentence-import/fetch_tatoeba.py   # 下载 + 合并 RU/ZH/links
python scripts/sentence-import/add_sentences.py   # pymorphy3 形态还原 + 索引 + 注入到 A1-C2.json
```

- 共 1.2M 俄语句 / 8.6 万中文句 → **11 301 对俄汉平行句**（母语人写，语法 100% 保证）
- 自动注入到每个 TORFL 等级的 `sentences` 数组；全新初始化或经审核的数据迁移可将其发布为课程。

### 5.3 AI 例句生成（OpenRouter gpt-4o-mini）

用于 Tatoeba 覆盖不到的术语 / C2 高级词：

```powershell
python scripts/sentence-import/build_term_lists.py
# 输出 oil_terms.json (~3000 个加油站·石油·工程术语)
# 输出 c2_missing_terms.json (尚无例句的 C2 词)

python scripts/sentence-import/ai_generate.py `
    --input  scripts/sentence-import/output/oil_terms.json `
    --output scripts/sentence-import/output/oil_sentences.jsonl `
    --batch 10 --resume

python scripts/sentence-import/inject_ai_sentences.py
```

每条 AI 输出经过以下校验后才入库：
- 必须含西里尔字母
- Cyrillic 词数 4-16
- 必须含非空中文翻译
- 失败行单独记录，可 `--resume` 重跑

输出示例：

```json
{
  "term": "АЗС",
  "russian": "Мы заехали на автозаправочную станцию за бензином.",
  "chinese": "我们去加油站加油。",
  "note": "АЗС 是缩写，作名词使用时保持原形。"
}
```

### 5.4 专业课程包导入（xlsx / csv → JSON）

```powershell
python scripts/custom-import/build_baby_care.py        # 婴幼儿护理俄语 → customs/baby_care.json
python scripts/custom-import/build_oil_engineering.py  # 加油站·石油·工程 → customs/oil_engineering.json
```

落盘到 `backend/src/main/resources/customs/*.json`。空库初始化时可通过 `SEED_WRITE_ON_STARTUP=true` 导入；已有业务库应通过备份、测试验证后的受控迁移发布，普通重启不会写入。

## 八、后台与数据保护

### `/dashboard` 总览页

实时显示：

- 三大计数（包 / 课 / 句词）
- 按系列分布（含渐变进度条）
- 全部课程包列表（按句数降序，点击直达详情）
- 重灌操作已停用，避免覆盖现有学习记录

### 后台 REST API

| Method | 路径 | 说明 |
|---|---|---|
| GET | `/admin/stats` | 聚合统计（dashboard 用） |
| POST | `/admin/torfl-pack/reseed` | 已停用：保护学习记录 |
| POST | `/admin/custom-pack/reseed` | 已停用：保护学习记录 |
| GET | `/admin/course-packs` | 全部包列表（带 metadata） |
| GET / PUT | `/admin/course-packs/{id}` | 单包详情 / 编辑 |
| POST | `/admin/course-packs/{id}/courses` | 新建课时 |
| POST/PUT | `/admin/courses/{id}/...` | 新建 / 编辑课时与练习句 |
| DELETE | `/admin/course-packs/{id}`、`/admin/courses/{id}`、`/admin/statements/{id}` | 安全归档：不物理删除内容或学习历史；可在管理页恢复 |
| POST | `/admin/statements/{id}/refine`、`/admin/courses/{id}/refine-all` | 规则精炼并保存句子辅助信息；不调用付费 AI |
| POST | `/admin/course-packs/{id}/generate-course` | AI 生成一节新课 |
| POST | `/admin/pdf-import-jobs/local-directory` | 已停用：PDF 导入尚未完成 |
| POST | `/admin/vocabulary-course-pack` | 已停用：词汇包工具尚未完成 |

## 九、典型工作流

### 加一个新词表 / 教材

1. 把素材整理成 xlsx 或 csv（参考 `scripts/custom-import/build_*.py`）
2. 写一个 `build_xxx.py` 输出到 `backend/src/main/resources/customs/xxx.json`
3. 在备份或测试库中验证生成的数据
4. 为已有业务库制定受控迁移后，在 `/course-pack` 检查上线

### 用 Tatoeba + AI 补例句

1. 先跑 `fetch_tatoeba.py` + `add_sentences.py` 把母语句尽可能多注入
2. 用 `build_term_lists.py` 找出仍然缺例句的术语
3. `ai_generate.py` 小批量生成
4. `inject_ai_sentences.py` 注入后，在测试库验证并通过受控迁移上线

### 审核 A1-C2 词条质量

跑 `quality_report.py` → 打开 `output/reports/A?_quality.csv` → 按 `action=replace / review` 排序 → 人工替换问题词条 → 保存 JSON → 在测试库验证迁移。

## 十、常见问题

**Q: 后端启动报 Flyway 校验失败？**
A: 不要删除 `flyway_schema_history`，也不要临时启用自动改表。先保留备份、恢复已执行迁移文件的原内容；确需变更结构时新增 migration，并先在测试库验证。

**Q: AI 助手返回"AI not configured"？**
A: 检查 `OPENROUTER_API_KEY` 环境变量；进 https://openrouter.ai/keys 看额度。

**Q: 可以直接重灌现有课程数据吗？**
A: 不可以。重灌入口已停用，以保护现有学习记录。课程内容变更应先备份，在测试库验证后通过受控迁移发布。

**Q: 媒体课程的三种模式有什么区别？**
A: 媒体课程（视频/音频）提供三种查看模式：
- **播放器**（player）：纯播放器界面
- **播放+答题**（quiz）：媒体播放器 + 课程语句列表（点击语句显示答案），语句目前无时间轴自动同步
- **音频+字幕**（lrc）：音频播放器 + 歌词/字幕面板（时间轴自动高亮，点击字幕跳转到对应时间）
- 非 YouTube 课程使用本地组件（支持变速、ffmpeg 自动转码）；YouTube 课程直接嵌入 iframe

**Q: 音频+字幕模式显示的歌词不对？**
A: 只有 `ru-songs`（俄语歌曲）课程包在 seed JSON 中有 `lyrics` 字段。其他课程包从 `statements` 回退生成字幕。如果歌词和音频对不上，说明需要修改 seed JSON 文件。

**Q: 播放+答题和视频内容对不上？**
A: 目前语句（statements）在 seed 数据中没有时间戳字段，暂时无法自动按视频时间轴高亮。这是一个已知限制，需要在 JSON 中加入 `startTime`/`endTime` 后由前端实现同步。

**Q: 一定要 OpenRouter 吗？**
A: 不一定。`application.yml` 中改 `openai.baseUrl` 和 `openai.model` 即可指向任何 OpenAI-兼容端点（如 OneAPI、本地 vllm）。AI 助手 / 课程生成 / 句子生成三处都是 OpenAI Chat Completions 协议。

**Q: 想把本地服务暴露到公网（演示给别人用）？**
A: 推荐 ngrok 单 tunnel + Nuxt routeRules 反代后端的方案，已内置在 `nuxt.config.ts`（默认 `apiBase=/api/backend` 自动反代到 `:8080`）。详见 [`DEPLOYMENT.md`](./DEPLOYMENT.md) 末尾的 *公网访问* 一节。

**Q: 换电脑要重新部署？**
A:
- **新机器从零跑**（不带历史数据）：看 [`DEPLOYMENT.md`](./DEPLOYMENT.md)，约 30 分钟。
- **完整迁移到固定 IP 服务器**（带账号 / 学习记录 / 错题本，生产模式 24/7）：看 [`MIGRATE.md`](./MIGRATE.md)，约 45-60 分钟。仓库根目录已带 `backup-db.ps1` / `prod-start.ps1` / `prod-stop.ps1` 三个脚本。

## 十一、国际化（i18n）

前端使用 **vue-i18n**，默认语言为 **俄语（ru）**，回退语言为 **中文（zh）**。

| 文件 | 说明 |
|------|------|
| `apps/client/locales/ru.json` | 俄语 UI 翻译（主语言） |
| `apps/client/locales/zh.json` | 中文 UI 翻译（fallback） |
| `apps/client/plugins/i18n.ts` | i18n 配置（locale 切换、fallback 策略） |

课程内容本身（语句、翻译）基本是双语（俄语 + 中文），不受 UI 语言切换影响。

## 十二、致谢

- 上游项目 [Earthworm](https://github.com/cuixueshe/earthworm) 提供了练习交互层与前端骨架
- [Tatoeba](https://tatoeba.org) 提供了高质量俄汉平行语料
- [BKRS](https://bkrs.info) 大俄汉词典
- [pymorphy3](https://github.com/no-plagiarism/pymorphy3) 俄语形态学分析
- [Leeds Russian Frequency List](http://corpus.leeds.ac.uk/list.html)

## 十三、License

Continuing the upstream MIT license. See [LICENSE](./LICENSE).
