# API压测工具设计文档

**创建日期：** 2026-06-03
**版本：** v1.0
**状态：** 设计完成

---

## 1. 项目概述

### 1.1 项目目标
开发一款API压测工具，支持通过粘贴curl命令自动生成压测脚本，提供可视化界面进行压测配置、执行和结果分析。

### 1.2 核心功能
1. Curl命令解析，自动生成压测脚本
2. 自定义压测参数（时长、线程数、循环次数）
3. 场景管理（接口组合、参数化场景）
4. 全局参数管理
5. 实时监控与结果展示
6. 白天/黑夜主题切换
7. 生成可执行.exe文件

---

## 2. 系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                   Electron 桌面应用                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │              Vue 3 + Element Plus 前端              │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐          │  │
│  │  │ curl解析  │ │ 场景管理  │ │ 任务监控  │ ...      │  │
│  │  └──────────┘ └──────────┘ └──────────┘          │  │
│  └───────────────────────────────────────────────────┘  │
│                           │ IPC通信                      │
│                           ▼                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │           Spring Boot 后端 (内嵌进程)               │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐          │  │
│  │  │ API层    │ │ 压测引擎  │ │ 数据访问  │          │  │
│  │  └──────────┘ └──────────┘ └──────────┘          │  │
│  └───────────────────────────────────────────────────┘  │
│                           │                              │
│                           ▼                              │
│  ┌───────────────────────────────────────────────────┐  │
│  │                  SQLite 数据库                      │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 项目结构

```
Api_Pressure_testing_tool/
├── electron/                    # Electron主进程
│   ├── main.js                 # 主进程入口
│   ├── preload.js              # 预加载脚本
│   └── package.json            # Electron依赖
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── components/         # 组件
│   │   │   ├── CurlParser/     # curl解析组件
│   │   │   ├── Scenario/       # 场景管理组件
│   │   │   ├── Monitor/        # 监控组件
│   │   │   └── Settings/       # 设置组件
│   │   ├── views/              # 页面
│   │   │   ├── Home.vue        # 首页
│   │   │   ├── CurlParser.vue  # curl解析页
│   │   │   ├── Scenario.vue    # 场景管理页
│   │   │   ├── Monitor.vue     # 监控页
│   │   │   └── Report.vue      # 报告页
│   │   ├── stores/             # Pinia状态
│   │   ├── router/             # 路由配置
│   │   ├── utils/              # 工具函数
│   │   ├── styles/             # 样式文件
│   │   │   ├── light.scss      # 白天主题
│   │   │   └── dark.scss       # 黑夜主题
│   │   └── App.vue             # 根组件
│   ├── package.json            # 前端依赖
│   └── vite.config.js          # Vite配置
│
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/pressurtest/
│   │       ├── controller/     # 控制器
│   │       ├── service/        # 服务层
│   │       │   ├── CurlParserService.java
│   │       │   ├── ScenarioService.java
│   │       │   ├── TestEngineService.java
│   │       │   └── ReportService.java
│   │       ├── model/          # 数据模型
│   │       ├── mapper/         # MyBatis映射
│   │       ├── config/         # 配置类
│   │       └── util/           # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml     # 应用配置
│   │   └── mapper/             # SQL映射文件
│   └── pom.xml                 # Maven依赖
│
├── package.json                # 根package.json(打包配置)
├── electron-builder.yml        # Electron打包配置
└── README.md                   # 项目说明
```

---

## 3. 技术栈

### 3.1 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Vue 3** | ^3.4 | 前端框架 |
| **Element Plus** | ^2.5 | UI组件库 |
| **Vite** | ^5.0 | 构建工具 |
| **Pinia** | ^2.1 | 状态管理 |
| **Vue Router** | ^4.2 | 路由管理 |
| **ECharts** | ^5.4 | 图表库 |
| **Axios** | ^1.6 | HTTP客户端 |
| **Electron** | ^28.0 | 桌面应用框架 |

