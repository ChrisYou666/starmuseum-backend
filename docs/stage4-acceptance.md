# Stage 4（阶段4）后端 - Phase 4.0 基线与最短可测闭环（Acceptance）

> 目标：在进入阶段4开发前，把「现有阶段0~3后端」的可重复验收跑通，并确定 Flyway 迁移版本起点。
>
> 你只需要做到：**本地一键启动后端 + 跑完 Postman 集合全绿**，后续每完成一个 Phase 就把对应请求追加到集合里。

---

## 0. 你将得到哪些交付物

- `postman/stage4.postman_collection.json`
  - Stage4 Phase4.0 的最小回归集合：注册→登录→refresh→核心接口→星空接口→健康检查
- `docs/stage4-acceptance.md`（本文件）
- Flyway 阶段4迁移版本起点确认：**从 V12 开始**（原因见第 3 节）

---

## 1. 本地准备（数据库 + 配置）

### 1.1 准备 MySQL 数据库

建议创建数据库与账号（如果你已存在可跳过）：

```sql
CREATE DATABASE IF NOT EXISTS starmuseum DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'starmuseum'@'%' IDENTIFIED BY 'starmuseum123';
GRANT ALL PRIVILEGES ON starmuseum.* TO 'starmuseum'@'%';
FLUSH PRIVILEGES;
```

### 1.2 导入你提供的初始化 dump（2026-01-12）

文件：`2026-01-12_starmuseum_dump.sql`

导入方式（任选其一）：

**方式 A：命令行**
```bash
mysql -ustarmuseum -pstarmuseum123 -h127.0.0.1 -P3306 starmuseum < 2026-01-12_starmuseum_dump.sql
```

**方式 B：Navicat / Workbench**
- 选择数据库 `starmuseum`
- 执行 SQL 文件导入

> 注意：这个 dump 里不包含 `flyway_schema_history` 表（Flyway 会在第一次启动时自动创建）。

### 1.3 检查后端 dev 配置（application-dev.yml）

你当前 `application-dev.yml` 关键配置如下（你无需改动，只需要确认值匹配你本机环境）：
- `spring.datasource.url` 指向本机 MySQL `starmuseum`
- `spring.flyway.baseline-on-migrate=true`
- `spring.flyway.baseline-version=1`
- `spring.flyway.validate-on-migrate=true`
- `starmuseum.storage.local.upload-dir` 指向本地可写目录

---

## 2. 启动后端（确保是 dev Profile）

### 2.1 IDEA 启动方式（推荐）
- Run/Debug 配置里设置：
  - `Active profiles: dev`

### 2.2 命令行启动方式
```bash
# Windows PowerShell 示例
mvnw -Dspring-boot.run.profiles=dev spring-boot:run
```

启动后你应该能看到：
- Flyway 创建 `flyway_schema_history`
- 写入 baseline 记录（version=1）
- 执行 V2~V11（阶段3脚本）迁移（如果数据库里还没有这些表/字段）

---

## 3. Flyway 迁移策略确认（阶段4从 V12 开始）

### 3.1 查看已安装的 Flyway 版本
在 MySQL 执行：

```sql
SELECT installed_rank, version, description, type, script, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

你当前工程 `db/migration` 下已有：
- `V2__...` ~ `V11__...`（阶段3.1~3.5.4）

因此，阶段4新增迁移脚本版本号应从：
- ✅ `V12__phase4_*.sql` 开始

### 3.2 如果你发现版本号不止到 V11
以数据库中的最大 version 为准：
- 假设你看到最大是 V13，那么阶段4要从 V14 开始
- 原则：**永远用 “当前最大版本 + 1”**

---

## 4. 导入并运行 Postman 集合（全绿验收）

### 4.1 导入集合
在 Postman：
- Import → 选择文件 `postman/stage4.postman_collection.json`

### 4.2 设置集合变量（通常不需要改）
集合内变量默认如下：
- `baseUrl = http://localhost:8080`
- `password = Passw0rd!123`
- `timeUtc = 2026-01-10T12:00:00Z`
- `lat = 31.2304`
- `lon = 121.4737`

运行时会自动：
- 生成随机邮箱 `stage4_<timestamp>@test.com`
- 注册用户
- 登录并保存 `accessToken/refreshToken`
- 创建帖子并保存 `postId`
- 依次跑 feed / 详情 / 点赞 / 评论 / 天体搜索 / 天体详情 / sky summary / actuator health / admin-check

### 4.3 运行方式（必须按顺序）
- Collection → Run（Collection Runner）
- 直接 Run 即可（集合内请求已按顺序排列）

### 4.4 你期望看到的结果（最短验收）
- 所有请求 **绿色通过**
- 关键变量自动写入（你可以在集合 Variables 里看到）：
  - `accessToken`
  - `refreshToken`
  - `postId`
  - `astroBodyId`

---

## 5. 常见问题排查（Phase 4.0 只关注“能跑通”）

### 5.1 Register 失败：email 已存在
正常不会发生（集合会生成随机 email）。
如果你手动改成固定 email，重复跑会冲突。

### 5.2 401 unauthorized
- 看 Postman 里 `accessToken` 是否写入成功
- 确认 `/api/**` 默认都需要登录（SecurityConfig 的设计如此）
- 确认你没有跳过 Login 就跑后续接口

### 5.3 /api/dev/admin-check 404
- 这个接口只在 `dev` profile 下启用（@Profile("dev")）
- 确认你确实用 `dev` 启动

### 5.4 Astro Body Detail 返回 null
- 集合会优先 `search?q=sirius`，用第一个结果的 id 作为 `astroBodyId`
- 如果你的 catalog 数据里没有 sirius，可把 `search` 的 q 改为你数据库里确定存在的对象名

---

## 6. Phase 4.0 完成判定（DoD）

满足以下 3 条即可进入 Phase 4.1：

1. 本地后端（dev）可启动，Flyway 迁移正常（无报错）
2. Postman 集合全绿
3. 已确认阶段4迁移脚本起点版本号（通常是 V12）

---

## 7. 下一步（进入 Phase 4.1）

当你确认 Phase 4.0 通过后，你只需要告诉我：
- **Flyway 当前最大 version 是多少（截图或复制查询结果）**
- Postman 全绿（OK）

然后我们进入 Phase 4.1：Catalog 版本治理（导入/校验/激活/回滚）。
