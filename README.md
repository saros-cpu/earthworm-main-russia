# 鹅语菌本地版

鹅语菌是基于 Earthworm 改造的俄语学习系统，当前目标是做成面向俄语初学者的游戏化学习网站。项目已从原英语学习系统迁移到 Spring Boot + MySQL + Nuxt 本地运行方案，不依赖 Docker。

## 当前状态

- 前台品牌已改为「鹅语菌」。
- 已接入 MySQL 本地数据库 `earthworm`。
- 已支持俄语课程包、俄语字母/单词/句子练习。
- 已支持单词包专项练习：看中文释义或听发音，输入俄语单词。
- 已支持后台 AI 批量补充单词释义、词性说明和短例句。
- 单词补全会自动升级旧数据：已有中文释义但缺少词性/例句的词也会继续补全。
- 已支持答题后展示单词释义、词性、例句和语法提示卡片。
- 已支持单词包按词性和学习阶段自动重排课程。
- 单词包前台详情页已支持词汇结构概览和每课词条数展示。
- 单词包结构概览支持点击筛选，可只查看代词、名词、动词、修饰词、功能词或待精炼课程。
- 单词包详情页新增推荐学习入口和分类直达练习入口。
- 已支持 PDF 课件导入课程包。
- 已支持本机目录批量导入 PDF。
- 已导入 `走遍俄罗斯` 相关 PDF 课程包。
- 已接入 OpenRouter 用于 AI 课程生成和课程精炼。
- 已支持俄语 TTS 发音，本地后端代理并缓存 mp3。
- 已做学习主页、课程包页、课程列表页、练习页、复习本页面的产品化美化。

暂时不做：正式用户体系、支付/会员/套餐、部署上线。

## 技术栈

- 前端：Nuxt 3、Vue 3、Pinia、Tailwind CSS、DaisyUI
- 后端：Spring Boot 3、Java 21、Spring Data JPA、Flyway
- 数据库：MySQL 8
- PDF：PDFBox、Tesseract OCR
- AI：OpenRouter / OpenAI 兼容接口
- 语音：后端代理 Google Translate TTS，并缓存到本地

## 环境要求

| 依赖 | 版本要求 | 验证命令 |
|------|---------|---------|
| Java | 21 (JDK) | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | >= 20.12.2 | `node --version` |
| pnpm | 9.3.0 | `pnpm -v` |
| MySQL | 8.0+ | `mysql -u root -p -e "SELECT VERSION()"` |

确认以上依赖已安装并加入系统 PATH，MySQL 服务已启动。

### 一键安装环境

项目根目录提供了 `setup.ps1` 一键安装脚本，自动检测并安装缺失的依赖：

```powershell
.\setup.ps1
```

脚本支持通过 **winget**（Windows 11 自带）或 **chocolatey** 自动安装以下组件：

- Git
- Eclipse Temurin JDK 21
- Apache Maven
- Node.js LTS
- pnpm（通过 npm 安装）
- MySQL 8
- 运行 `pnpm install` 安装项目前端依赖

> 已安装的组件会被自动跳过，脚本仅安装缺失项。MySQL 安装后需要手动启动服务。

### 手动配置 MySQL

MySQL 安装后，确保服务已启动：

```powershell
# 检查 MySQL 服务状态
Get-Service MySQL80

# 如果未启动
Start-Service MySQL80
```

## 数据库配置

创建数据库（如果尚未创建）：

```sql
CREATE DATABASE IF NOT EXISTS earthworm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

默认连接配置在 `backend/src/main/resources/application.yml`：

```yaml
url: jdbc:mysql://localhost:3306/earthworm
username: root
password: ***REDACTED***
```

如需修改数据库密码或连接信息，编辑 `application.yml` 中的 `spring.datasource` 配置即可。

## AI 配置

AI 功能依赖 OpenRouter 或兼容 OpenAI 接口的服务。需要配置 Windows 用户环境变量：

| 变量名 | 说明 | 推荐值 |
|--------|------|--------|
| `AI_PROVIDER` | AI 服务提供商 | `openrouter` |
| `OPENROUTER_API_KEY` | API 密钥 | (你的密钥) |
| `OPENROUTER_BASE_URL` | API 地址 | `https://openrouter.ai/api/v1` |
| `OPENROUTER_MODEL` | 模型名称 | `openai/gpt-4o-mini` |
| `OPENROUTER_SITE_URL` | 站点地址 | `http://localhost:3000` |
| `OPENROUTER_APP_NAME` | 应用名称 | `Russian Learning` |

