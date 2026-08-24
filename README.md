# 海洋预报发布值班监控系统

国家海洋预报中心网站及相关业务系统的日常值班监控平台，实现网站状态监测、数据更新时间监测、异常诊断和值班日志管理。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3.x、MyBatis-Plus、SQLite、Quartz、Jsoup |
| 前端 | Vue 3、Vite、Element Plus、Pinia、ECharts、Axios |
| 部署 | Docker Compose、Nginx |

## 项目结构

```
ocean-duty-system/
├── ocean-duty-server/     # 后端 Spring Boot 项目
├── ocean-duty-web/        # 前端 Vue3 项目
├── docker-compose.yml     # Docker 编排
├── todo.md                # 需求文档
├── Java开发规范.md
└── Vue 项目规范.md
```

## 快速启动

### 后端

```bash
cd ocean-duty-server
mvn spring-boot:run
```

- API 地址：http://localhost:8080/api
- Swagger 文档：http://localhost:8080/api/swagger-ui.html
- 默认账号：`admin` / `admin123`

### 前端

```bash
cd ocean-duty-web
npm install
npm run dev
```

- 访问地址：http://localhost:3000

### Docker 部署

```bash
docker compose up -d --build
```

- 前端：http://localhost
- 后端 API：http://localhost:8080/api

## 功能模块

- **监控首页**：网站状态卡片、异常告警、模块更新时间监控
- **值班日志**：日志查询、新增、修改、导出（待完善）
- **定时任务**：每 5 分钟网站检测、每 10 分钟模块检测
- **登录认证**：管理员 / 值班人员角色（JWT 待完善）

## 后续开发

参见 [todo.md](./todo.md) 中的完整需求清单。
