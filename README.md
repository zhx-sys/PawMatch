# PawMatch — 宠物领养寄养全栈平台

PawMatch 是一个面向宠物领养与寄养场景的全栈平台，采用 **Vue 3 + Spring Boot + Android** 技术栈，构建从信息浏览、智能匹配、寄养服务到领养闭环的完整生态。

## 功能模块

| 模块 | 说明 |
|---|---|
| 首页 | 平台入口，聚合宠物推荐、动态更新与快捷入口 |
| 宠物信息 | 宠物详情展示、筛选搜索、收藏管理 |
| 寄养 | 寄养服务发布、预约、管理，支持在线沟通 |
| 社区 | 用户动态、互动评论、宠物日常分享 |
| 百科 | 宠物品种百科，支持**众包编辑**与审核机制 |
| 救助站 | 领养申请、进度追踪、**领养回访闭环** |
| 智能匹配 | 基于问卷的领养意愿智能匹配算法 |
| 宠物互动游戏 | 内置轻量互动小游戏，增强用户粘性 |
| 用户体系 | 登录/注册、信用分与成长激励、角色鉴权（普通用户→登录用户→救助站） |

## 技术栈

| 层级 | 技术 | 说明 |
|---|---|---|
| 前端 | Vue 3 + Vite + Element Plus | SPA，28 条路由，Hash 模式 + 懒加载 |
| 前端路由 | Vue Router 4 | 三级 beforeEach 鉴权（公开→登录→救助站） |
| 后端 | Spring Boot | RESTful API，端口 8080 |
| 持久层 | MyBatis-Plus | ORM，24 张表 |
| 数据库 | MySQL | 关系型数据存储 |
| 认证 | JWT | 无状态 Token 鉴权 |
| 实时通信 | WebSocket | 即时消息推送 |
| Android | Kotlin | 原生移动端 |

## 快速启动

### 前置要求

- Node.js >= 18
- JDK >= 17
- Maven >= 3.8
- MySQL >= 8.0
- Android Studio (可选)

### 1. 后端

```bash
cd pawmatch-server

# 修改 src/main/resources/application.yml 中的数据库连接配置
# spring.datasource.url / username / password 按本地环境填写

# 执行数据库初始化脚本（位于 sql/ 目录）
# mysql -u root -p < sql/init.sql

mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 2. 前端

```bash
cd pawmatch-web

npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，代理请求至后端 8080。

### 3. Android

用 Android Studio 打开 `pawmatch-android/` 目录，同步 Gradle 后直接运行。

## 项目结构

```
PawMatch/
├── pawmatch-web/          # Vue 3 前端
│   ├── src/
│   │   ├── router/        # 路由配置（三级鉴权）
│   │   ├── views/         # 页面组件
│   │   ├── components/    # 复用组件
│   │   ├── api/           # 接口封装
│   │   └── stores/        # Pinia 状态管理
│   └── package.json
├── pawmatch-server/       # Spring Boot 后端
│   ├── src/main/java/
│   │   ├── controller/    # 控制器
│   │   ├── service/       # 业务层
│   │   ├── mapper/        # MyBatis-Plus 映射
│   │   └── config/        # 配置（JWT、WebSocket 等）
│   ├── sql/               # 数据库初始化脚本
│   └── pom.xml
└── pawmatch-android/      # Android 客户端
```

## 系统亮点

- **领养回访闭环**：救助站发布 → 用户申请 → 审核 → 领养后定期回访，形成完整领养追踪链路
- **信用+成长激励体系**：用户行为（领养、寄养、百科贡献）积累信用分与成长等级，规范社区行为
- **宠物百科众包**：用户协作编辑品种百科，经审核后发布，保证内容质量的同时持续扩充
- **领养寄养一体**：同一平台覆盖领养与寄养双场景，打破传统平台功能割裂
- **智能匹配问卷**：基于用户填写偏好（品种、体型、生活方式等）智能推荐匹配宠物
- **互动游戏**：内置宠物主题轻量游戏，提升用户活跃与留存

## License

MIT