### 3.2 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Java** | 17+ | 编程语言 |
| **Spring Boot** | ^3.2 | 后端框架 |
| **Apache HttpClient** | ^5.3 | HTTP客户端 |
| **SQLite** | ^3.4 | 数据库 |
| **MyBatis-Plus** | ^3.5 | ORM框架 |
| **Lombok** | ^1.18 | 代码简化 |
| **Jackson** | ^2.15 | JSON处理 |
| **WebSocket** | - | 实时通信 |

---

## 4. 功能模块设计

### 4.1 Curl解析模块

**功能目标：** 粘贴curl命令，自动解析生成可配置的压测请求

**支持的curl特性：**
1. **基础解析**
   - URL（含查询参数）
   - HTTP方法（GET/POST/PUT/DELETE等）
   - 请求头（-H）
   - 请求体（-d / --data）
   - 认证（-u / --user）

2. **高级特性**
   - 多行curl（反斜杠换行）
   - 变量替换（${variable}）
   - 文件上传（-F / --form）
   - Cookie（-b / --cookie）

**解析流程：**
```
粘贴curl → 语法解析 → 提取组件 → 参数识别 → 生成请求模板
    │           │           │           │           │
    ▼           ▼           ▼           ▼           ▼
  字符串    Token化    URL/头部/   可变参数    可配置的
   输入     解析       参数/体     标记       请求对象
```

### 4.2 压测配置模块

**配置项：**

| 配置项 | 说明 | 默认值 | 选项 |
|--------|------|--------|------|
| **线程数** | 并发线程数量 | 10 | 1-1000 |
| **压测时长** | 测试持续时间 | 60秒 | 秒/分钟/小时 |
| **循环次数** | 每线程循环次数 | 1次 | 1-999999 或 **永远** |
| **启动延迟** | 线程启动间隔 | 0ms | 0-10000ms |
| **请求超时** | 单个请求超时 | 30秒 | 1-300秒 |

**循环次数设计：**
- 固定次数：输入具体数字（如 100）
- 永远：持续执行直到手动停止
- 时长控制：按时间自动停止（与压测时长联动）

**逻辑关系：**
- 如果设置"永远"，则忽略"压测时长"，需手动停止
- 如果设置固定次数，到达次数后自动停止
- "压测时长"和"循环次数"取先达到的条件

**高级配置：**

| 配置项 | 说明 |
|--------|------|
| **RPS限制** | 限制每秒请求数（防止打垮服务器） |
| **响应断言** | 自动判断请求成功/失败的条件 |
| **思考时间** | 请求之间的随机延迟（模拟真实用户） |
| **SSL验证** | 是否验证HTTPS证书 |
| **代理设置** | HTTP代理配置 |

### 4.3 场景管理模块

**场景类型：**

#### 4.3.1 接口组合场景

多个接口按顺序/并行执行，模拟真实业务流程。

**示例：送礼场景**
```
场景名称：送礼流程
├── 步骤1：查询礼物列表 (GET /api/gifts)
├── 步骤2：发送礼物 (POST /api/gift/send) × 3次
└── 步骤3：确认送达 (GET /api/gift/status)
```

**配置项：**
| 配置项 | 说明 |
|--------|------|
| 执行顺序 | 顺序/并行/混合 |
| 失败策略 | 继续/停止/重试 |
| 循环控制 | 单步骤循环次数 |

#### 4.3.2 参数化场景

同一接口，不同参数组合，测试各种边界情况。

**示例：送礼参数化**
```
场景名称：送礼参数化测试
├── 接口：POST /api/gift/send
├── 参数组合1：单人送单人 (sender=A, receiver=B)
├── 参数组合2：单人送多人 (sender=A, receiver=B,C,D)
├── 参数组合3：多人送单人 (sender=A,B,C, receiver=D)
└── 参数组合4：互相赠送 (sender=A, receiver=B + sender=B, receiver=A)
```

