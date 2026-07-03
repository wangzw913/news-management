# 📰 新闻发布管理系统 v3

基于 **Spring Boot 3 + Vue 3** 的全栈新闻发布管理系统。v2 的 PHP 版本位于 `../news_v2/`。

## 技术栈

| 层面 | 技术 |
|------|------|
| 后端 | Java 17 · Spring Boot 3.2 · MyBatis-Plus 3.5 · Spring Security · JWT |
| 数据库 | MySQL 8.0 · Redis 7 · Elasticsearch 8 |
| 前端 | Vue 3 · Element Plus · Pinia · Vite · Vue Router · ECharts |
| 存储 | MinIO（可选，开发环境用本地存储） |
| 部署 | Docker Compose 一键启动 6 个服务 |

## 快速开始

### 本地开发

**前置条件**: JDK 17+, Node.js 20+, MySQL 8.0+, Redis 7+

```bash
# 1. 创建数据库
mysql -u root -e "CREATE DATABASE IF NOT EXISTS news_v3 DEFAULT CHARSET utf8mb4"

# 2. 启动后端
cd news-server
./mvnw spring-boot:run

# 3. 启动前端
cd news-web
npm install
npm run dev

# 4. 访问 http://localhost:5173
```

### Docker 一键部署

```bash
docker compose up -d
# 访问 http://localhost
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 审核员 | auditor | auditor123 |
| 编辑 | editor | editor123 |
| 普通用户 | user | user123 |

## 功能

- 四角色 RBAC 权限（管理员/审核员/编辑/普通用户）
- 稿件审核工作流（草稿→待审→通过/退回）
- 用户门户首页（轮播/分类/卡片流/热门排行）
- ES 全文搜索（ik 中文分词）
- Redis 缓存（热点新闻、点击计数缓冲、限流）
- 内容推荐（TF-IDF + 余弦相似度）
- 用户投稿、收藏、点赞、评论回复
- 个人中心（收藏/点赞/评论/历史/投稿/改密）
- 仪表盘、用户管理、分类管理、轮播管理、操作日志
- 实时 WebSocket 通知（审核结果推送）
- Knife4j API 文档（http://localhost:8080/doc.html）

## 项目结构

```
news_v3/
├── docker-compose.yml
├── news-server/          # Spring Boot 后端
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/example/news/
│       ├── common/       # 配置、安全、AOP、异常处理
│       └── modules/      # 业务模块（user/news/category/review/...）
├── news-web/             # Vue 3 前端
│   ├── package.json
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── api/          # Axios 封装
│       ├── router/       # 路由 + 守卫
│       ├── stores/       # Pinia 状态管理
│       ├── layouts/      # 布局组件
│       └── views/        # 页面组件
```

## API 文档

启动后端后访问: http://localhost:8080/doc.html

## v2 → v3 升级要点

1. PHP → Spring Boot 3 企业级后端
2. 原生 HTML/CSS/JS → Vue 3 SPA
3. MD5 → BCrypt 密码加密
4. Session → JWT 无状态认证
5. MySQL LIKE → Elasticsearch 全文搜索
6. 直接写库 → Redis 缓存 + 点击缓冲
7. 新增内容推荐、WebSocket 通知、限流
8. Docker Compose 一键部署全栈

---

## 🏗 架构设计（保研面试参考）

### 系统架构图

```
┌─────────────────────────────────────────────────────┐
│                    Nginx (port 80)                   │
│         / → Vue SPA    /api → proxy :8080           │
└──────────────────┬──────────────────────────────────┘
                   │
    ┌──────────────┼──────────────┐
    ▼              ▼              ▼
┌───────┐   ┌──────────┐   ┌──────────┐
│  Vue 3 │   │  Spring  │   │  MinIO   │
│  SPA   │   │ Boot 3.2 │   │ :9000    │
│ :80    │   │ :8080    │   │ (图片)   │
└───────┘   └────┬─────┘   └──────────┘
                 │
    ┌────────────┼────────────┐
    ▼            ▼            ▼
