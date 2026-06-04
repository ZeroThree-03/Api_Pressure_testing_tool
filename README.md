# API压测工具

一款基于Electron + Vue 3 + Spring Boot的API压测工具，支持curl解析、多步骤场景、实时监控和响应断言。

## 功能特性

- Curl命令解析，自动生成压测脚本，查询参数自动拆分可独立编辑
- 多步骤场景：支持按顺序执行多个接口，模拟完整业务流程
- 响应断言：自动检查响应体中的业务码（`code`字段），`code=200`为成功，其他为失败
- 实时监控：QPS趋势图、响应时间趋势图、结果树（含ID、状态、响应时间、详情）
- 任务管理：任务列表支持查看、停止、删除操作
- 环境变量管理：支持将参数存为环境变量，切换不同环境
- 白天/黑夜主题切换
- 支持打包为桌面exe

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
mvnw.cmd spring-boot:run

# 终端2: 启动前端
cd frontend
npm run dev
```

### 访问地址

- 前端: http://localhost:5173
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

## 核心模块说明

### 压测引擎

- 支持多线程并发请求（1-1000线程）
- 支持多步骤顺序执行：场景中的多个接口按顺序依次调用
- 支持设置循环次数、持续时间、思考时间、重试次数
- 每个请求结果自动生成递增ID，包含状态、状态码、响应时间、请求/响应体

### 响应断言

- HTTP状态码判断：200-299为通过
- 业务码断言：解析响应体JSON，检查`code`字段
  - `code = 200` → 成功
  - `code ≠ 200` → 失败，记录错误信息（如`业务码: 500 - 服务器异常`）
  - 非JSON响应体不做业务码断言

### 实时监控

- Dashboard：QPS、平均响应时间、错误率、活跃线程
- 趋势图：QPS和响应时间的实时折线图（ECharts，支持自适应尺寸）
- 结果树：展示所有请求结果，支持按成功/失败筛选，点击详情查看完整请求/响应

### Curl解析

- 解析curl命令提取URL、方法、请求头、请求体
- URL查询参数自动拆分为可编辑的键值对
- 支持将参数存为环境变量，方便参数化管理
- 保存为模板时，查询参数独立存储，便于后续修改

## 项目结构

```
Api_Pressure_testing_tool/
├── backend/                    # Spring Boot后端
│   └── src/main/java/com/pressurtest/
│       ├── controller/         # API控制器（TaskController, TemplateController等）
│       ├── service/            # 业务服务（TestEngineService, ScenarioService等）
│       ├── model/              # 数据模型（TestResult, TestTask, RequestTemplate等）
│       ├── mapper/             # MyBatis映射
│       ├── engine/             # 压测引擎（TestSession, WorkerThread, ResultCollector）
│       ├── websocket/          # WebSocket实时推送（MonitorWebSocket）
│       └── config/             # 配置类（WebSocketConfig, JacksonConfig等）
├── frontend/                   # Vue 3前端
│   └── src/
│       ├── views/              # 页面（Monitor, TaskList, ScenarioEdit等）
│       ├── components/         # 组件（Monitor/QpsChart, ResultTree等）
│       ├── api/                # API调用封装
│       ├── stores/             # Pinia状态管理
│       └── utils/              # 工具（websocket, request）
├── electron/                   # Electron主进程
├── start-dev.bat               # 开发启动脚本
├── start-backend.bat           # 后端启动脚本
└── build.bat                   # 打包脚本
```

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/curl/parse` | POST | 解析curl命令 |
| `/api/templates` | GET/POST | 获取/创建模板 |
| `/api/templates/{id}` | GET/PUT/DELETE | 模板CRUD |
| `/api/scenarios` | GET/POST | 获取/创建场景 |
| `/api/scenarios/{id}` | GET/PUT/DELETE | 场景CRUD |
| `/api/tasks/start` | POST | 启动压测任务（支持scenarioId或直接传url等参数） |
| `/api/tasks/{id}/stop` | POST | 停止压测任务 |
| `/api/tasks/{id}` | DELETE | 删除任务 |
| `/api/tasks/{id}/status` | GET | 查询任务状态 |
| `/api/tasks` | GET | 获取所有任务 |
| `/ws/monitor/{taskId}` | WebSocket | 实时监控（推送monitor/result/status消息） |

## 许可证

MIT License