**参数来源：**
- 手动输入（界面直接配置）
- CSV文件导入（批量参数）
- 全局参数引用（${global.xxx}）

### 4.4 全局参数模块

**参数类型：**

| 类型 | 说明 | 示例 |
|------|------|------|
| **静态参数** | 固定值，手动设置 | `api_version=1.0` |
| **动态参数** | 运行时生成 | `timestamp=${__timestamp}` |
| **随机参数** | 随机生成 | `random_id=${__random(1000,9999)}` |
| **序列参数** | 顺序递增 | `user_id=${__sequence(1,100)}` |

**内置函数：**
```
${__timestamp}          # 当前时间戳（秒）
${__timestamp_ms}       # 当前时间戳（毫秒）
${__random(min,max)}    # 随机整数
${__random_string(len)} # 随机字符串
${__uuid}               # UUID
${__sequence(start,end)} # 顺序递增
${__date(format)}       # 格式化日期
```

**参数作用域：**
```
全局参数
├── 项目级参数（所有场景共享）
└── 场景级参数（仅当前场景可用）
    └── 覆盖同名的项目级参数
```

### 4.5 实时监控模块

**核心指标：**

| 指标 | 说明 | 显示方式 |
|------|------|----------|
| **QPS** | 每秒请求数 | 实时曲线图 |
| **响应时间** | 平均/P50/P90/P99 | 实时曲线图 + 数值 |
| **错误率** | 失败请求占比 | 百分比 + 曲线图 |
| **活跃线程** | 当前执行线程数 | 数值 |
| **总请求数** | 累计发送请求数 | 数值 |
| **吞吐量** | 数据传输量 | 数值 |

**结果树功能：**
- 实时展示每个请求的详细信息
- 状态筛选（成功/失败/执行中）
- 详情展开（完整请求/响应）
- 错误高亮（失败请求红色标记）

### 4.6 压测报告模块

**报告内容：**
- 基本信息（场景、时间、配置）
- 性能指标（QPS、响应时间、错误率）
- 响应时间分布图
- 错误分析

**导出格式：**
- CSV - 原始数据，便于进一步分析
- JSON - 结构化数据，便于程序处理
- HTML - 可视化报告，便于分享

### 4.7 主题切换模块

**主题类型：**
- **白天模式 (Light)** - 浅色背景，深色文字
- **黑夜模式 (Dark)** - 深色背景，浅色文字

**切换方式：**
- 界面右上角主题切换按钮
- 支持系统主题自动跟随
- 记住用户选择（本地存储）

---

## 5. 数据库设计

### 5.1 表结构

**表1：请求模板 (request_templates)**
```sql
CREATE TABLE request_templates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    method TEXT NOT NULL,
    url TEXT NOT NULL,
    headers TEXT,
    body TEXT,
    auth_type TEXT,
    auth_config TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**表2：压测场景 (test_scenarios)**
```sql
CREATE TABLE test_scenarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    description TEXT,
    config TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**表3：场景步骤 (scenario_steps)**
```sql
CREATE TABLE scenario_steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    scenario_id INTEGER NOT NULL,
    template_id INTEGER,
    step_order INTEGER NOT NULL,
    name TEXT,
    config TEXT,
    FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id),
    FOREIGN KEY (template_id) REFERENCES request_templates(id)
);
```

**表4：全局参数 (global_params)**
```sql
CREATE TABLE global_params (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    value TEXT,
    scope TEXT DEFAULT 'project',
    scenario_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id)
);
```

**表5：压测任务 (test_tasks)**
```sql
CREATE TABLE test_tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    scenario_id INTEGER NOT NULL,
    status TEXT NOT NULL,
    config TEXT,
    started_at DATETIME,
    completed_at DATETIME,
    result_summary TEXT,
    FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id)
);
```

### 5.2 ER图

```
request_templates ◄──── scenario_steps ────► test_scenarios
                                               ▲
                                               │
                                          global_params
                                               │
                                          test_tasks
```

---

## 6. API接口设计

### 6.1 REST API