┌───────┐  ┌────────┐  ┌──────────┐
│ MySQL │  │ Redis  │  │  Elastic │
│ :3306 │  │ :6379  │  │ :9200    │
└───────┘  └────────┘  └──────────┘
```

### 技术选型理由

| 选择 | 对比方案 | 理由 |
|------|---------|------|
| **Spring Boot 3.2** | PHP/Go/Python | Java 生态最成熟，保研导师最熟悉，MyBatis-Plus 消灭模板代码 |
| **MyBatis-Plus** | JPA/Hibernate | 国内主流，复杂 SQL 可控，分页/自动填充开箱即用 |
| **JWT 双 token** | Session | 无状态，便于水平扩展；30min access + 7天 refresh 兼顾安全与体验 |
| **BCrypt** | MD5/SHA256 | 自适应哈希，抗彩虹表，Spring Security 默认推荐 |
| **Redis** | 直接写 MySQL | 缓存热点数据，写后缓冲(Write-Behind)降低行锁竞争 |
| **Elasticsearch** | MySQL LIKE | 倒排索引，ik 中文分词，高亮搜索，降级方案回退 MySQL LIKE |
| **TF-IDF 推荐** | 协同过滤 | 冷启动友好，纯 Java 零依赖，用户数据积累后可扩展 CF |
| **WebSocket** | 轮询 | 审核结果实时推送作者，连接池管理，离线通知落库 |
| **Docker Compose** | 手动安装 | 6 个服务一键编排，开发/生产环境一致 |

### 系统设计亮点

**1. 安全认证**
- JWT access token(30min) + refresh token(7天 Redis) 双 token 机制
- refresh token 一次一换(Rotation)，防重放攻击
- 登出时 access token 加入 Redis 黑名单，防止已签发 token 被滥用
- 密码 BCrypt 加密，注册强制普通用户角色，高权限需管理员分配

**2. 点击计数优化**
- Redis 可用时：内存 INCR → 每 5 分钟批量 UPDATE（Write-Behind 模式）
- Redis 不可用时：ConcurrentHashMap 内存缓冲 → @Scheduled 定时刷库
- 避免每次阅读直接 UPDATE 导致行锁竞争

**3. 内容推荐算法**
- 中文分词：Unigram + Bigram 简单分词
- TF-IDF 向量化 → 余弦相似度排序
- LRU 缓存相似度结果，避免重复计算
- 冷启动降级：按同分类高点击推荐

**4. 审核工作流**
- 状态机：draft → pending → published/rejected
- 退回携带原因，作者可修改后重新提交
- 审核通过/退回通过 WebSocket 实时推送通知作者
- 通知同时落库，离线用户下次登录可见

**5. 缓存策略**
- 缓存穿透：空值缓存 + 布隆过滤器(可选)
- 缓存击穿：分布式锁(SETNX) + 热点预热
- 缓存雪崩：随机 TTL + 多级缓存

### 测试

```bash
cd news-server && mvn test
```

包含：
- `UserServiceTest`: 登录/注册业务逻辑测试 (6 cases)
- `NewsServiceTest`: 审核流程 + 点击缓冲测试 (5 cases)

### 面试可能被问到的问题

1. **JWT vs Session？为什么用双 token？** → 无状态方便扩展，双 token 平衡安全与体验
2. **缓存怎么防止击穿/穿透/雪崩？** → 见上方"缓存策略"
3. **推荐系统怎么做的？冷启动怎么办？** → TF-IDF + 余弦相似度，降级到同分类推荐
4. **为什么 MyBatis-Plus 而不是 JPA？** → SQL 可控，复杂查询写 XML，国内主流
5. **点击计数为什么不直接 UPDATE？** → 行锁竞争，Write-Behind 模式降低 DB 压力
6. **微服务为什么拆成 6 个容器？** → 职责分离，独立扩缩容，故障隔离
