# 俄语学习平台 · 完整迁移手册（本地 → 固定 IP 服务器）

> **使用前提**：你已经决定要按 `DEPLOYMENT.md` 的「**路径 B：完整迁移**」操作。这份文档是路径 B 的**专用 runbook**：从当前本地机器把数据 + 代码 + 配置全部搬到一台**有固定 IP 的目标机器**，并以**生产模式**（jar + nuxt build）24/7 运行。
>
> 看到任何编号别跳，命令在哪台机器执行**都标了**。

---

## 目录

- [§ A. 源机器（你现在这台）：备份 + 上传](#a-源机器你现在这台备份--上传)
- [§ B. 目标机器（固定 IP）：装系统软件](#b-目标机器固定-ip装系统软件)
- [§ C. 目标机器：拉代码 + 还原数据库](#c-目标机器拉代码--还原数据库)
- [§ D. 目标机器：环境变量与配置](#d-目标机器环境变量与配置)
- [§ E. 目标机器：构建](#e-目标机器构建)
- [§ F. 目标机器：启动 + 防火墙](#f-目标机器启动--防火墙)
- [§ G. 目标机器：开机自启动（Windows / Linux）](#g-目标机器开机自启动windows--linux)
- [§ H. 验收清单](#h-验收清单)
- [§ I. 排错速查](#i-排错速查)

---

## A. 源机器（你现在这台）：备份 + 上传

### A.1 备份数据库

```powershell
# 在仓库根目录
.\backup-db.ps1
```

产出 `earthworm-dump-YYYYMMDD-HHMMSS.sql`（约 5–30 MB）。

### A.2 列出**绝对要带过去**的东西

| 类别 | 路径 | 必带？ | 备注 |
|---|---|---|---|
| 源码（含 git 历史） | 整个 `D:\earthworm-main\` | ✅ | 推荐用 git push + 目标机器 git clone |
| **数据库 dump** | A.1 产出的 `earthworm-dump-*.sql` | ✅ | 带账号 / 学习记录的关键 |
| `.env` 私密值 | OPENROUTER_API_KEY、JWT_SECRET、DB 密码 | ✅ | 用记事本写下来或 `secrets.txt` 临时拷贝（用完删） |
| `application.yml` 改动 | `backend/src/main/resources/application.yml` | 可选 | 如果你改过 root 密码、AI baseUrl 等 |
| ngrok token | `%LOCALAPPDATA%\ngrok\ngrok.yml` | 可选 | 目标机器有固定 IP 时不需要 |
| `node_modules`、`.m2` 缓存 | 不带 | ❌ | 目标机器 `pnpm install` / `mvn` 会重下，有时反而冲突 |
| 浏览器 localStorage 留言 | `gusi-feedback-messages-v1` | 不带 | 客户端浏览器数据，不在服务器上 |

### A.3 把代码 + dump 推到目标机器

任选一种：

**方式 1（推荐）：通过 git 中转**

```powershell
# 在源机器
git add -A
git commit -m "snapshot before migrate"
git push origin main

# dump 文件不要 commit！通过其它方式发：
# scp earthworm-dump-*.sql user@<目标 IP>:~/
# 或拷到 U 盘 / 上传到云盘
```

**方式 2：直接打包整个文件夹**

```powershell
# 排除大体积无用目录后打包
Compress-Archive -Path D:\earthworm-main `
    -DestinationPath D:\earthworm-migrate.zip `
    -CompressionLevel Optimal `
    -Force
# 然后 Compress-Archive 命令不支持排除模式，建议先临时删 node_modules / target / .output
```

更稳的做法：

```powershell
robocopy D:\earthworm-main D:\earthworm-migrate /MIR `
    /XD node_modules .output target .pnpm-store .nuxt
# 然后压缩 D:\earthworm-migrate
```

记住把 `earthworm-dump-*.sql` 也放进去。

---

## B. 目标机器（固定 IP）：装系统软件

> 假设是 Windows Server 或 Windows 10/11，ip 设为 `<SERVER_IP>`（下文都用这个占位符）。
> Linux 服务器在每节末尾都给了对应命令。

### B.1 一键脚本（推荐）

如果用 git clone 来的项目，仓库根目录已经有：

```powershell
.\setup.ps1
```

会自动用 winget 装 Git / JDK 21 / Maven / Node 20+ / pnpm / MySQL 8。

### B.2 手工版（Linux 等价）

```bash
# Ubuntu 22.04
sudo apt update
sudo apt install -y openjdk-21-jdk maven nodejs npm mysql-server git
sudo npm install -g pnpm
```

### B.3 自检

```powershell
java -version    # 21.x
mvn --version    # 3.9+
node --version   # 20+
pnpm --version   # 9.x
mysql --version  # 8.x
```

任何一个不对就别往下走。

---

## C. 目标机器：拉代码 + 还原数据库

### C.1 拉代码

```powershell
# 选个工作目录，比如 C:\app
mkdir C:\app; cd C:\app
git clone <你的仓库 URL> earthworm
cd earthworm
pnpm install         # 第一次约 3-5 分钟
```

把 `earthworm-dump-*.sql` 也放到 `C:\app\earthworm\`。

### C.2 创建空数据库

```powershell
mysql -u root -p
```

进入 mysql shell：

```sql
CREATE DATABASE earthworm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### C.3 还原 dump

```powershell
mysql -u root -p earthworm < earthworm-dump-YYYYMMDD-HHMMSS.sql
```

立刻验证：

```powershell
mysql -u root -p earthworm -e "SELECT COUNT(*) FROM course_packs; SELECT COUNT(*) FROM courses; SELECT COUNT(*) FROM statements; SELECT COUNT(*) FROM users;"
```

应该看到：

| 表 | 行数 |
|---|---|
| course_packs | 25 |
| courses | 1 841 |
| statements | 19 103 |
| users | 你源机器上的用户数 |

数字不对就**先别启动后端**，把 dump 重导一遍。

---

## D. 目标机器：环境变量与配置

### D.1 修改数据库连接（如有不同）

在目标机器上通过环境变量设置数据库连接信息，不要把 `username/password` 写入仓库配置：

```powershell
[Environment]::SetEnvironmentVariable("SPRING_DATASOURCE_USERNAME", "root", "Machine")
[Environment]::SetEnvironmentVariable("SPRING_DATASOURCE_PASSWORD", "你的密码", "Machine")
```

> `Machine` 范围适合服务器，对所有用户和服务有效。需要管理员 PowerShell。

### D.2 必填环境变量

```powershell
[Environment]::SetEnvironmentVariable("OPENROUTER_API_KEY", "your_openrouter_key_here", "Machine")
[Environment]::SetEnvironmentVariable("OPENROUTER_MODEL",   "openai/gpt-4o-mini", "Machine")

# JWT 密钥要换成新随机串（不要复用源机器的，除非你想保持已签的 token 还有效）
$jwt = -join ((48..57)+(65..90)+(97..122) | Get-Random -Count 64 | ForEach-Object {[char]$_})
[Environment]::SetEnvironmentVariable("JWT_SECRET", $jwt, "Machine")
```

### D.3 让 Nuxt Nitro 监听到外网

`apps/client/nuxt.config.ts` 已经默认 `apiBase=/api/backend` + routeRules 反代到 `:8080`，**不用改**。

但 Nitro 默认只监听 127.0.0.1，固定 IP 服务器需要监听 0.0.0.0。我们用环境变量：

```powershell
[Environment]::SetEnvironmentVariable("NITRO_HOST", "0.0.0.0", "Machine")
[Environment]::SetEnvironmentVariable("NITRO_PORT", "3000", "Machine")
```

> 后端 Spring Boot 不需要改：它默认 `0.0.0.0:8080`，但因为 Nuxt 反代会从本机 `localhost:8080` 调用它，**强烈建议把 8080 端口在防火墙上关闭对外**（见 §F.4），让后端只在内网可见。

### D.4 设完之后

```powershell
# 关掉这个 PowerShell，重新开一个新的 PowerShell（让 Machine 范围的变量生效）
# 然后验证：
$env:OPENROUTER_API_KEY
$env:NITRO_HOST           # 应输出 0.0.0.0
```

---

## E. 目标机器：构建

### E.1 后端：打 fat jar

```powershell
mvn -f backend/pom.xml -DskipTests clean package
```

成功后产出：

```
backend/target/backend-0.0.1-SNAPSHOT.jar
```

（具体文件名由 pom.xml 决定，可能略有不同。记住路径。）

### E.2 前端：构建 Nitro 服务

```powershell
pnpm --filter client build
```

成功后产出：

```
apps/client/.output/
├── public/         静态资源
└── server/
    └── index.mjs   Node 入口
```

### E.3 验证 build 产物

```powershell
Test-Path backend\target\*.jar
Test-Path apps\client\.output\server\index.mjs
```

两个都 `True` 才能进 §F。

---

## F. 目标机器：启动 + 防火墙

### F.1 启动后端

```powershell
# 在仓库根目录
java -jar (Resolve-Path backend\target\*.jar | Select-Object -First 1).Path
```

第一次启动会看到 Flyway 迁移信息（应该是 0 条，因为 dump 已带 `flyway_schema_history` 表），然后 `Tomcat started on port(s): 8080`。

完整迁移使用已有业务数据库时请保持 `SEED_WRITE_ON_STARTUP=false`（默认值），避免普通启动对课程内容产生写入。

测试：

```powershell
(Invoke-RestMethod http://localhost:8080/admin/stats).totals | ConvertTo-Json
```

应输出 `{packs:25, courses:1841, statements:19103}`。

### F.2 启动前端 Nitro

新开一个 PowerShell：

```powershell
cd C:\app\earthworm
node apps/client/.output/server/index.mjs
```

输出类似：

```
Listening on http://[::]:3000
```

`[::]` 表示监听所有地址（IPv6 也包括 IPv4）。

### F.3 浏览器访问

在另一台机器上打开浏览器：

```
http://<SERVER_IP>:3000
```

应该看到俄语学习平台主页。点登录用源机器上的账号密码登录，看到学习历史 = 迁移成功。

### F.4 配置 Windows 防火墙

只放行 3000 端口（让外网能访问前端），把 8080 锁死在本机：

```powershell
# 在管理员 PowerShell
New-NetFirewallRule -DisplayName "Earthworm Frontend 3000" `
    -Direction Inbound -LocalPort 3000 -Protocol TCP `
    -Action Allow -Profile Any

# 显式拒绝外网访问 8080（可选，因为默认 Windows 也不会放行）
New-NetFirewallRule -DisplayName "Earthworm Backend 8080 (deny external)" `
    -Direction Inbound -LocalPort 8080 -Protocol TCP `
    -Action Block -Profile Public,Domain
```

Linux 服务器：

```bash
sudo ufw allow 3000/tcp
sudo ufw deny 8080/tcp
sudo ufw enable
```

> 若公网访问还要走域名 / HTTPS，建议在前面挂一层 Nginx 或 Caddy 反代到 :3000，并配 Let's Encrypt 证书。本文档不展开。

---

## G. 目标机器：开机自启动（Windows / Linux）

### G.1 Windows：用 NSSM 把 jar 和 node 注册成服务

```powershell
winget install NSSM.NSSM      # 或从 https://nssm.cc 下载
nssm install EarthwormBackend  java
# 弹出窗口里填：
#   Path:        C:\Program Files\Eclipse Adoptium\jdk-21\bin\java.exe
#   Startup dir: C:\app\earthworm
#   Arguments:   -jar backend\target\backend-0.0.1-SNAPSHOT.jar

nssm install EarthwormFrontend node
#   Path:        C:\Program Files\nodejs\node.exe
#   Startup dir: C:\app\earthworm
#   Arguments:   apps\client\.output\server\index.mjs
#   Environment: NITRO_HOST=0.0.0.0  NITRO_PORT=3000

# 启动
nssm start EarthwormBackend
nssm start EarthwormFrontend
```

服务自启自停，开机后自动运行。

### G.2 Linux：systemd unit

```bash
# /etc/systemd/system/earthworm-backend.service
[Unit]
Description=Earthworm Backend (Spring Boot)
After=mysql.service network.target
[Service]
WorkingDirectory=/opt/earthworm
EnvironmentFile=/etc/earthworm.env
ExecStart=/usr/bin/java -jar backend/target/backend-0.0.1-SNAPSHOT.jar
Restart=always
[Install]
WantedBy=multi-user.target
```

```bash
# /etc/systemd/system/earthworm-frontend.service
[Unit]
Description=Earthworm Frontend (Nuxt Nitro)
After=earthworm-backend.service
[Service]
WorkingDirectory=/opt/earthworm
EnvironmentFile=/etc/earthworm.env
Environment=NITRO_HOST=0.0.0.0 NITRO_PORT=3000
ExecStart=/usr/bin/node apps/client/.output/server/index.mjs
Restart=always
[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now earthworm-backend earthworm-frontend
sudo systemctl status earthworm-backend earthworm-frontend
```

---

## H. 验收清单

把目标机器调到这套状态后逐项打勾：

- [ ] `mysql -e "SELECT COUNT(*) FROM course_packs"` 返回 **25**
- [ ] `mysql -e "SELECT COUNT(*) FROM courses"` 返回 **1841**
- [ ] `mysql -e "SELECT COUNT(*) FROM statements"` 返回 **19103**
- [ ] `mysql -e "SELECT COUNT(*) FROM users"` 等于源机器的 users 行数
- [ ] 后端启动日志含 `Started ... Application in N seconds`，无 `ERROR` 关键字
- [ ] 本机 `curl http://localhost:8080/admin/stats` 返回正确 JSON
- [ ] 本机 `curl http://localhost:3000/api/backend/admin/stats` 返回**同样**的 JSON（验证 Nitro 反代）
- [ ] 同局域网 / 公网另一台机器打开 `http://<SERVER_IP>:3000` 看到俄语学习平台主页
- [ ] 用源机器上的账号密码登录成功，能看到原来的学习记录 / 错题本 / 生词本
- [ ] 8080 端口对外**不可访问**（外网 telnet `<SERVER_IP> 8080` 拒绝）
- [ ] 重启服务器后 `EarthwormBackend` / `EarthwormFrontend` 自动起来（NSSM/systemd 配过）

全部勾上 = 迁移完成 ✅

---

## I. 排错速查

### Q: `mysql < dump.sql` 报 `ERROR 1064 ... near 'DEFINER'`

dump 里有 stored procedure 用了源机器的用户名作为 DEFINER。两种解决：

```powershell
# 方法 A: 重导时跳过 routine
mysqldump --no-create-info --no-routines --no-triggers earthworm > clean-dump.sql
# 在源机器重做这一步，再传过去

# 方法 B: 把 dump 里的 DEFINER=`xxx`@`localhost` 全部 sed 替换成空
(Get-Content earthworm-dump-*.sql) -replace 'DEFINER=`[^`]+`@`[^`]+`', '' | Set-Content earthworm-dump-clean.sql
mysql -u root -p earthworm < earthworm-dump-clean.sql
```

### Q: 后端启动报 `Validate failed: ... Detected applied migration not resolved locally`

dump 带过来的 `flyway_schema_history` 行数比目标机器代码 `db/migration/V*.sql` 多。两种情况：

- **目标代码比源旧**：拉新代码（`git pull`）后再启动
- **迁移历史不一致**：先停止应用启动，保留 dump，拉齐源端和目标端的 migration 文件；在备份副本或测试库验证修复方案后，再恢复正式服务。不要绕过 Flyway 校验直接启动。

### Q: 浏览器访问 `http://<SERVER_IP>:3000` 超时

按顺序排查：

1. 防火墙：`Get-NetFirewallRule -DisplayName "*3000*"` 看规则有没有
2. Nitro 监听：在服务器上 `netstat -an | Select-String "3000"`，应有 `0.0.0.0:3000 LISTENING`，不能是 `127.0.0.1:3000`
3. Nitro 启动时是否有读到 `NITRO_HOST=0.0.0.0`：检查环境变量
4. 路由器/云厂商安全组：阿里云 / 腾讯云 / AWS 等需要在 web 控制台单独放行 3000

### Q: 前端能打开但 API 全部 404

Nuxt 反代失败。检查：

```powershell
# 在服务器本机
curl http://localhost:3000/api/backend/admin/stats
```

200 = 反代正常；404/502 = 后端没起或后端端口不对。先 `curl http://localhost:8080/admin/stats` 看后端。

### Q: 启动 Nitro 报「Module not found ofetch」之类

`pnpm install` 装的依赖被 `nuxt build` 用 esbuild 打进去了，但是有时候本地缓存出问题。重做：

```powershell
Remove-Item -Recurse apps/client/.output, apps/client/.nuxt
pnpm --filter client build
```

### Q: 想本地修一下再热更新

生产模式 jar + nuxt build 不支持热更新。短期改动可以直接退回 dev 模式：

```powershell
mvn -f backend/pom.xml spring-boot:run
pnpm --filter client dev --host 0.0.0.0 --port 3000
```

dev 模式照样能监听 0.0.0.0 让外网访问，只是启动慢、内存占用高。改完后重新 build 替换 jar 和 .output。

---

## 附录：一行总结

源机器跑 `.\backup-db.ps1` → dump 拷过去 → 目标机器装环境 + `git clone` + `mysql < dump.sql` + 设环境变量 + `mvn package` + `pnpm build` + `java -jar` + `node .output/server/index.mjs` + 防火墙 + （可选）NSSM 自启。

完。