**1. Curl解析接口**
```
POST /api/curl/parse
请求: { "curl": "curl -X POST http://api.test.com/gift/send..." }
响应: { 
  "code": 0, 
  "data": {
    "method": "POST",
    "url": "http://api.test.com/gift/send",
    "headers": {...},
    "body": {...},
    "params": [...]
  }
}
```

**2. 请求模板管理**
```
GET    /api/templates              # 获取模板列表
POST   /api/templates              # 创建模板
GET    /api/templates/{id}         # 获取模板详情
PUT    /api/templates/{id}         # 更新模板
DELETE /api/templates/{id}         # 删除模板
```

**3. 场景管理**
```
GET    /api/scenarios              # 获取场景列表
POST   /api/scenarios              # 创建场景
GET    /api/scenarios/{id}         # 获取场景详情
PUT    /api/scenarios/{id}         # 更新场景
DELETE /api/scenarios/{id}         # 删除场景
```

**4. 压测任务管理**
```
POST   /api/tasks/start            # 启动压测任务
POST   /api/tasks/{id}/stop        # 停止压测任务
GET    /api/tasks/{id}/status      # 获取任务状态
GET    /api/tasks/{id}/result      # 获取任务结果
GET    /api/tasks/{id}/result-tree # 获取结果树数据
```

**5. 全局参数管理**
```
GET    /api/params                 # 获取全局参数列表
POST   /api/params                 # 创建参数
PUT    /api/params/{id}            # 更新参数
DELETE /api/params/{id}            # 删除参数
POST   /api/params/import          # 导入参数(CSV)
GET    /api/params/export          # 导出参数(CSV)
```

**6. 报告导出**
```
GET    /api/reports/{taskId}/csv   # 导出CSV报告
GET    /api/reports/{taskId}/json  # 导出JSON报告
GET    /api/reports/{taskId}/html  # 导出HTML报告
```

### 6.2 WebSocket接口

**实时监控数据推送**
```
WS /ws/monitor/{taskId}

服务端推送消息格式:
{
  "type": "monitor",
  "data": {
    "qps": 156,
    "avgResponseTime": 23,
    "errorRate": 0.5,
    "activeThreads": 50,
    "totalRequests": 12345,
    "timestamp": 1705312201000
  }
}

结果树数据推送:
{
  "type": "result",
  "data": {
    "id": 1234,
    "status": "success",
    "method": "POST",
    "url": "/api/gift/send",
    "statusCode": 200,
    "responseTime": 23,
    "request": {...},
    "response": {...},
    "timestamp": 1705312201000
  }
}
```

### 6.3 响应格式规范

```json
// 成功响应
{
  "code": 0,
  "message": "success",
  "data": {...}
}

// 错误响应
{
  "code": 400,
  "message": "参数错误",
  "data": null,
  "errors": [...]
}
```

---

## 7. 压测引擎设计

### 7.1 引擎架构

