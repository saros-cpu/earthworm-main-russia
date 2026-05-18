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

| 指标     | 数字                                                                              |
| -------- | --------------------------------------------------------------------------------- |
| 课程包   | **25 个**                                                                         |
| 课时     | **1 841 节**                                                                      |
| 练习句词 | **19 103 条**                                                                     |
| 覆盖     | 俄语字母 → 入门 → TORFL A1-C2 → 走遍俄罗斯 1-4 册 → 婴幼儿护理 + 加油站/石油/工程 |

按系列分布：

| 系列                                          | 包  | 课时 | 练习项 |
| --------------------------------------------- | --- | ---- | ------ |
| **专业领域**（婴幼儿护理 + 加油站·石油·工程） | 2   | 750  | 7 410  |
| **TORFL 等级备考**（A1-C2）                   | 6   | 687  | 6 815  |
| 走遍俄罗斯 · 教材正本 1-10                    | 10  | 271  | 3 211  |
| 走遍俄罗斯 · 自学辅导 1-4                     | 4   | 72   | 844    |
| 词汇专项                                      | 2   | 51   | 732    |
| 入门基础                                      | 1   | 4    | 28     |

## 二、技术栈

| 层       | 选型                                                        |
| -------- | ----------------------------------------------------------- |
| 前端     | **Nuxt 3** + Vue 3 + TypeScript + Pinia + UnoCSS + Tailwind |
| 后端     | **Spring Boot 3** + **Java 21** + JPA/Hibernate + Flyway    |
| 数据库   | **MySQL 8**                                                 |
| AI       | OpenRouter（默认 `openai/gpt-4o-mini`，可换）               |
| 俄语 NLP | pymorphy3（形态学还原）                                     |
| 句库     | Tatoeba 俄汉平行语料（11 301 对母语句对）                   |
| 词典     | BKRS 大俄汉词典（DSL 格式）                                 |
| 频率表   | Leeds Russian Frequency List                                |

## 三、快速启动

### 准备

```powershell
# 先决条件：JDK 21、Node 20+、MySQL 8、pnpm 8+
pnpm install

# 创建数据库
mysql -e "CREATE DATABASE earthworm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 编辑 backend/src/main/resources/application.yml 中的 datasource 用户名和密码

# 可选：开启 AI 精炼
$env:OPENROUTER_API_KEY = "sk-or-v1-..."
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
cd apps\client
pnpm build          # 编译打包
pnpm preview        # 启动生产服务（端口 3000）
```

> 生产模式下所有页面组件预编译为静态 JS 文件，避免 Vite 开发模式下逐个加载 `.vue` 文件导致的网络连接过多问题，适合办公室等防火墙严格的环境。

### 访问地址

- 前端 UI：http://localhost:3000
- 后端 API：http://localhost:8080
- 后台管理：http://localhost:3000/admin（仅管理员可见）
- 管理员账号：`yangjie` / `123456`

## 四、权限体系

| 角色                 | 权限                                                     |
| -------------------- | -------------------------------------------------------- |
| **游客**（未登录）   | 仅可查看「入门基础」课程包，点击「先玩一课」进入课程包页 |
| **普通用户**（USER） | 查看全部课程包、正常练习                                 |
| **管理员**（ADMIN）  | 额外可见「课程编辑器」菜单，可访问后台管理页面           |

> 新增用户默认角色为 `USER`，需手动修改数据库 `users.role` 字段提升为 `ADMIN`。

## 五、课程包管理

### Logo 素材

项目根目录 `Logo/` 存放品牌素材：

| 文件                                 | 用途                                           |
| ------------------------------------ | ---------------------------------------------- |
| `Logo/中亚能源logo（双语）.png`      | 导航栏左上角 Logo（含品牌名+圆形图标，263×48） |
| `apps/client/public/logo-circle.png` | 其他位置使用的圆形黑红无文字 Logo（256×256）   |

### 数据生成管线

```bash
# 生成 TORFL 词汇包（A1-C2 六个等级）
# 走遍俄罗斯教材课程包
# PDF 自动导入课程包
```

### 2. 数据库

```sql
CREATE DATABASE earthworm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `backend/src/main/resources/application.yml` 里的 `spring.datasource.username/password` 为你的 MySQL 账号。

Flyway 会在第一次启动时自动建表 + 执行所有 migration。

### 3. 环境变量

```powershell
$env:OPENROUTER_API_KEY = "sk-or-v1-..."   # 可选：开启 AI 助手 / AI 课程生成
$env:OPENROUTER_MODEL   = "openai/gpt-4o-mini"
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

第一次启动后端时会自动 seed 全部课程数据（TORFL 6 包 + 专业 2 包 + 走遍俄罗斯等），无需手动导入。

## 四、目录结构