设置环境变量（PowerShell 管理员模式）：

```powershell
[Environment]::SetEnvironmentVariable("OPENROUTER_API_KEY", "sk-or-v1-你的密钥", "User")
[Environment]::SetEnvironmentVariable("AI_PROVIDER", "openrouter", "User")
[Environment]::SetEnvironmentVariable("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1", "User")
[Environment]::SetEnvironmentVariable("OPENROUTER_MODEL", "openai/gpt-4o-mini", "User")
[Environment]::SetEnvironmentVariable("OPENROUTER_SITE_URL", "http://localhost:3000", "User")
[Environment]::SetEnvironmentVariable("OPENROUTER_APP_NAME", "Russian Learning", "User")
```

**不要把真实 API Key 提交到仓库。** 运行 `git status` 确认没有误加入 `.env` 或配置文件。

如果要切换到其他 OpenAI 兼容服务（如 DeepSeek、Azure OpenAI），修改 `AI_PROVIDER`、`OPENROUTER_BASE_URL` 和 `OPENROUTER_MODEL` 即可。

## 启动项目

### 一键启动（开发模式）

```powershell
.\run-local.ps1
```

脚本会自动：
1. 从用户环境变量读取 AI 配置
2. 关闭旧进程（端口 8080、3000）
3. 启动 Spring Boot 后端（Maven）
4. 等待后端就绪后启动 Nuxt 前端
5. 等待前端就绪后显示访问地址

启动后访问：

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端首页 |
| http://localhost:8080 | 后端 API |
| http://localhost:3000/admin | 后台管理 |

### 分步启动（便于排查问题）

```powershell
# 1. 停止旧进程
Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
Get-NetTCPConnection -State Listen -LocalPort 3000 -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }

# 2. 启动后端（后台运行）
cd D:\earthworm-main
mvn -f backend\pom.xml spring-boot:run > backend-spring.log 2> backend-spring.err.log

# 3. 新开终端，启动前端
cd D:\earthworm-main\apps\client
pnpm dev --port 3000
```

### 查看日志

```powershell
# 后端日志
Get-Content D:\earthworm-main\backend-spring.log -Tail 50 -Wait

# 后端错误日志
Get-Content D:\earthworm-main\backend-spring.err.log -Tail 50 -Wait

# 前端日志
Get-Content D:\earthworm-main\frontend-nuxt.log -Tail 50 -Wait
```

## 打包与部署

### 后端打包

```powershell
mvn -f backend\pom.xml clean package -DskipTests
```

打包后在 `backend/target/` 下生成可执行 JAR 文件（如 `earthworm-backend.jar`）。

运行打包后的后端（不需要 Maven）：

```powershell
java -jar backend\target\*.jar
```

### 前端打包

```powershell
cd apps\client
pnpm build
```

生成静态文件在 `apps\client\.output\public\`，可直接部署到 Nginx / IIS / 任意静态服务器。

### 一键打包重启

```powershell
# 后端打包 + 重启
mvn -f backend\pom.xml clean package -DskipTests; if ($?) {
    Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
    java -jar backend\target\*.jar > backend-spring.log 2> backend-spring.err.log
}

