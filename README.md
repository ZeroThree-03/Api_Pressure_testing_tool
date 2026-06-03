# API压测工具

一款基于Electron + Vue 3 + Spring Boot的API压测工具，支持curl解析、场景管理、实时监控和报告导出。

## 功能特性

- ✅ Curl命令解析，自动生成压测脚本
- ✅ 自定义压测参数（时长、线程数、循环次数）
- ✅ 场景管理（接口组合、参数化场景）
- ✅ 全局参数管理
- ✅ 实时监控与结果展示
- ✅ 白天/黑夜主题切换
- ✅ 生成可执行.exe文件

## 技术栈

- **后端**: Spring Boot 3.2 + MyBatis-Plus + SQLite + Apache HttpClient
- **前端**: Vue 3 + Element Plus + ECharts + Pinia
- **桌面**: Electron 28

## 环境要求

- Java 17+
- Node.js 18+
- Maven 3.9+（可选，使用Maven Wrapper）

## 快速开始

### 开发模式

```bash
# 方式1: 使用启动脚本（Windows）
双击 start-dev.bat

# 方式2: 手动启动
# 终端1: 启动后端
cd backend
mvn spring-boot:run

# 终端2: 启动前端
cd frontend
npm run dev
```

### 访问地址

- 前端: http://localhost:3001
- 后端: http://localhost:8080

### 打包成exe

```bash
# 方式1: 使用打包脚本（Windows）
双击 build.bat

# 方式2: 手动打包
npm install
cd frontend && npm run build && cd ..
npx electron-builder --win
```

## 项目结构

```
Api_Pressure_testing_tool/
├── backend/          # Spring Boot后端
│   ├── src/main/java/com/pressurtest/
│   │   ├── controller/     # API控制器
│   │   ├── service/        # 业务服务
│   │   ├── model/          # 数据模型
│   │   ├── mapper/         # MyBatis映射
│   │   ├── engine/         # 压测引擎
│   │   ├── websocket/      # WebSocket
│   │   └── config/         # 配置类
│   └── src/main/resources/
│       ├── application.yml
│       └── schema.sql
├── frontend/         # Vue 3前端
│   └── src/
│       ├── views/          # 页面组件
│       ├── components/     # 通用组件
│       ├── api/            # API调用
│       ├── stores/         # Pinia状态
│       └── styles/         # 主题样式
├── electron/         # Electron主进程
│   ├── main.js
│   └── preload.js
├── package.json
├── electron-builder.yml
├── start-dev.bat     # 开发启动脚本
└── build.bat         # 打包脚本
```

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/curl/parse` | POST | 解析curl命令 |
| `/api/templates` | GET/POST | 获取/创建模板 |
| `/api/templates/{id}` | GET/PUT/DELETE | 模板CRUD |
| `/api/scenarios` | GET/POST | 获取/创建场景 |
| `/api/scenarios/{id}` | GET/PUT/DELETE | 场景CRUD |
| `/api/tasks/start` | POST | 启动压测任务 |
| `/api/tasks/{id}/stop` | POST | 停止压测任务 |
| `/ws/monitor/{taskId}` | WebSocket | 实时监控 |

## 开发说明

### 后端开发

```bash
cd backend
mvn clean compile    # 编译
mvn spring-boot:run  # 运行
mvn test             # 测试
```

### 前端开发

```bash
cd frontend
npm install          # 安装依赖
npm run dev          # 开发模式
npm run build        # 构建
```

## 许可证

MIT License