```
earthworm-main/
├── apps/client/                 Nuxt 前端
│   ├── pages/
│   │   ├── index.vue            登录前 Landing / 登录后 Home
│   │   ├── course-pack/         课程包列表 + 详情
│   │   ├── dashboard.vue        ★ 课程库总览（实时统计 + 一键重灌）
│   │   └── admin.vue            后台管理（PDF 上传 / 词条编辑 / AI 生成）
│   └── components/
├── backend/                     Spring Boot 后端
│   └── src/main/
│       ├── java/com/earthworm/
│       │   ├── controller/      REST API
│       │   ├── service/         业务逻辑
│       │   │   ├── TorflPackService.java       TORFL 6 包种子加载
│       │   │   ├── CustomCoursePackService.java 专业包种子加载
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

## 五、数据管线

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
- 自动注入到每个 TORFL 等级的 `sentences` 数组，TorflPackService 在 reseed 时把每 10 条切成"组词成句"小课。

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

落盘到 `backend/src/main/resources/customs/*.json`，后端启动时由 `CustomCoursePackService` 自动 seed。

## 六、后台与一键重灌

### `/dashboard` 总览页

实时显示：

- 三大计数（包 / 课 / 句词）
- 按系列分布（含渐变进度条）
- 全部课程包列表（按句数降序，点击直达详情）
- 两个一键重灌按钮：`重灌 TORFL` / `重灌专业包`

### 后台 REST API

| Method          | 路径                                       | 说明                                         |
| --------------- | ------------------------------------------ | -------------------------------------------- |
| GET             | `/admin/stats`                             | 聚合统计（dashboard 用）                     |
| POST            | `/admin/torfl-pack/reseed`                 | 从 `torfl/levels/*.json` 重新生成 TORFL 6 包 |
| POST            | `/admin/custom-pack/reseed`                | 从 `customs/*.json` 重新生成专业包           |
| GET             | `/admin/course-packs`                      | 全部包列表（带 metadata）                    |
| GET / PUT       | `/admin/course-packs/{id}`                 | 单包详情 / 编辑                              |
| POST            | `/admin/course-packs/{id}/courses`         | 新建课时                                     |
| POST/PUT/DELETE | `/admin/courses/{id}/...`                  | CRUD 课时与练习句                            |
| POST            | `/admin/course-packs/{id}/generate-course` | AI 生成一节新课                              |
| POST            | `/admin/pdf-import-jobs/local-directory`   | 批量导入本机 PDF 目录                        |
| POST            | `/admin/vocabulary-course-pack`            | 高频词汇课程包生成                           |

## 七、典型工作流

### 加一个新词表 / 教材

1. 把素材整理成 xlsx 或 csv（参考 `scripts/custom-import/build_*.py`）
2. 写一个 `build_xxx.py` 输出到 `backend/src/main/resources/customs/xxx.json`
3. 重启后端 或 `POST /admin/custom-pack/reseed`
4. 在 `/course-pack` 检查上线

### 用 Tatoeba + AI 补例句

1. 先跑 `fetch_tatoeba.py` + `add_sentences.py` 把母语句尽可能多注入
2. 用 `build_term_lists.py` 找出仍然缺例句的术语
3. `ai_generate.py` 小批量生成
4. `inject_ai_sentences.py` 注入 + `POST /admin/torfl-pack/reseed`

### 审核 A1-C2 词条质量

跑 `quality_report.py` → 打开 `output/reports/A?_quality.csv` → 按 `action=replace / review` 排序 → 人工替换问题词条 → 保存 JSON → reseed。

## 八、常见问题

**Q: 后端启动报 Flyway 校验失败？**
A: 删除 `flyway_schema_history` 表或在 `application.yml` 临时把 `ddl-auto: validate` 改成 `update`，但生产环境千万别这么干。

**Q: AI 助手返回"AI not configured"？**
A: 检查 `OPENROUTER_API_KEY` 环境变量；进 https://openrouter.ai/keys 看额度。

**Q: 重灌之后用户做题进度会丢吗？**
A: 不会。reseed 只删 / 重建课程包与课时（id 是按 hash 稳定的），用户的 `course_history` 表通过 `course_id` 关联，重建后会重新对上。但如果你 **修改了 statement 的内容**（不是删除），旧的错题本/掌握记录可能指向已被替换的句子，需要在 admin 里手动整理。

**Q: 一定要 OpenRouter 吗？**
A: 不一定。`application.yml` 中改 `openai.baseUrl` 和 `openai.model` 即可指向任何 OpenAI-兼容端点（如 OneAPI、本地 vllm）。AI 助手 / 课程生成 / 句子生成三处都是 OpenAI Chat Completions 协议。

**Q: 想把本地服务暴露到公网（演示给别人用）？**
A: 推荐 ngrok 单 tunnel + Nuxt routeRules 反代后端的方案，已内置在 `nuxt.config.ts`（默认 `apiBase=/api/backend` 自动反代到 `:8080`）。详见 [`DEPLOYMENT.md`](./DEPLOYMENT.md) 末尾的 _公网访问_ 一节。

**Q: 换电脑要重新部署？**
A:

- **新机器从零跑**（不带历史数据）：看 [`DEPLOYMENT.md`](./DEPLOYMENT.md)，约 30 分钟。
- **完整迁移到固定 IP 服务器**（带账号 / 学习记录 / 错题本，生产模式 24/7）：看 [`MIGRATE.md`](./MIGRATE.md)，约 45-60 分钟。仓库根目录已带 `backup-db.ps1` / `prod-start.ps1` / `prod-stop.ps1` 三个脚本。

## 九、致谢

- 上游项目 [Earthworm](https://github.com/cuixueshe/earthworm) 提供了练习交互层与前端骨架
- [Tatoeba](https://tatoeba.org) 提供了高质量俄汉平行语料
- [BKRS](https://bkrs.info) 大俄汉词典
- [pymorphy3](https://github.com/no-plagiarism/pymorphy3) 俄语形态学分析
- [Leeds Russian Frequency List](http://corpus.leeds.ac.uk/list.html)

## 十、License

Continuing the upstream MIT license. See [LICENSE](./LICENSE).