# 前端打包 + 重启
cd apps\client
pnpm build
# 然后将 .output/public 部署到静态服务器即可
```

## PDF 导入

后台入口：http://localhost:3000/admin

支持两种方式：

- **上传单个 PDF** — 直接选择文件上传
- **本机目录批量导入** — 填写本机绝对路径，批量导入目录内所有 PDF

当前上传/读取限制：**120MB**

本机批量导入会优先生成可用课程包：

- 俄语字母课程
- 高频单词课程：优先用中文释义出题，缺少释义时用发音提示出题
- 句子练习课程

AI 精炼可在后台对具体句子继续执行。

## 语音发音

前端发音地址会请求后端：

```text
GET /tts/ru?text=...
```

后端流程：
1. 检查本地缓存 `runtime/tts-cache`
2. 如果没有缓存，代理请求俄语 TTS
3. 长句自动分段取音频
4. 返回 `audio/mpeg`
5. 将 mp3 缓存到本地

如果后续要上线，建议替换为正式 TTS 服务，如 Azure Speech、Google Cloud TTS、OpenAI TTS 或自建 Piper。

## 新功能特性

### 1. 连击系统 + SSS 评级

练习过程中连续答对会累积连击数，触发动态缩放动画和连击特效。

| 连击数 | 倍率 | 标签 |
|--------|------|------|
| 3-4 | 1.1x | COOL |
| 5-6 | 1.2x | NICE |
| 7-9 | 1.3x | GOOD |
| 10-14 | 1.5x | GREAT |
| 15-19 | 1.7x | AMAZING |
| 20+ | 2.0x | LEGENDARY |

每次课程完成后根据正确率和最大连击给出评级：

| 评级 | 要求 |
|------|------|
| SSS | 正确率 ≥98% 且最大连击 ≥10 |
| SS | 正确率 ≥95% 且最大连击 ≥7 |
| S | 正确率 ≥90% 且最大连击 ≥5 |
| A | 正确率 ≥80% |
| B | 正确率 ≥60% |
| C | 其余情况 |

### 2. 间隔复习系统

基于 SM-2 间隔重复算法，根据每次复习时对每道题的掌握程度（1-5 分），自动计算下次复习时间：

- **答对且轻松 (5分)**：间隔加倍
- **答对但犹豫 (3-4分)**：正常推进
- **答错/忘记 (1-2分)**：重置间隔，明天再复习

复习入口：导航栏 → `复习`，显示待复习数量。进入后以问答形式逐题复习，复习后选择掌握程度即可。

### 3. 学习统计面板

访问 `/stats` 查看完整学习数据：

- **总览卡片**：总练习数、正确率、连续学习天数、总得分
- **学习热力图**：过去 84 天每日练习量可视化（类似 GitHub 贡献图）
- **每日详情列表**：每日练习数/正确数/时长/得分/最高连击

### 4. 生词本

在练习的答案页点击 `收藏生词` 按钮，系统自动提取当前句子中的俄语单词加入生词本。

访问 `/vocabulary` 查看、搜索和管理所有生词。

### 5. 连词成句模式

新增第三种练习模式：点击打散的俄语单词，按正确顺序拼成句子。

- 系统自动打乱句子中的单词顺序
- 点击单词将其加入答案区域
- 点击答案区域的单词可移除
- 答对/答错触发连击系统
- 支持重置和重新排列

在练习工具条下拉菜单中切换 `中译俄` / `俄语听写` / `连词成句` 三种模式。

### 6. 每日任务系统

首页右侧显示每日任务面板，自动生成三种任务：

| 任务类型 | 目标 |
|---------|------|
| 完成 10 道练习 | 完成 10 次答题 |
| 连击 5 次 | 达到 5 连击 |
| 学习 15 分钟 | 累计学习 15 分钟 |

- 显示任务进度条和完成状态
- 完成后可领取奖励
- 每天自动重置

### 7. 页内 AI 助手

练习页面右下角悬浮 AI 俄语语法助手按钮，点击展开对话窗口：

- 自动带入当前句子上下文（如果有）
- 可提问任何俄语语法问题（变格、变位、词义等）
- 通过 OpenRouter 调用 AI 回答
- 对话历史记录
- 每日提问次数限制（10次）
- 快捷提问按钮（动词变位、格用法等）

### 8. 正式用户体系

在 `正在登录` 状态下已替换为自建 JWT 认证：

- **注册** `/auth/register` — 用户名 + 密码，自动返回 JWT Token
- **登录** `/auth/login` — 验证凭据，返回 Token
- **当前用户** `/auth/me` — 获取用户信息
- 前端 `/login` 页面支持注册/登录切换
- 前端 `services/auth.ts` 管理 Token 存储和请求头注入
- 后端 `UserContext` 上下文管理 + `JwtAuthFilter` 自动提取用户 ID
- 所有控制器使用 `UserContext.getUserId()` 替代硬编码 `dev-user-001`
- 未携带 Token 的请求自动降级为 dev-user-001（向后兼容）

### 10. 连击音效

连击达到里程碑时播放特殊音效：
- 5 连击：普通音效
- 10 连击：1.2 倍速音效
- 15 连击：1.5 倍速音效
- 20 连击：2 倍速「传奇」音效

### 9. 学习小组

访问 `/groups` 创建和管理学习小组：

- 创建小组后自动生成 8 位邀请码
- 支持设置小组名称和介绍
- 发现其他公开小组

### 10. 音频/视频课程模式

课程包中的课程如果包含 `video` 字段，可切换到音视频学习模式：

- 视频课程：播放视频，结束后显示字幕
- 音频课程：纯听力训练
- 在练习工具条下拉菜单中切换模式

### 11. 新增 API

| 端点 | 方法 | 说明 |
|------|------|------|
| `/exercise-records` | POST | 保存练习记录 |
| `/stats/daily` | GET | 获取每日统计 |
| `/stats/total` | GET | 获取总统计 |
| `/reviews/due` | GET | 获取待复习列表 |
| `/reviews/due-count` | GET | 获取待复习数量 |
| `/reviews/record` | POST | 记录复习结果 |
| `/reviews/schedule` | POST | 安排复习 |
| `/vocabulary` | GET/POST/DELETE | 生词本 CRUD |
| `/tasks/today` | GET | 获取今日任务 |
| `/tasks/ensure` | POST | 创建今日任务 |
| `/tasks/progress` | POST | 更新任务进度 |
| `/tasks/claim` | POST | 领取任务奖励 |
| `/ai/ask` | POST | AI 语法问答 |
| `/auth/register` | POST | 用户注册 |
| `/auth/login` | POST | 用户登录 |
| `/auth/me` | GET | 当前用户信息 |
| `/groups` | GET/POST | 学习小组列表/创建 |
| `/groups/my` | GET | 我的小组 |
| `/groups/{id}` | GET | 小组详情 |

## 常用检查命令

```powershell
# 前端类型检查
pnpm -F client type-check