```
┌─────────────────────────────────────────────────────────┐
│                    TestEngineService                     │
├─────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ TaskScheduler │  │ ThreadManager│  │ ResultCollector│ │
│  │  (任务调度)   │  │  (线程管理)   │  │  (结果收集)   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│         │                │                  │           │
│         ▼                ▼                  ▼           │
│  ┌────────────────────────────────────────────────────┐ │
│  │              WorkerThread (工作线程)                │ │
│  │  ┌──────────────┐  ┌──────────────┐               │ │
│  │  │ HttpClient   │  │ ParamResolver│               │ │
│  │  │ (HTTP客户端)  │  │ (参数解析)   │               │ │
│  │  └──────────────┘  └──────────────┘               │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 7.2 核心组件

**TaskScheduler (任务调度器)**
- 管理压测任务生命周期
- 支持同步/异步执行模式
- 任务队列管理
- 定时任务支持

**ThreadManager (线程管理器)**
- 线程池创建和管理
- 线程启动延迟控制
- 线程状态监控
- 优雅停止线程

**ResultCollector (结果收集器)**
- 收集每个请求的结果
- 实时计算统计指标
- WebSocket推送数据
- 结果持久化存储

**WorkerThread (工作线程)**
- 执行HTTP请求
- 参数替换和解析
- 请求重试机制
- 错误处理

### 7.3 关键配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| **连接池大小** | HTTP连接池最大连接数 | 200 |
| **连接超时** | 建立连接超时时间 | 5秒 |
| **读取超时** | 读取响应超时时间 | 30秒 |
| **重试次数** | 请求失败重试次数 | 3 |
| **重试间隔** | 重试之间的等待时间 | 1秒 |

### 7.4 性能优化

1. **连接池复用** - 复用HTTP连接，减少握手开销
2. **异步非阻塞** - 使用NIO提升并发性能
3. **批量处理** - 批量写入数据库，减少IO
4. **内存管理** - 控制结果队列大小，防止OOM

---

## 8. 开发计划

### 8.1 开发阶段划分

**阶段一：基础框架搭建（1-2周）**
- [ ] Electron + Vue 3 项目初始化
- [ ] Spring Boot 后端项目初始化
- [ ] SQLite数据库配置
- [ ] 基础UI框架搭建
- [ ] 主题切换功能

**阶段二：核心功能开发（2-3周）**
- [ ] Curl解析模块开发
- [ ] 请求模板管理
- [ ] 全局参数管理
- [ ] 场景管理功能
- [ ] 压测引擎核心

**阶段三：监控与报告（1-2周）**
- [ ] 实时监控仪表盘
- [ ] WebSocket通信
- [ ] 结果树功能
- [ ] 压测报告生成
- [ ] 报告导出功能

**阶段四：优化与完善（1周）**
- [ ] 性能优化
- [ ] 错误处理完善
- [ ] 用户体验优化
- [ ] 文档编写
- [ ] 打包发布

### 8.2 里程碑

| 里程碑 | 目标 | 预计时间 |
|--------|------|----------|
| **M1** | 基础框架可运行 | 第2周末 |
| **M2** | 核心功能可用 | 第5周末 |
| **M3** | 监控报告完成 | 第7周末 |
| **M4** | 正式版本发布 | 第8周末 |

### 8.3 风险识别

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| Electron打包复杂 | 中 | 提前研究打包配置 |
| WebSocket性能问题 | 中 | 使用STOMP协议 |
| 大数据量内存溢出 | 高 | 流式处理，限制队列大小 |
| 跨平台兼容性 | 低 | 使用跨平台技术栈 |

---

## 9. 测试策略

### 9.1 测试类型

1. **单元测试** - 核心业务逻辑测试
2. **集成测试** - API接口测试
3. **E2E测试** - Electron应用测试
4. **性能测试** - 压测工具自身性能验证

### 9.2 测试覆盖率目标

- 单元测试覆盖率：80%+
- 集成测试覆盖率：70%+
- E2E测试覆盖核心流程

---

## 10. 部署与发布

### 10.1 打包方式

使用Electron打包成.exe安装包：
- 前端：Vue 3 打包成静态资源
- 后端：Spring Boot 打包成可执行JAR
- Electron：打包成.exe安装包，内含前端和后端

### 10.2 发布流程

1. 代码提交到Git仓库
2. 运行测试套件
3. 执行Electron打包
4. 生成.exe安装包
5. 发布到内部服务器或网盘

---

## 附录

### A. 术语表

| 术语 | 说明 |
|------|------|
| **QPS** | Queries Per Second，每秒查询数 |
| **P50/P90/P99** | 响应时间百分位数 |
| **压测** | 压力测试，测试系统在高负载下的表现 |
| **场景** | 一组接口请求的组合，模拟真实业务流程 |

### B. 参考资料

- [Jmeter官方文档](https://jmeter.apache.org/)
- [Electron官方文档](https://www.electronjs.org/)
- [Vue 3官方文档](https://vuejs.org/)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