# 后端编译（不运行）
mvn -f backend\pom.xml -DskipTests compile

# 后端测试
mvn -f backend\pom.xml test

# 重启本地服务
.\run-local.ps1
```

## 已完成重点

### 基础架构
- 本地非 Docker 启动脚本
- Spring Boot 后端迁移
- MySQL + Flyway 初始化
- 俄语课程包基础数据
- 删除原英语课程包

### PDF 导入与 AI
- PDF 异步导入任务
- 本机目录 PDF 批量导入
- OpenRouter AI 精炼和 AI 课程生成
- 后台课程编辑和 PDF 导入入口
- 后台单词包 AI 补全：批量生成中文释义、词性/变格提示和例句

### 学习功能
- 单词课程包专项模式：中文释义/发音提示输入俄语单词
- 俄语词性/格/变位前端提示
- 标点符号不再要求手动输入
- 练习结果页学习卡片：展示词汇释义、词性、例句和 AI 精炼来源
- 单词包分层重排：按代词、名词、动词、形容词/副词、功能词、待补释义词自动拆课

### 游戏化与激励（新增）
- **连击系统**：连续答对累计连击，倍率最高 2.0x
- **SSS 评级**：课程完成后根据正确率和最大连击给予 C~SSS 评级
- **连击动画**：动态缩放动画 + 浮动得分数字
- **组合评分**：每次练习总得分和评级展示在结算面板

### 连词成句模式（新增）
- 第三种练习模式：点击单词拼成句子
- 自动打乱单词顺序，答对答错触发连击
- 工具条下拉菜单切换三种模式

### 每日任务（新增）
- 每日三种任务：完成练习 / 连击 / 学习时长
- 进度条显示，完成后可领取奖励

### 复习体系（新增）
- **间隔复习**：基于 SM-2 算法自动安排复习计划
- **复习页**：导航栏一键进入，答题 → 自评 → 自动计算下次复习时间
- **待复习提醒**：导航栏显示待复习数量小红点

### 学习统计（新增）
- **总览面板**：总练习数、正确率、连续天数、总得分
- **学习热力图**：84 天练习日历图
- **每日详情**：每日练习数、正确率、时长、得分、最高连击

### AI 俄语助手（新增）
- 练习页面右下角悬浮 AI 助手按钮
- 可提问语法、变格、变位等问题
- 自动带入当前句子上下文
- 通过 OpenRouter 提供回答

### 连击音效（新增）
- 5/10/15/20 连击里程碑触发加速音效
- 使用 `watch` 监听连击数自动播放

### 学习小组（新增）
- 创建小组，自动生成邀请码
- `/groups` 页面浏览和加入
- 导航栏和侧边栏快捷入口

### 音频/视频课程模式（新增）
- 课程含 `video` 字段时触发
- 支持视频播放和纯音频听力
- 工具条切换模式

### 生词本（新增）
- 练习答案页一键收藏生词
- 生词列表页搜索和管理
- 自动提取句子中的俄语单词

### 前台页面
- 前台隐藏公开上传入口
- 前台课程结构可视化：按词性展示课程数量和词条数量
- 前台词汇分类筛选：点击结构概览卡片即可聚焦某一类词汇课程
- 前台学习路径入口：默认推荐从代词课开始
- 首页学习控制台
- 课程包内容库
- 练习页产品化视觉
- 复习本页面

## 后续开发路线

### 1. 课程内容精炼

- 清理重复课程包（可通过 `/admin` 批量操作）
- 统一课程包命名和排序（后台 UI 就绪）
- 给课程增加难度、册数、单元、主题标签
- 批量 AI 精炼重点课程

### 2. 课程内容精炼

- 清理重复课程包
- 统一课程包命名和排序
- 将 PDF 生成课程按"字母、单词、短语、句子、场景"重组
- 批量 AI 精炼重点课程
- 给课程增加难度、册数、单元、主题标签

### 3. 后台运营增强

- 课程包批量上下架
- 课程包排序
- PDF 导入任务详情
- 导入失败重试
- 批量删除重复课程包
- 批量 AI 精炼
- TTS 缓存清理
- 用户自建课程审核

### 4. 学习体验增强

- 课程完成页
- 下一课推荐
- 错题解释（AI 分析错误原因）
- 朗读/听写专项模式增强
- PK 对战
- 笔记功能
- 开放用户自建课程（编辑端完善）

### 5. 俄语词法标注

当前词性提示是前端规则推断。后续建议新增数据库字段或新表，保存：

- 词性 / 性 / 数 / 格
- 动词体 / 时态 / 人称 / 变位说明
- 例句中的语法作用

## 注意事项

### 启动相关

- **首次启动较慢**：Maven 首次运行需要下载依赖（后端），`pnpm install` 需要安装依赖（前端）。后续启动会快很多。
- **端口冲突**：如果 8080 或 3000 被占用，`run-local.ps1` 会自动尝试关闭占用进程。如果自动关闭失败，手动关闭后重试。
- **MySQL 连接失败**：确认 MySQL 服务已启动（`Get-Service MySQL80`），且 `application.yml` 中的连接信息正确。
- **后端启动但前端 503**：Nuxt 在首次请求时可能还在编译，等几秒刷新即可。
- **日志排查**：启动失败时优先查看对应日志文件（`backend-spring.log` / `backend-spring.err.log` / `frontend-nuxt.log`）。
- **WMI 启动进程问题**：如果通过 `([wmiclass]"Win32_Process").Create()` 启动后端，AI 环境变量需要显式传入，不会自动继承用户变量。

### 浏览器相关

- 浏览器控制台里的 `message port closed` 多数来自 Chrome 扩展，不是项目代码。
- 浏览器可能拦截自动播放，用户点击触发的发音更稳定。

### PDF 导入

- 扫描版 PDF 依赖 Tesseract OCR，需安装俄语语言包。
- PDF 导入是异步任务，可在后台查看导入进度。
- 超大 PDF（>120MB）需要修改 `application.yml` 中的 `spring.servlet.multipart.max-file-size`。

### 安全与生产

- 当前项目仍是本地开发形态，生产上线前需要补充正式鉴权、安全配置、日志、备份和部署方案。
- 后端硬编码了 `dev-user-001` 作为默认用户 ID，没有真正的用户登录校验。
- JWT 密钥 `backend/src/main/resources/application.yml` 中的 `jwt.secret` 是占位符，上线前必须更换。
- TTS 使用 Google 免费接口，有请求频率限制，生产环境应替换为付费 TTS 服务。

### 仓库规范

- 不要将 `OPENROUTER_API_KEY` 等密钥提交到 Git 仓库。
- 运行时生成的目录（`runtime/`、`backend-spring.log` 等）已加入 `.gitignore`。
- 修改 `application.yml` 中的数据库密码后，确认不要误提交。
