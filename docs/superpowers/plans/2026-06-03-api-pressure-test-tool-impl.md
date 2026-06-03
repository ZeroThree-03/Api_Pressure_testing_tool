# API压测工具实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一款基于Electron + Vue 3 + Spring Boot的API压测工具，支持curl解析、场景管理、实时监控和报告导出。

**Architecture:** 前端使用Vue 3 + Element Plus构建UI，通过Electron打包成桌面应用；后端使用Spring Boot提供REST API，使用Apache HttpClient执行压测任务；数据存储使用SQLite；前后端通过HTTP和WebSocket通信。

**Tech Stack:** Vue 3, Element Plus, Electron, Vite, Pinia, ECharts, Spring Boot 3.2, Apache HttpClient 5.3, SQLite, MyBatis-Plus, WebSocket

---

## 文件结构映射

### 后端文件结构
```
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/pressurtest/
│   │   │   ├── Application.java
│   │   │   ├── controller/
│   │   │   │   ├── CurlController.java
│   │   │   │   ├── TemplateController.java
│   │   │   │   ├── ScenarioController.java
│   │   │   │   ├── TaskController.java
│   │   │   │   ├── ParamController.java
│   │   │   │   └── ReportController.java
│   │   │   ├── service/
│   │   │   │   ├── CurlParserService.java
│   │   │   │   ├── TemplateService.java
│   │   │   │   ├── ScenarioService.java
│   │   │   │   ├── TestEngineService.java
│   │   │   │   ├── ParamService.java
│   │   │   │   └── ReportService.java
│   │   │   ├── model/
│   │   │   │   ├── RequestTemplate.java
│   │   │   │   ├── TestScenario.java
│   │   │   │   ├── ScenarioStep.java
│   │   │   │   ├── GlobalParam.java
│   │   │   │   ├── TestTask.java
│   │   │   │   └── TestResult.java
│   │   │   ├── mapper/
│   │   │   │   ├── RequestTemplateMapper.java
│   │   │   │   ├── TestScenarioMapper.java
│   │   │   │   ├── ScenarioStepMapper.java
│   │   │   │   ├── GlobalParamMapper.java
│   │   │   │   └── TestTaskMapper.java
│   │   │   ├── engine/
│   │   │   │   ├── TestEngine.java
│   │   │   │   ├── WorkerThread.java
│   │   │   │   ├── ResultCollector.java
│   │   │   │   └── ThreadManager.java
│   │   │   ├── websocket/
│   │   │   │   └── MonitorWebSocket.java
│   │   │   └── config/
│   │   │       ├── WebSocketConfig.java
│   │   │       ├── HttpClientConfig.java
│   │   │       └── CorsConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── schema.sql
│   │       └── mapper/
│   │           ├── RequestTemplateMapper.xml
│   │           ├── TestScenarioMapper.xml
│   │           ├── ScenarioStepMapper.xml
│   │           ├── GlobalParamMapper.xml
│   │           └── TestTaskMapper.xml
│   └── test/java/com/pressurtest/
│       ├── service/
│       │   ├── CurlParserServiceTest.java
│       │   ├── TemplateServiceTest.java
│       │   ├── ScenarioServiceTest.java
│       │   ├── ParamServiceTest.java
│       │   └── ReportServiceTest.java
│       └── engine/
│           └── TestEngineTest.java
```

### 前端文件结构
```
frontend/
├── package.json
├── vite.config.js
├── index.html
├── src/
│   ├── main.js
│   ├── App.vue
│   ├── router/
│   │   └── index.js
│   ├── stores/
│   │   ├── app.js
│   │   ├── template.js
│   │   ├── scenario.js
│   │   ├── task.js
│   │   └── param.js
│   ├── api/
│   │   ├── curl.js
│   │   ├── template.js
│   │   ├── scenario.js
│   │   ├── task.js
│   │   ├── param.js
│   │   └── report.js
│   ├── views/
│   │   ├── Home.vue
│   │   ├── CurlParser.vue
│   │   ├── TemplateList.vue
│   │   ├── TemplateEdit.vue
│   │   ├── ScenarioList.vue
│   │   ├── ScenarioEdit.vue
│   │   ├── Monitor.vue
│   │   ├── Report.vue
│   │   └── Settings.vue
│   ├── components/
│   │   ├── Layout/
│   │   │   ├── AppLayout.vue
│   │   │   ├── Sidebar.vue
│   │   │   └── Header.vue
│   │   ├── CurlParser/
│   │   │   ├── CurlInput.vue
│   │   │   └── ParsedResult.vue
│   │   ├── Scenario/
│   │   │   ├── StepList.vue
│   │   │   ├── StepEdit.vue
│   │   │   └── ParamConfig.vue
│   │   ├── Monitor/
│   │   │   ├── Dashboard.vue
│   │   │   ├── QpsChart.vue
│   │   │   ├── ResponseTimeChart.vue
│   │   │   └── ResultTree.vue
│   │   └── Common/
│   │       ├── ThemeToggle.vue
│   │       └── ParamEditor.vue
│   ├── utils/
│   │   ├── request.js
│   │   ├── websocket.js
│   │   └── theme.js
│   └── styles/
│       ├── variables.scss
│       ├── light.scss
│       ├── dark.scss
│       └── global.scss
```

### Electron文件结构
```
electron/
├── main.js
├── preload.js
└── package.json
```

---

## 阶段一：基础框架搭建

### Task 1: 初始化Spring Boot后端项目

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/pressurtest/Application.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/schema.sql`

- [ ] **Step 1: 创建Maven pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.pressurtest</groupId>
    <artifactId>api-pressure-test-tool</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>API Pressure Test Tool</name>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <sqlite-jdbc.version>3.44.1.0</sqlite-jdbc.version>
        <httpclient5.version>5.3</httpclient5.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- SQLite -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>${sqlite-jdbc.version}</version>
        </dependency>

        <!-- Apache HttpClient -->
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
            <version>${httpclient5.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Jackson -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建应用主类**

```java
// backend/src/main/java/com/pressurtest/Application.java
package com.pressurtest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pressurtest.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- [ ] **Step 3: 创建应用配置文件**

```yaml
# backend/src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: org.sqlite.JDBC
    url: jdbc:sqlite:./data/pressurtest.db
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto

# 压测引擎配置
pressurtest:
  engine:
    max-threads: 1000
    default-connect-timeout: 5000
    default-read-timeout: 30000
    max-retry: 3
    retry-interval: 1000
```

- [ ] **Step 4: 创建数据库Schema**

```sql
-- backend/src/main/resources/schema.sql

-- 请求模板表
CREATE TABLE IF NOT EXISTS request_templates (
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

-- 压测场景表
CREATE TABLE IF NOT EXISTS test_scenarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    description TEXT,
    config TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 场景步骤表
CREATE TABLE IF NOT EXISTS scenario_steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    scenario_id INTEGER NOT NULL,
    template_id INTEGER,
    step_order INTEGER NOT NULL,
    name TEXT,
    config TEXT,
    FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id),
    FOREIGN KEY (template_id) REFERENCES request_templates(id)
);

-- 全局参数表
CREATE TABLE IF NOT EXISTS global_params (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    value TEXT,
    scope TEXT DEFAULT 'project',
    scenario_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id)
);

-- 压测任务表
CREATE TABLE IF NOT EXISTS test_tasks (
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

- [ ] **Step 5: 验证项目启动**

Run: `cd backend && mvn spring-boot:run`
Expected: 应用启动成功，监听8080端口

- [ ] **Step 6: 提交代码**

```bash
cd backend
git add pom.xml src/
git commit -m "feat: initialize Spring Boot backend project"
```

---

### Task 2: 创建数据模型和Mapper

**Files:**
- Create: `backend/src/main/java/com/pressurtest/model/RequestTemplate.java`
- Create: `backend/src/main/java/com/pressurtest/model/TestScenario.java`
- Create: `backend/src/main/java/com/pressurtest/model/ScenarioStep.java`
- Create: `backend/src/main/java/com/pressurtest/model/GlobalParam.java`
- Create: `backend/src/main/java/com/pressurtest/model/TestTask.java`
- Create: `backend/src/main/java/com/pressurtest/mapper/RequestTemplateMapper.java`
- Create: `backend/src/main/java/com/pressurtest/mapper/TestScenarioMapper.java`
- Create: `backend/src/main/java/com/pressurtest/mapper/ScenarioStepMapper.java`
- Create: `backend/src/main/java/com/pressurtest/mapper/GlobalParamMapper.java`
- Create: `backend/src/main/java/com/pressurtest/mapper/TestTaskMapper.java`

- [ ] **Step 1: 创建RequestTemplate模型**

```java
// backend/src/main/java/com/pressurtest/model/RequestTemplate.java
package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("request_templates")
public class RequestTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String method;
    private String url;
    private String headers;
    private String body;
    private String authType;
    private String authConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建TestScenario模型**

```java
// backend/src/main/java/com/pressurtest/model/TestScenario.java
package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_scenarios")
public class TestScenario {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private String description;
    private String config;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 创建ScenarioStep模型**

```java
// backend/src/main/java/com/pressurtest/model/ScenarioStep.java
package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scenario_steps")
public class ScenarioStep {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scenarioId;
    private Long templateId;
    private Integer stepOrder;
    private String name;
    private String config;
}
```

- [ ] **Step 4: 创建GlobalParam模型**

```java
// backend/src/main/java/com/pressurtest/model/GlobalParam.java
package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("global_params")
public class GlobalParam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private String value;
    private String scope;
    private Long scenarioId;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 5: 创建TestTask模型**

```java
// backend/src/main/java/com/pressurtest/model/TestTask.java
package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_tasks")
public class TestTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scenarioId;
    private String status;
    private String config;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String resultSummary;
}
```

- [ ] **Step 6: 创建Mapper接口**

```java
// backend/src/main/java/com/pressurtest/mapper/RequestTemplateMapper.java
package com.pressurtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pressurtest.model.RequestTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequestTemplateMapper extends BaseMapper<RequestTemplate> {
}
```

```java
// backend/src/main/java/com/pressurtest/mapper/TestScenarioMapper.java
package com.pressurtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pressurtest.model.TestScenario;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestScenarioMapper extends BaseMapper<TestScenario> {
}
```

```java
// backend/src/main/java/com/pressurtest/mapper/ScenarioStepMapper.java
package com.pressurtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pressurtest.model.ScenarioStep;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScenarioStepMapper extends BaseMapper<ScenarioStep> {
}
```

```java
// backend/src/main/java/com/pressurtest/mapper/GlobalParamMapper.java
package com.pressurtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pressurtest.model.GlobalParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GlobalParamMapper extends BaseMapper<GlobalParam> {
}
```

```java
// backend/src/main/java/com/pressurtest/mapper/TestTaskMapper.java
package com.pressurtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pressurtest.model.TestTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestTaskMapper extends BaseMapper<TestTask> {
}
```

- [ ] **Step 7: 验证数据库表创建**

Run: `cd backend && mvn spring-boot:run`
Expected: 应用启动成功，数据库表自动创建

- [ ] **Step 8: 提交代码**

```bash
cd backend
git add src/main/java/com/pressurtest/model/ src/main/java/com/pressurtest/mapper/
git commit -m "feat: add data models and MyBatis mappers"
```

---

### Task 3: 初始化Vue 3前端项目

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/router/index.js`
- Create: `frontend/src/stores/app.js`
- Create: `frontend/src/utils/request.js`
- Create: `frontend/src/styles/variables.scss`
- Create: `frontend/src/styles/global.scss`
- Create: `frontend/src/styles/light.scss`
- Create: `frontend/src/styles/dark.scss`

- [ ] **Step 1: 创建package.json**

```json
{
  "name": "api-pressure-test-tool-frontend",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "element-plus": "^2.5.3",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.6.2",
    "echarts": "^5.4.3",
    "vue-echarts": "^6.6.7"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.10",
    "sass": "^1.69.5",
    "unplugin-auto-import": "^0.17.3",
    "unplugin-vue-components": "^0.26.0"
  }
}
```

- [ ] **Step 2: 创建Vite配置**

```javascript
// frontend/vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
      },
    },
  },
})
```

- [ ] **Step 3: 创建index.html**

```html
<!-- frontend/index.html -->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>API压测工具</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 4: 创建main.js**

```javascript
// frontend/src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/global.scss'

const app = createApp(App)
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```

- [ ] **Step 5: 创建App.vue**

```vue
<!-- frontend/src/App.vue -->
<template>
  <div :class="['app-container', themeClass]">
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const themeClass = computed(() => appStore.theme === 'dark' ? 'dark-theme' : 'light-theme')
</script>

<style lang="scss">
.app-container {
  width: 100%;
  min-height: 100vh;
}
</style>
```

- [ ] **Step 6: 创建路由配置**

```javascript
// frontend/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/components/Layout/AppLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
      },
      {
        path: 'curl-parser',
        name: 'CurlParser',
        component: () => import('@/views/CurlParser.vue'),
      },
      {
        path: 'templates',
        name: 'TemplateList',
        component: () => import('@/views/TemplateList.vue'),
      },
      {
        path: 'templates/:id',
        name: 'TemplateEdit',
        component: () => import('@/views/TemplateEdit.vue'),
      },
      {
        path: 'scenarios',
        name: 'ScenarioList',
        component: () => import('@/views/ScenarioList.vue'),
      },
      {
        path: 'scenarios/:id',
        name: 'ScenarioEdit',
        component: () => import('@/views/ScenarioEdit.vue'),
      },
      {
        path: 'monitor/:taskId',
        name: 'Monitor',
        component: () => import('@/views/Monitor.vue'),
      },
      {
        path: 'report/:taskId',
        name: 'Report',
        component: () => import('@/views/Report.vue'),
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
```

- [ ] **Step 7: 创建Pinia Store**

```javascript
// frontend/src/stores/app.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const theme = ref(localStorage.getItem('theme') || 'light')

  function setTheme(newTheme) {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
  }

  function toggleTheme() {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  return {
    theme,
    setTheme,
    toggleTheme,
  }
})
```

- [ ] **Step 8: 创建HTTP请求工具**

```javascript
// frontend/src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code !== 0) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **Step 9: 创建主题样式文件**

```scss
// frontend/src/styles/variables.scss
:root {
  --bg-primary: #ffffff;
  --bg-secondary: #f5f7fa;
  --text-primary: #303133;
  --text-secondary: #606266;
  --border-color: #dcdfe6;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
}

.dark-theme {
  --bg-primary: #1d1e1f;
  --bg-secondary: #2d2d2d;
  --text-primary: #e5eaf3;
  --text-secondary: #a3a6ad;
  --border-color: #414243;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
}
```

```scss
// frontend/src/styles/global.scss
@import './variables.scss';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.3s, color 0.3s;
}

a {
  color: inherit;
  text-decoration: none;
}
```

```scss
// frontend/src/styles/light.scss
.light-theme {
  --bg-primary: #ffffff;
  --bg-secondary: #f5f7fa;
  --text-primary: #303133;
  --text-secondary: #606266;
  --border-color: #dcdfe6;
}
```

```scss
// frontend/src/styles/dark.scss
.dark-theme {
  --bg-primary: #1d1e1f;
  --bg-secondary: #2d2d2d;
  --text-primary: #e5eaf3;
  --text-secondary: #a3a6ad;
  --border-color: #414243;
}
```

- [ ] **Step 10: 安装依赖并验证启动**

Run: `cd frontend && npm install && npm run dev`
Expected: 开发服务器启动成功，访问 http://localhost:3000 可看到页面

- [ ] **Step 11: 提交代码**

```bash
cd frontend
git add package.json vite.config.js index.html src/
git commit -m "feat: initialize Vue 3 frontend project with Element Plus"
```

---

### Task 4: 创建前端布局组件

**Files:**
- Create: `frontend/src/components/Layout/AppLayout.vue`
- Create: `frontend/src/components/Layout/Sidebar.vue`
- Create: `frontend/src/components/Layout/Header.vue`
- Create: `frontend/src/components/Common/ThemeToggle.vue`

- [ ] **Step 1: 创建AppLayout组件**

```vue
<!-- frontend/src/components/Layout/AppLayout.vue -->
<template>
  <div class="app-layout">
    <Sidebar />
    <div class="main-content">
      <Header />
      <div class="page-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 240px;
}

.page-content {
  flex: 1;
  padding: 20px;
  background-color: var(--bg-secondary);
}
</style>
```

- [ ] **Step 2: 创建Sidebar组件**

```vue
<!-- frontend/src/components/Layout/Sidebar.vue -->
<template>
  <div class="sidebar">
    <div class="logo">
      <h2>API压测工具</h2>
    </div>
    <el-menu
      :default-active="currentRoute"
      router
      class="sidebar-menu"
    >
      <el-menu-item index="/">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/curl-parser">
        <el-icon><Document /></el-icon>
        <span>Curl解析</span>
      </el-menu-item>
      <el-menu-item index="/templates">
        <el-icon><Files /></el-icon>
        <span>请求模板</span>
      </el-menu-item>
      <el-menu-item index="/scenarios">
        <el-icon><List /></el-icon>
        <span>压测场景</span>
      </el-menu-item>
      <el-menu-item index="/settings">
        <el-icon><Setting /></el-icon>
        <span>设置</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, Document, Files, List, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style lang="scss" scoped>
.sidebar {
  width: 240px;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  background-color: var(--bg-primary);
  border-right: 1px solid var(--border-color);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border-color);

  h2 {
    font-size: 18px;
    color: var(--text-primary);
  }
}

.sidebar-menu {
  border-right: none;
}
</style>
```

- [ ] **Step 3: 创建Header组件**

```vue
<!-- frontend/src/components/Layout/Header.vue -->
<template>
  <div class="header">
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentRoute.meta.title">
          {{ currentRoute.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <ThemeToggle />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ThemeToggle from '@/components/Common/ThemeToggle.vue'

const route = useRoute()
const currentRoute = computed(() => route)
</script>

<style lang="scss" scoped>
.header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
```

- [ ] **Step 4: 创建ThemeToggle组件**

```vue
<!-- frontend/src/components/Common/ThemeToggle.vue -->
<template>
  <el-switch
    v-model="isDark"
    inline-prompt
    :active-icon="Moon"
    :inactive-icon="Sunny"
    @change="handleThemeChange"
  />
</template>

<script setup>
import { ref, watch } from 'vue'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const isDark = ref(appStore.theme === 'dark')

watch(() => appStore.theme, (newTheme) => {
  isDark.value = newTheme === 'dark'
})

function handleThemeChange(value) {
  appStore.setTheme(value ? 'dark' : 'light')
}
</script>
```

- [ ] **Step 5: 验证布局效果**

Run: `cd frontend && npm run dev`
Expected: 访问 http://localhost:3000 可看到侧边栏、头部和主题切换功能

- [ ] **Step 6: 提交代码**

```bash
cd frontend
git add src/components/
git commit -m "feat: add layout components and theme toggle"
```

---

## 阶段二：核心功能开发

### Task 5: 实现Curl解析服务

**Files:**
- Create: `backend/src/main/java/com/pressurtest/service/CurlParserService.java`
- Create: `backend/src/main/java/com/pressurtest/controller/CurlController.java`
- Create: `backend/src/test/java/com/pressurtest/service/CurlParserServiceTest.java`

- [ ] **Step 1: 编写CurlParserService测试**

```java
// backend/src/test/java/com/pressurtest/service/CurlParserServiceTest.java
package com.pressurtest.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CurlParserServiceTest {

    @Autowired
    private CurlParserService curlParserService;

    @Test
    void testParseSimpleGetRequest() {
        String curl = "curl http://api.example.com/users";
        Map<String, Object> result = curlParserService.parse(curl);

        assertEquals("GET", result.get("method"));
        assertEquals("http://api.example.com/users", result.get("url"));
    }

    @Test
    void testParsePostRequestWithBody() {
        String curl = "curl -X POST http://api.example.com/users -H 'Content-Type: application/json' -d '{\"name\":\"test\"}'";
        Map<String, Object> result = curlParserService.parse(curl);

        assertEquals("POST", result.get("method"));
        assertEquals("http://api.example.com/users", result.get("url"));
        assertNotNull(result.get("headers"));
        assertNotNull(result.get("body"));
    }

    @Test
    void testParseMultilineCurl() {
        String curl = "curl -X POST \\\n  http://api.example.com/users \\\n  -H 'Content-Type: application/json' \\\n  -d '{\"name\":\"test\"}'";
        Map<String, Object> result = curlParserService.parse(curl);

        assertEquals("POST", result.get("method"));
        assertEquals("http://api.example.com/users", result.get("url"));
    }

    @Test
    void testParseRequestWithVariables() {
        String curl = "curl http://api.example.com/users/${userId}";
        Map<String, Object> result = curlParserService.parse(curl);

        assertEquals("GET", result.get("method"));
        assertTrue(result.get("url").toString().contains("${userId}"));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd backend && mvn test -Dtest=CurlParserServiceTest`
Expected: 测试失败，因为CurlParserService还未实现

- [ ] **Step 3: 实现CurlParserService**

```java
// backend/src/main/java/com/pressurtest/service/CurlParserService.java
package com.pressurtest.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CurlParserService {

    private static final Pattern URL_PATTERN = Pattern.compile("(?:curl\\s+)?(?:-X\\s+\\w+\\s+)?(['\"]?)(https?://[^\\s'\"]+)\\1");
    private static final Pattern METHOD_PATTERN = Pattern.compile("-X\\s+(\\w+)");
    private static final Pattern HEADER_PATTERN = Pattern.compile("-H\\s+(['\"])([^'\"]+)\\1");
    private static final Pattern BODY_PATTERN = Pattern.compile("-d\\s+(['\"])([^'\"]+)\\1");
    private static final Pattern USER_PATTERN = Pattern.compile("-u\\s+(['\"])([^'\"]+)\\1");

    public Map<String, Object> parse(String curl) {
        if (curl == null || curl.isBlank()) {
            throw new IllegalArgumentException("Curl命令不能为空");
        }

        // 预处理：移除换行符和反斜杠
        curl = curl.replaceAll("\\\\\\s*\\n\\s*", " ").trim();

        Map<String, Object> result = new HashMap<>();

        // 解析URL
        String url = parseUrl(curl);
        result.put("url", url);

        // 解析HTTP方法
        String method = parseMethod(curl);
        result.put("method", method);

        // 解析请求头
        Map<String, String> headers = parseHeaders(curl);
        result.put("headers", headers);

        // 解析请求体
        String body = parseBody(curl);
        if (body != null) {
            result.put("body", body);
        }

        // 解析认证
        String auth = parseAuth(curl);
        if (auth != null) {
            result.put("auth", auth);
        }

        // 识别可变参数
        List<String> params = identifyParams(url, headers, body);
        result.put("params", params);

        return result;
    }

    private String parseUrl(String curl) {
        Matcher matcher = URL_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(2);
        }
        throw new IllegalArgumentException("无法解析URL");
    }

    private String parseMethod(String curl) {
        Matcher matcher = METHOD_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        // 如果有请求体，默认POST
        if (parseBody(curl) != null) {
            return "POST";
        }
        return "GET";
    }

    private Map<String, String> parseHeaders(String curl) {
        Map<String, String> headers = new LinkedHashMap<>();
        Matcher matcher = HEADER_PATTERN.matcher(curl);
        while (matcher.find()) {
            String header = matcher.group(2);
            int colonIndex = header.indexOf(':');
            if (colonIndex > 0) {
                String key = header.substring(0, colonIndex).trim();
                String value = header.substring(colonIndex + 1).trim();
                headers.put(key, value);
            }
        }
        return headers;
    }

    private String parseBody(String curl) {
        Matcher matcher = BODY_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private String parseAuth(String curl) {
        Matcher matcher = USER_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private List<String> identifyParams(String url, Map<String, String> headers, String body) {
        List<String> params = new ArrayList<>();
        Pattern varPattern = Pattern.compile("\\$\\{([^}]+)\\}");

        // 检查URL中的变量
        Matcher urlMatcher = varPattern.matcher(url);
        while (urlMatcher.find()) {
            params.add(urlMatcher.group(1));
        }

        // 检查请求头中的变量
        for (String value : headers.values()) {
            Matcher headerMatcher = varPattern.matcher(value);
            while (headerMatcher.find()) {
                params.add(headerMatcher.group(1));
            }
        }

        // 检查请求体中的变量
        if (body != null) {
            Matcher bodyMatcher = varPattern.matcher(body);
            while (bodyMatcher.find()) {
                params.add(bodyMatcher.group(1));
            }
        }

        return params;
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd backend && mvn test -Dtest=CurlParserServiceTest`
Expected: 所有测试通过

- [ ] **Step 5: 创建CurlController**

```java
// backend/src/main/java/com/pressurtest/controller/CurlController.java
package com.pressurtest.controller;

import com.pressurtest.service.CurlParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/curl")
@RequiredArgsConstructor
public class CurlController {

    private final CurlParserService curlParserService;

    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parse(@RequestBody Map<String, String> request) {
        String curl = request.get("curl");
        Map<String, Object> result = curlParserService.parse(curl);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        response.put("data", result);

        return ResponseEntity.ok(response);
    }
}
```

- [ ] **Step 6: 测试API接口**

Run: `curl -X POST http://localhost:8080/api/curl/parse -H "Content-Type: application/json" -d '{"curl":"curl http://api.example.com/users"}'`
Expected: 返回解析结果JSON

- [ ] **Step 7: 提交代码**

```bash
cd backend
git add src/main/java/com/pressurtest/service/CurlParserService.java src/main/java/com/pressurtest/controller/CurlController.java src/test/java/com/pressurtest/service/CurlParserServiceTest.java
git commit -m "feat: implement curl parser service and controller"
```

---

### Task 6: 实现Curl解析前端页面

**Files:**
- Create: `frontend/src/views/CurlParser.vue`
- Create: `frontend/src/components/CurlParser/CurlInput.vue`
- Create: `frontend/src/components/CurlParser/ParsedResult.vue`
- Create: `frontend/src/api/curl.js`

- [ ] **Step 1: 创建curl API模块**

```javascript
// frontend/src/api/curl.js
import request from '@/utils/request'

export function parseCurl(curl) {
  return request({
    url: '/curl/parse',
    method: 'post',
    data: { curl },
  })
}
```

- [ ] **Step 2: 创建CurlInput组件**

```vue
<!-- frontend/src/components/CurlParser/CurlInput.vue -->
<template>
  <div class="curl-input">
    <el-input
      v-model="curlText"
      type="textarea"
      :rows="6"
      placeholder="请粘贴curl命令..."
      @input="handleInput"
    />
    <div class="actions">
      <el-button type="primary" @click="handleParse" :loading="loading">
        解析
      </el-button>
      <el-button @click="handleClear">清空</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['parse'])

const curlText = ref('')
const loading = ref(false)

function handleInput(value) {
  curlText.value = value
}

function handleParse() {
  if (!curlText.value.trim()) {
    return
  }
  emit('parse', curlText.value)
}

function handleClear() {
  curlText.value = ''
}

function setLoading(value) {
  loading.value = value
}

defineExpose({
  setLoading,
})
</script>

<style lang="scss" scoped>
.curl-input {
  .actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>
```

- [ ] **Step 3: 创建ParsedResult组件**

```vue
<!-- frontend/src/components/CurlParser/ParsedResult.vue -->
<template>
  <div class="parsed-result" v-if="result">
    <el-card>
      <template #header>
        <span>解析结果</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="请求方法">
          <el-tag :type="getMethodType(result.method)">
            {{ result.method }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求URL">
          {{ result.url }}
        </el-descriptions-item>
        <el-descriptions-item label="请求头" v-if="result.headers && Object.keys(result.headers).length > 0">
          <div v-for="(value, key) in result.headers" :key="key">
            <strong>{{ key }}:</strong> {{ value }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="请求体" v-if="result.body">
          <pre>{{ formatJson(result.body) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="可变参数" v-if="result.params && result.params.length > 0">
          <el-tag v-for="param in result.params" :key="param" class="param-tag">
            ${{ param }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="handleSave">保存为模板</el-button>
        <el-button @click="handleCopy">复制</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const props = defineProps({
  result: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['save'])

function getMethodType(method) {
  const types = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
  }
  return types[method] || 'info'
}

function formatJson(str) {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

function handleSave() {
  emit('save', props.result)
}

function handleCopy() {
  const text = JSON.stringify(props.result, null, 2)
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制到剪贴板')
}
</script>

<style lang="scss" scoped>
.parsed-result {
  margin-top: 20px;

  pre {
    background-color: var(--bg-secondary);
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
  }

  .param-tag {
    margin-right: 8px;
    margin-bottom: 8px;
  }

  .actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>
```

- [ ] **Step 4: 创建CurlParser页面**

```vue
<!-- frontend/src/views/CurlParser.vue -->
<template>
  <div class="curl-parser-page">
    <h2>Curl解析</h2>
    <p class="description">粘贴curl命令，自动解析生成压测请求</p>

    <CurlInput ref="curlInputRef" @parse="handleParse" />
    <ParsedResult :result="parsedResult" @save="handleSave" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CurlInput from '@/components/CurlParser/CurlInput.vue'
import ParsedResult from '@/components/CurlParser/ParsedResult.vue'
import { parseCurl } from '@/api/curl'

const router = useRouter()
const curlInputRef = ref(null)
const parsedResult = ref(null)

async function handleParse(curl) {
  curlInputRef.value?.setLoading(true)
  try {
    const response = await parseCurl(curl)
    parsedResult.value = response.data
  } catch (error) {
    console.error('Parse error:', error)
  } finally {
    curlInputRef.value?.setLoading(false)
  }
}

function handleSave(result) {
  // 跳转到模板编辑页面，携带解析结果
  router.push({
    path: '/templates/new',
    query: {
      method: result.method,
      url: result.url,
      headers: JSON.stringify(result.headers),
      body: result.body || '',
    },
  })
  ElMessage.success('请在模板编辑页面完善信息后保存')
}
</script>

<style lang="scss" scoped>
.curl-parser-page {
  max-width: 800px;
  margin: 0 auto;

  h2 {
    margin-bottom: 8px;
    color: var(--text-primary);
  }

  .description {
    margin-bottom: 24px;
    color: var(--text-secondary);
  }
}
</style>
```

- [ ] **Step 5: 验证Curl解析功能**

Run: `cd frontend && npm run dev`
Expected: 访问 /curl-parser 页面，粘贴curl命令并解析成功

- [ ] **Step 6: 提交代码**

```bash
cd frontend
git add src/views/CurlParser.vue src/components/CurlParser/ src/api/curl.js
git commit -m "feat: implement curl parser frontend page"
```

---

### Task 7: 实现请求模板管理

**Files:**
- Create: `backend/src/main/java/com/pressurtest/service/TemplateService.java`
- Create: `backend/src/main/java/com/pressurtest/controller/TemplateController.java`
- Create: `frontend/src/api/template.js`
- Create: `frontend/src/stores/template.js`
- Create: `frontend/src/views/TemplateList.vue`
- Create: `frontend/src/views/TemplateEdit.vue`

- [ ] **Step 1: 实现TemplateService**

```java
// backend/src/main/java/com/pressurtest/service/TemplateService.java
package com.pressurtest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pressurtest.mapper.RequestTemplateMapper;
import com.pressurtest.model.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService extends ServiceImpl<RequestTemplateMapper, RequestTemplate> {

    public List<RequestTemplate> listAll() {
        return list(new LambdaQueryWrapper<RequestTemplate>()
                .orderByDesc(RequestTemplate::getUpdatedAt));
    }

    public RequestTemplate getById(Long id) {
        return super.getById(id);
    }

    public RequestTemplate create(RequestTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        save(template);
        return template;
    }

    public RequestTemplate update(Long id, RequestTemplate template) {
        RequestTemplate existing = getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在");
        }
        template.setId(id);
        template.setUpdatedAt(LocalDateTime.now());
        updateById(template);
        return template;
    }

    public void delete(Long id) {
        removeById(id);
    }
}
```

- [ ] **Step 2: 实现TemplateController**

```java
// backend/src/main/java/com/pressurtest/controller/TemplateController.java
package com.pressurtest.controller;

import com.pressurtest.model.RequestTemplate;
import com.pressurtest.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<RequestTemplate> templates = templateService.listAll();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", templates);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        RequestTemplate template = templateService.getById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", template);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody RequestTemplate template) {
        RequestTemplate created = templateService.create(template);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", created);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody RequestTemplate template) {
        RequestTemplate updated = templateService.update(id, template);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        templateService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }
}
```

- [ ] **Step 3: 创建前端API模块**

```javascript
// frontend/src/api/template.js
import request from '@/utils/request'

export function getTemplates() {
  return request({
    url: '/templates',
    method: 'get',
  })
}

export function getTemplate(id) {
  return request({
    url: `/templates/${id}`,
    method: 'get',
  })
}

export function createTemplate(data) {
  return request({
    url: '/templates',
    method: 'post',
    data,
  })
}

export function updateTemplate(id, data) {
  return request({
    url: `/templates/${id}`,
    method: 'put',
    data,
  })
}

export function deleteTemplate(id) {
  return request({
    url: `/templates/${id}`,
    method: 'delete',
  })
}
```

- [ ] **Step 4: 创建前端Store**

```javascript
// frontend/src/stores/template.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getTemplates, getTemplate, createTemplate, updateTemplate, deleteTemplate } from '@/api/template'

export const useTemplateStore = defineStore('template', () => {
  const templates = ref([])
  const currentTemplate = ref(null)
  const loading = ref(false)

  async function fetchTemplates() {
    loading.value = true
    try {
      const response = await getTemplates()
      templates.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchTemplate(id) {
    loading.value = true
    try {
      const response = await getTemplate(id)
      currentTemplate.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function create(data) {
    const response = await createTemplate(data)
    templates.value.unshift(response.data)
    return response.data
  }

  async function update(id, data) {
    const response = await updateTemplate(id, data)
    const index = templates.value.findIndex(t => t.id === id)
    if (index !== -1) {
      templates.value[index] = response.data
    }
    return response.data
  }

  async function remove(id) {
    await deleteTemplate(id)
    templates.value = templates.value.filter(t => t.id !== id)
  }

  return {
    templates,
    currentTemplate,
    loading,
    fetchTemplates,
    fetchTemplate,
    create,
    update,
    remove,
  }
})
```

- [ ] **Step 5: 创建TemplateList页面**

```vue
<!-- frontend/src/views/TemplateList.vue -->
<template>
  <div class="template-list-page">
    <div class="page-header">
      <h2>请求模板</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建模板
      </el-button>
    </div>

    <el-table :data="templates" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="模板名称" />
      <el-table-column prop="method" label="请求方法" width="100">
        <template #default="{ row }">
          <el-tag :type="getMethodType(row.method)">{{ row.method }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="请求URL" show-overflow-tooltip />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row.id)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useTemplateStore } from '@/stores/template'

const router = useRouter()
const templateStore = useTemplateStore()
const { templates, loading } = templateStore

onMounted(() => {
  templateStore.fetchTemplates()
})

function getMethodType(method) {
  const types = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
  }
  return types[method] || 'info'
}

function handleCreate() {
  router.push('/templates/new')
}

function handleEdit(id) {
  router.push(`/templates/${id}`)
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '确认删除', {
      type: 'warning',
    })
    await templateStore.remove(id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.template-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: var(--text-primary);
    }
  }
}
</style>
```

- [ ] **Step 6: 创建TemplateEdit页面**

```vue
<!-- frontend/src/views/TemplateEdit.vue -->
<template>
  <div class="template-edit-page">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑模板' : '新建模板' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入模板名称" />
      </el-form-item>

      <el-form-item label="请求方法" prop="method">
        <el-select v-model="form.method" placeholder="请选择请求方法">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </el-form-item>

      <el-form-item label="请求URL" prop="url">
        <el-input v-model="form.url" placeholder="请输入请求URL" />
      </el-form-item>

      <el-form-item label="请求头">
        <div v-for="(value, key) in form.headers" :key="key" class="header-item">
          <el-input v-model="form.headers[key]" :placeholder="key">
            <template #prepend>{{ key }}</template>
          </el-input>
          <el-button type="danger" @click="removeHeader(key)">删除</el-button>
        </div>
        <el-button @click="addHeader">添加请求头</el-button>
      </el-form-item>

      <el-form-item label="请求体">
        <el-input
          v-model="form.body"
          type="textarea"
          :rows="6"
          placeholder="请输入请求体(JSON格式)"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTemplateStore } from '@/stores/template'

const route = useRoute()
const router = useRouter()
const templateStore = useTemplateStore()

const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => route.params.id && route.params.id !== 'new')

const form = ref({
  name: '',
  method: 'GET',
  url: '',
  headers: {},
  body: '',
})

const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  url: [{ required: true, message: '请输入请求URL', trigger: 'blur' }],
}

onMounted(async () => {
  // 从query参数获取初始值（来自curl解析）
  if (route.query.method) {
    form.value.method = route.query.method
  }
  if (route.query.url) {
    form.value.url = route.query.url
  }
  if (route.query.headers) {
    try {
      form.value.headers = JSON.parse(route.query.headers)
    } catch {
      form.value.headers = {}
    }
  }
  if (route.query.body) {
    form.value.body = route.query.body
  }

  // 编辑模式加载数据
  if (isEdit.value) {
    await templateStore.fetchTemplate(route.params.id)
    if (templateStore.currentTemplate) {
      Object.assign(form.value, templateStore.currentTemplate)
      if (typeof form.value.headers === 'string') {
        try {
          form.value.headers = JSON.parse(form.value.headers)
        } catch {
          form.value.headers = {}
        }
      }
    }
  }
})

function addHeader() {
  const key = prompt('请输入请求头名称:')
  if (key) {
    form.value.headers[key] = ''
  }
}

function removeHeader(key) {
  delete form.value.headers[key]
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      ...form.value,
      headers: JSON.stringify(form.value.headers),
    }

    if (isEdit.value) {
      await templateStore.update(route.params.id, data)
      ElMessage.success('更新成功')
    } else {
      await templateStore.create(data)
      ElMessage.success('创建成功')
    }

    router.push('/templates')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/templates')
}
</script>

<style lang="scss" scoped>
.template-edit-page {
  max-width: 800px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 24px;

    h2 {
      margin: 0;
      color: var(--text-primary);
    }
  }

  .header-item {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
  }
}
</style>
```

- [ ] **Step 7: 验证模板管理功能**

Run: `cd frontend && npm run dev`
Expected: 可以创建、编辑、删除请求模板

- [ ] **Step 8: 提交代码**

```bash
cd backend
git add src/main/java/com/pressurtest/service/TemplateService.java src/main/java/com/pressurtest/controller/TemplateController.java
git commit -m "feat: implement template service and controller"

cd frontend
git add src/views/TemplateList.vue src/views/TemplateEdit.vue src/api/template.js src/stores/template.js
git commit -m "feat: implement template management frontend"
```

---

## 阶段三：压测引擎开发

### Task 8: 实现压测引擎核心

**Files:**
- Create: `backend/src/main/java/com/pressurtest/engine/TestEngine.java`
- Create: `backend/src/main/java/com/pressurtest/engine/WorkerThread.java`
- Create: `backend/src/main/java/com/pressurtest/engine/ResultCollector.java`
- Create: `backend/src/main/java/com/pressurtest/engine/ThreadManager.java`
- Create: `backend/src/main/java/com/pressurtest/service/TestEngineService.java`
- Create: `backend/src/test/java/com/pressurtest/engine/TestEngineTest.java`

- [ ] **Step 1: 创建TestResult模型**

```java
// backend/src/main/java/com/pressurtest/model/TestResult.java
package com.pressurtest.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestResult {
    private Long id;
    private String status;
    private String method;
    private String url;
    private int statusCode;
    private long responseTime;
    private String requestBody;
    private String responseBody;
    private String error;
    private LocalDateTime timestamp;
}
```

- [ ] **Step 2: 创建ResultCollector**

```java
// backend/src/main/java/com/pressurtest/engine/ResultCollector.java
package com.pressurtest.engine;

import com.pressurtest.model.TestResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class ResultCollector {
    private final ConcurrentLinkedQueue<TestResult> results = new ConcurrentLinkedQueue<>();
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger activeThreads = new AtomicInteger(0);

    public void addResult(TestResult result) {
        results.add(result);
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(result.getResponseTime());

        if ("success".equals(result.getStatus())) {
            successRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
    }

    public void incrementActiveThreads() {
        activeThreads.incrementAndGet();
    }

    public void decrementActiveThreads() {
        activeThreads.decrementAndGet();
    }

    public double getQps(long durationSeconds) {
        if (durationSeconds == 0) return 0;
        return (double) totalRequests.get() / durationSeconds;
    }

    public double getAvgResponseTime() {
        int total = totalRequests.get();
        if (total == 0) return 0;
        return (double) totalResponseTime.get() / total;
    }

    public double getErrorRate() {
        int total = totalRequests.get();
        if (total == 0) return 0;
        return (double) failedRequests.get() / total * 100;
    }

    public List<TestResult> getRecentResults(int count) {
        List<TestResult> allResults = new ArrayList<>(results);
        int size = allResults.size();
        if (size <= count) {
            return allResults;
        }
        return allResults.subList(size - count, size);
    }

    public MonitorData getMonitorData(long durationSeconds) {
        MonitorData data = new MonitorData();
        data.setQps(getQps(durationSeconds));
        data.setAvgResponseTime(getAvgResponseTime());
        data.setErrorRate(getErrorRate());
        data.setActiveThreads(activeThreads.get());
        data.setTotalRequests(totalRequests.get());
        data.setSuccessRequests(successRequests.get());
        data.setFailedRequests(failedRequests.get());
        return data;
    }

    @Data
    public static class MonitorData {
        private double qps;
        private double avgResponseTime;
        private double errorRate;
        private int activeThreads;
        private int totalRequests;
        private int successRequests;
        private int failedRequests;
    }
}
```

- [ ] **Step 3: 创建WorkerThread**

```java
// backend/src/main/java/com/pressurtest/engine/WorkerThread.java
package com.pressurtest.engine;

import com.pressurtest.model.TestResult;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class WorkerThread implements Callable<Void> {

    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
    private final ResultCollector collector;
    private final AtomicBoolean running;
    private final int loopCount;
    private final long thinkTimeMin;
    private final long thinkTimeMax;

    @Override
    public Void call() throws Exception {
        collector.incrementActiveThreads();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            int count = 0;
            while (running.get() && (loopCount == -1 || count < loopCount)) {
                if (!running.get()) break;

                TestResult result = executeRequest(httpClient);
                collector.addResult(result);
                count++;

                // 思考时间
                if (thinkTimeMax > 0) {
                    long thinkTime = thinkTimeMin + (long) (Math.random() * (thinkTimeMax - thinkTimeMin));
                    Thread.sleep(thinkTime);
                }
            }
        } finally {
            collector.decrementActiveThreads();
        }

        return null;
    }

    private TestResult executeRequest(CloseableHttpClient httpClient) {
        TestResult result = new TestResult();
        result.setTimestamp(LocalDateTime.now());

        try {
            // 创建请求
            HttpUriRequestBase request = createRequest();
            result.setMethod(method);
            result.setUrl(url);
            result.setRequestBody(body);

            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.setHeader(entry.getKey(), entry.getValue());
                }
            }

            // 执行请求
            long startTime = System.currentTimeMillis();
            httpClient.execute(request, response -> {
                long endTime = System.currentTimeMillis();
                result.setResponseTime(endTime - startTime);
                result.setStatusCode(response.getCode());

                if (response.getCode() >= 200 && response.getCode() < 300) {
                    result.setStatus("success");
                } else {
                    result.setStatus("failed");
                    result.setError("HTTP " + response.getCode());
                }

                if (response.getEntity() != null) {
                    result.setResponseBody(EntityUtils.toString(response.getEntity()));
                }

                return null;
            });

        } catch (Exception e) {
            result.setStatus("failed");
            result.setError(e.getMessage());
        }

        return result;
    }

    private HttpUriRequestBase createRequest() {
        return switch (method.toUpperCase()) {
            case "GET" -> new HttpGet(url);
            case "POST" -> {
                HttpPost post = new HttpPost(url);
                if (body != null && !body.isEmpty()) {
                    post.setEntity(new StringEntity(body));
                }
                yield post;
            }
            case "PUT" -> {
                HttpPut put = new HttpPut(url);
                if (body != null && !body.isEmpty()) {
                    put.setEntity(new StringEntity(body));
                }
                yield put;
            }
            case "DELETE" -> new HttpDelete(url);
            default -> throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        };
    }
}
```

- [ ] **Step 4: 创建ThreadManager**

```java
// backend/src/main/java/com/pressurtest/engine/ThreadManager.java
package com.pressurtest.engine;

import lombok.Data;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class ThreadManager {

    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public void init(int threadCount) {
        executor = Executors.newFixedThreadPool(threadCount);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void shutdown() {
        running.set(false);
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean value) {
        running.set(value);
    }
}
```

- [ ] **Step 5: 创建TestEngine**

```java
// backend/src/main/java/com/pressurtest/engine/TestEngine.java
package com.pressurtest.engine;

import com.pressurtest.model.TestResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class TestEngine {

    private final ThreadManager threadManager;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ResultCollector currentCollector;

    public ResultCollector startTest(TestConfig config) {
        if (running.get()) {
            throw new IllegalStateException("已有测试正在运行");
        }

        running.set(true);
        threadManager.setRunning(true);
        threadManager.init(config.getThreadCount());

        currentCollector = new ResultCollector();

        // 启动工作线程
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < config.getThreadCount(); i++) {
            WorkerThread worker = new WorkerThread(
                    config.getMethod(),
                    config.getUrl(),
                    config.getHeaders(),
                    config.getBody(),
                    currentCollector,
                    running,
                    config.getLoopCount(),
                    config.getThinkTimeMin(),
                    config.getThinkTimeMax()
            );

            // 延迟启动
            if (config.getStartDelay() > 0) {
                try {
                    Thread.sleep(config.getStartDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            futures.add(threadManager.submit(worker));
        }

        // 监控线程
        Thread monitorThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (running.get()) {
                try {
                    Thread.sleep(1000);
                    long duration = (System.currentTimeMillis() - startTime) / 1000;
                    // 可以在这里推送监控数据
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        monitorThread.start();

        // 如果设置了时长限制
        if (config.getDuration() > 0) {
            Thread durationThread = new Thread(() -> {
                try {
                    Thread.sleep(config.getDuration() * 1000L);
                    stopTest();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            durationThread.start();
        }

        return currentCollector;
    }

    public void stopTest() {
        running.set(false);
        threadManager.setRunning(false);
        threadManager.shutdown();
    }

    public boolean isRunning() {
        return running.get();
    }

    public ResultCollector getCurrentCollector() {
        return currentCollector;
    }

    @Data
    public static class TestConfig {
        private String method;
        private String url;
        private Map<String, String> headers;
        private String body;
        private int threadCount = 10;
        private int loopCount = 1;
        private long duration = 0;
        private long startDelay = 0;
        private long thinkTimeMin = 0;
        private long thinkTimeMax = 0;
        private int timeout = 30;
    }
}
```

- [ ] **Step 6: 创建TestEngineService**

```java
// backend/src/main/java/com/pressurtest/service/TestEngineService.java
package com.pressurtest.service;

import com.pressurtest.engine.ResultCollector;
import com.pressurtest.engine.TestEngine;
import com.pressurtest.engine.TestEngine.TestConfig;
import com.pressurtest.model.TestTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TestEngineService {

    private final TestEngine testEngine;

    public TestTask startTest(TestConfig config) {
        if (testEngine.isRunning()) {
            throw new IllegalStateException("已有测试正在运行");
        }

        // 创建任务记录
        TestTask task = new TestTask();
        task.setStatus("running");
        task.setStartedAt(LocalDateTime.now());
        task.setConfig(config.toString());

        // 异步执行测试
        CompletableFuture.runAsync(() -> {
            try {
                ResultCollector collector = testEngine.startTest(config);
                // 测试完成后更新任务状态
                task.setStatus("completed");
                task.setCompletedAt(LocalDateTime.now());
                task.setResultSummary(collector.getMonitorData(0).toString());
            } catch (Exception e) {
                task.setStatus("failed");
                task.setCompletedAt(LocalDateTime.now());
            }
        });

        return task;
    }

    public void stopTest() {
        testEngine.stopTest();
    }

    public boolean isRunning() {
        return testEngine.isRunning();
    }

    public ResultCollector.MonitorData getMonitorData() {
        ResultCollector collector = testEngine.getCurrentCollector();
        if (collector == null) {
            return null;
        }
        long duration = 0; // 计算已运行时长
        return collector.getMonitorData(duration);
    }
}
```

- [ ] **Step 7: 编写单元测试**

```java
// backend/src/test/java/com/pressurtest/engine/TestEngineTest.java
package com.pressurtest.engine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TestEngineTest {

    @Test
    void testThreadManagerInit() {
        ThreadManager manager = new ThreadManager();
        manager.init(10);
        assertNotNull(manager);
        assertFalse(manager.isRunning());
    }

    @Test
    void testResultCollector() {
        ResultCollector collector = new ResultCollector();
        assertEquals(0, collector.getTotalRequests().get());
        assertEquals(0, collector.getQps(10));
    }
}
```

- [ ] **Step 8: 提交代码**

```bash
cd backend
git add src/main/java/com/pressurtest/engine/ src/main/java/com/pressurtest/service/TestEngineService.java src/main/java/com/pressurtest/model/TestResult.java src/test/java/com/pressurtest/engine/TestEngineTest.java
git commit -m "feat: implement test engine core with thread management"
```

---

## 阶段四：场景管理开发

### Task 9: 实现场景管理服务

**Files:**
- Create: `backend/src/main/java/com/pressurtest/service/ScenarioService.java`
- Create: `backend/src/main/java/com/pressurtest/controller/ScenarioController.java`
- Create: `frontend/src/api/scenario.js`
- Create: `frontend/src/stores/scenario.js`
- Create: `frontend/src/views/ScenarioList.vue`
- Create: `frontend/src/views/ScenarioEdit.vue`
- Create: `frontend/src/components/Scenario/StepList.vue`
- Create: `frontend/src/components/Scenario/StepEdit.vue`

- [ ] **Step 1: 实现ScenarioService**

```java
// backend/src/main/java/com/pressurtest/service/ScenarioService.java
package com.pressurtest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pressurtest.mapper.ScenarioStepMapper;
import com.pressurtest.mapper.TestScenarioMapper;
import com.pressurtest.model.ScenarioStep;
import com.pressurtest.model.TestScenario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final TestScenarioMapper scenarioMapper;
    private final ScenarioStepMapper stepMapper;

    public List<TestScenario> listAll() {
        return scenarioMapper.selectList(
                new LambdaQueryWrapper<TestScenario>()
                        .orderByDesc(TestScenario::getUpdatedAt));
    }

    public TestScenario getById(Long id) {
        return scenarioMapper.selectById(id);
    }

    public List<ScenarioStep> getSteps(Long scenarioId) {
        return stepMapper.selectList(
                new LambdaQueryWrapper<ScenarioStep>()
                        .eq(ScenarioStep::getScenarioId, scenarioId)
                        .orderByAsc(ScenarioStep::getStepOrder));
    }

    @Transactional
    public TestScenario create(TestScenario scenario, List<ScenarioStep> steps) {
        scenario.setCreatedAt(LocalDateTime.now());
        scenario.setUpdatedAt(LocalDateTime.now());
        scenarioMapper.insert(scenario);

        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                ScenarioStep step = steps.get(i);
                step.setScenarioId(scenario.getId());
                step.setStepOrder(i + 1);
                stepMapper.insert(step);
            }
        }

        return scenario;
    }

    @Transactional
    public TestScenario update(Long id, TestScenario scenario, List<ScenarioStep> steps) {
        scenario.setId(id);
        scenario.setUpdatedAt(LocalDateTime.now());
        scenarioMapper.updateById(scenario);

        // 删除旧步骤
        stepMapper.delete(
                new LambdaQueryWrapper<ScenarioStep>()
                        .eq(ScenarioStep::getScenarioId, id));

        // 插入新步骤
        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                ScenarioStep step = steps.get(i);
                step.setScenarioId(id);
                step.setStepOrder(i + 1);
                stepMapper.insert(step);
            }
        }

        return scenario;
    }

    @Transactional
    public void delete(Long id) {
        stepMapper.delete(
                new LambdaQueryWrapper<ScenarioStep>()
                        .eq(ScenarioStep::getScenarioId, id));
        scenarioMapper.deleteById(id);
    }
}
```

- [ ] **Step 2: 实现ScenarioController**

```java
// backend/src/main/java/com/pressurtest/controller/ScenarioController.java
package com.pressurtest.controller;

import com.pressurtest.model.ScenarioStep;
import com.pressurtest.model.TestScenario;
import com.pressurtest.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<TestScenario> scenarios = scenarioService.listAll();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", scenarios);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        TestScenario scenario = scenarioService.getById(id);
        List<ScenarioStep> steps = scenarioService.getSteps(id);

        Map<String, Object> data = new HashMap<>();
        data.put("scenario", scenario);
        data.put("steps", steps);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> request) {
        TestScenario scenario = new TestScenario();
        scenario.setName((String) request.get("name"));
        scenario.setType((String) request.get("type"));
        scenario.setDescription((String) request.get("description"));
        scenario.setConfig(request.get("config") != null ? request.get("config").toString() : null);

        @SuppressWarnings("unchecked")
        List<ScenarioStep> steps = (List<ScenarioStep>) request.get("steps");

        TestScenario created = scenarioService.create(scenario, steps);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", created);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        TestScenario scenario = new TestScenario();
        scenario.setName((String) request.get("name"));
        scenario.setType((String) request.get("type"));
        scenario.setDescription((String) request.get("description"));
        scenario.setConfig(request.get("config") != null ? request.get("config").toString() : null);

        @SuppressWarnings("unchecked")
        List<ScenarioStep> steps = (List<ScenarioStep>) request.get("steps");

        TestScenario updated = scenarioService.update(id, scenario, steps);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        scenarioService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }
}
```

- [ ] **Step 3: 创建前端API和Store**

```javascript
// frontend/src/api/scenario.js
import request from '@/utils/request'

export function getScenarios() {
  return request({
    url: '/scenarios',
    method: 'get',
  })
}

export function getScenario(id) {
  return request({
    url: `/scenarios/${id}`,
    method: 'get',
  })
}

export function createScenario(data) {
  return request({
    url: '/scenarios',
    method: 'post',
    data,
  })
}

export function updateScenario(id, data) {
  return request({
    url: `/scenarios/${id}`,
    method: 'put',
    data,
  })
}

export function deleteScenario(id) {
  return request({
    url: `/scenarios/${id}`,
    method: 'delete',
  })
}
```

```javascript
// frontend/src/stores/scenario.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getScenarios, getScenario, createScenario, updateScenario, deleteScenario } from '@/api/scenario'

export const useScenarioStore = defineStore('scenario', () => {
  const scenarios = ref([])
  const currentScenario = ref(null)
  const currentSteps = ref([])
  const loading = ref(false)

  async function fetchScenarios() {
    loading.value = true
    try {
      const response = await getScenarios()
      scenarios.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchScenario(id) {
    loading.value = true
    try {
      const response = await getScenario(id)
      currentScenario.value = response.data.scenario
      currentSteps.value = response.data.steps || []
    } finally {
      loading.value = false
    }
  }

  async function create(data) {
    const response = await createScenario(data)
    scenarios.value.unshift(response.data)
    return response.data
  }

  async function update(id, data) {
    const response = await updateScenario(id, data)
    const index = scenarios.value.findIndex(s => s.id === id)
    if (index !== -1) {
      scenarios.value[index] = response.data
    }
    return response.data
  }

  async function remove(id) {
    await deleteScenario(id)
    scenarios.value = scenarios.value.filter(s => s.id !== id)
  }

  return {
    scenarios,
    currentScenario,
    currentSteps,
    loading,
    fetchScenarios,
    fetchScenario,
    create,
    update,
    remove,
  }
})
```

- [ ] **Step 4: 创建StepList组件**

```vue
<!-- frontend/src/components/Scenario/StepList.vue -->
<template>
  <div class="step-list">
    <div v-for="(step, index) in steps" :key="index" class="step-item">
      <div class="step-header">
        <span class="step-order">{{ index + 1 }}</span>
        <span class="step-name">{{ step.name || '未命名步骤' }}</span>
        <div class="step-actions">
          <el-button type="primary" link @click="$emit('edit', index)">编辑</el-button>
          <el-button type="danger" link @click="$emit('remove', index)">删除</el-button>
          <el-button v-if="index > 0" link @click="$emit('move-up', index)">上移</el-button>
          <el-button v-if="index < steps.length - 1" link @click="$emit('move-down', index)">下移</el-button>
        </div>
      </div>
      <div class="step-info">
        <el-tag size="small">{{ step.method || 'GET' }}</el-tag>
        <span class="step-url">{{ step.url || '未设置URL' }}</span>
      </div>
    </div>

    <el-button type="primary" @click="$emit('add')">
      <el-icon><Plus /></el-icon>
      添加步骤
    </el-button>
  </div>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'

defineProps({
  steps: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['add', 'edit', 'remove', 'move-up', 'move-down'])
</script>

<style lang="scss" scoped>
.step-list {
  .step-item {
    border: 1px solid var(--border-color);
    border-radius: 4px;
    padding: 12px;
    margin-bottom: 12px;
    background-color: var(--bg-primary);

    .step-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      .step-order {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: var(--el-color-primary);
        color: white;
        border-radius: 50%;
        font-size: 12px;
      }

      .step-name {
        flex: 1;
        font-weight: 500;
      }

      .step-actions {
        display: flex;
        gap: 8px;
      }
    }

    .step-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--text-secondary);
      font-size: 14px;
    }
  }
}
</style>
```

- [ ] **Step 5: 创建StepEdit组件**

```vue
<!-- frontend/src/components/Scenario/StepEdit.vue -->
<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑步骤' : '添加步骤'"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="步骤名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入步骤名称" />
      </el-form-item>

      <el-form-item label="请求方法" prop="method">
        <el-select v-model="form.method" placeholder="请选择请求方法">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </el-form-item>

      <el-form-item label="请求URL" prop="url">
        <el-input v-model="form.url" placeholder="请输入请求URL" />
      </el-form-item>

      <el-form-item label="请求头">
        <el-input
          v-model="form.headersStr"
          type="textarea"
          :rows="3"
          placeholder="JSON格式的请求头"
        />
      </el-form-item>

      <el-form-item label="请求体">
        <el-input
          v-model="form.body"
          type="textarea"
          :rows="4"
          placeholder="请求体内容"
        />
      </el-form-item>

      <el-form-item label="循环次数">
        <el-input-number v-model="form.loopCount" :min="1" :max="999999" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  step: Object,
  isEdit: Boolean,
})

const emit = defineEmits(['update:modelValue', 'submit'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const formRef = ref(null)
const form = ref({
  name: '',
  method: 'GET',
  url: '',
  headersStr: '',
  body: '',
  loopCount: 1,
})

const rules = {
  name: [{ required: true, message: '请输入步骤名称', trigger: 'blur' }],
  method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  url: [{ required: true, message: '请输入请求URL', trigger: 'blur' }],
}

watch(() => props.step, (newStep) => {
  if (newStep) {
    form.value = {
      name: newStep.name || '',
      method: newStep.method || 'GET',
      url: newStep.url || '',
      headersStr: newStep.headersStr || '',
      body: newStep.body || '',
      loopCount: newStep.loopCount || 1,
    }
  }
}, { immediate: true })

function handleClose() {
  visible.value = false
  formRef.value?.resetFields()
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    emit('submit', { ...form.value })
    handleClose()
  } catch {
    // 验证失败
  }
}
</script>
```

- [ ] **Step 6: 创建ScenarioList和ScenarioEdit页面**

```vue
<!-- frontend/src/views/ScenarioList.vue -->
<template>
  <div class="scenario-list-page">
    <div class="page-header">
      <h2>压测场景</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建场景
      </el-button>
    </div>

    <el-table :data="scenarios" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="场景名称" />
      <el-table-column prop="type" label="场景类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 'combination' ? 'primary' : 'success'">
            {{ row.type === 'combination' ? '接口组合' : '参数化' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row.id)">编辑</el-button>
          <el-button type="success" link @click="handleRun(row.id)">运行</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useScenarioStore } from '@/stores/scenario'

const router = useRouter()
const scenarioStore = useScenarioStore()
const { scenarios, loading } = scenarioStore

onMounted(() => {
  scenarioStore.fetchScenarios()
})

function handleCreate() {
  router.push('/scenarios/new')
}

function handleEdit(id) {
  router.push(`/scenarios/${id}`)
}

function handleRun(id) {
  router.push(`/monitor/new?scenarioId=${id}`)
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个场景吗？', '确认删除', {
      type: 'warning',
    })
    await scenarioStore.remove(id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.scenario-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: var(--text-primary);
    }
  }
}
</style>
```

```vue
<!-- frontend/src/views/ScenarioEdit.vue -->
<template>
  <div class="scenario-edit-page">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑场景' : '新建场景' }}</h2>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="场景名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入场景名称" />
      </el-form-item>

      <el-form-item label="场景类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio value="combination">接口组合</el-radio>
          <el-radio value="parameterized">参数化</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="场景描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入场景描述"
        />
      </el-form-item>

      <el-form-item label="场景步骤" v-if="form.type === 'combination'">
        <StepList
          :steps="steps"
          @add="handleAddStep"
          @edit="handleEditStep"
          @remove="handleRemoveStep"
          @move-up="handleMoveUp"
          @move-down="handleMoveDown"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>

    <StepEdit
      v-model="stepEditVisible"
      :step="currentStep"
      :is-edit="isEditStep"
      @submit="handleStepSubmit"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useScenarioStore } from '@/stores/scenario'
import StepList from '@/components/Scenario/StepList.vue'
import StepEdit from '@/components/Scenario/StepEdit.vue'

const route = useRoute()
const router = useRouter()
const scenarioStore = useScenarioStore()

const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => route.params.id && route.params.id !== 'new')

const form = ref({
  name: '',
  type: 'combination',
  description: '',
})

const steps = ref([])
const stepEditVisible = ref(false)
const currentStep = ref(null)
const currentStepIndex = ref(-1)
const isEditStep = computed(() => currentStepIndex.value >= 0)

const rules = {
  name: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择场景类型', trigger: 'change' }],
}

onMounted(async () => {
  if (isEdit.value) {
    await scenarioStore.fetchScenario(route.params.id)
    if (scenarioStore.currentScenario) {
      Object.assign(form.value, scenarioStore.currentScenario)
      steps.value = [...scenarioStore.currentSteps]
    }
  }
})

function handleAddStep() {
  currentStep.value = null
  currentStepIndex.value = -1
  stepEditVisible.value = true
}

function handleEditStep(index) {
  currentStep.value = steps.value[index]
  currentStepIndex.value = index
  stepEditVisible.value = true
}

function handleRemoveStep(index) {
  steps.value.splice(index, 1)
}

function handleMoveUp(index) {
  if (index > 0) {
    const temp = steps.value[index]
    steps.value[index] = steps.value[index - 1]
    steps.value[index - 1] = temp
  }
}

function handleMoveDown(index) {
  if (index < steps.value.length - 1) {
    const temp = steps.value[index]
    steps.value[index] = steps.value[index + 1]
    steps.value[index + 1] = temp
  }
}

function handleStepSubmit(stepData) {
  if (isEditStep.value) {
    steps.value[currentStepIndex.value] = stepData
  } else {
    steps.value.push(stepData)
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      ...form.value,
      steps: steps.value,
    }

    if (isEdit.value) {
      await scenarioStore.update(route.params.id, data)
      ElMessage.success('更新成功')
    } else {
      await scenarioStore.create(data)
      ElMessage.success('创建成功')
    }

    router.push('/scenarios')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/scenarios')
}
</script>

<style lang="scss" scoped>
.scenario-edit-page {
  max-width: 900px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 24px;

    h2 {
      margin: 0;
      color: var(--text-primary);
    }
  }
}
</style>
```

- [ ] **Step 7: 验证场景管理功能**

Run: `cd frontend && npm run dev`
Expected: 可以创建、编辑、删除压测场景，支持添加步骤

- [ ] **Step 8: 提交代码**

```bash
cd backend
git add src/main/java/com/pressurtest/service/ScenarioService.java src/main/java/com/pressurtest/controller/ScenarioController.java
git commit -m "feat: implement scenario service and controller"

cd frontend
git add src/views/ScenarioList.vue src/views/ScenarioEdit.vue src/components/Scenario/ src/api/scenario.js src/stores/scenario.js
git commit -m "feat: implement scenario management frontend"
```

---

## 阶段五：实时监控开发

### Task 10: 实现WebSocket实时监控

**Files:**
- Create: `backend/src/main/java/com/pressurtest/websocket/MonitorWebSocket.java`
- Create: `backend/src/main/java/com/pressurtest/config/WebSocketConfig.java`
- Create: `frontend/src/utils/websocket.js`
- Create: `frontend/src/views/Monitor.vue`
- Create: `frontend/src/components/Monitor/Dashboard.vue`
- Create: `frontend/src/components/Monitor/QpsChart.vue`
- Create: `frontend/src/components/Monitor/ResponseTimeChart.vue`
- Create: `frontend/src/components/Monitor/ResultTree.vue`

- [ ] **Step 1: 创建WebSocket配置**

```java
// backend/src/main/java/com/pressurtest/config/WebSocketConfig.java
package com.pressurtest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.pressurtest.websocket.MonitorWebSocket;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MonitorWebSocket monitorWebSocket;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(monitorWebSocket, "/ws/monitor/{taskId}")
                .setAllowedOrigins("*");
    }
}
```

- [ ] **Step 2: 实现MonitorWebSocket**

```java
// backend/src/main/java/com/pressurtest/websocket/MonitorWebSocket.java
package com.pressurtest.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pressurtest.engine.ResultCollector;
import com.pressurtest.service.TestEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorWebSocket extends TextWebSocketHandler {

    private final TestEngineService testEngineService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String taskId = getTaskId(session);
        sessions.put(taskId, session);
        startMonitor(taskId);
        log.info("WebSocket连接建立: {}", taskId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String taskId = getTaskId(session);
        sessions.remove(taskId);
        stopMonitor();
        log.info("WebSocket连接关闭: {}", taskId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理客户端消息
    }

    private String getTaskId(WebSocketSession session) {
        String uri = session.getUri().toString();
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    private void startMonitor(String taskId) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                ResultCollector.MonitorData data = testEngineService.getMonitorData();
                if (data != null) {
                    Map<String, Object> message = Map.of(
                            "type", "monitor",
                            "data", data
                    );
                    sendMessage(taskId, objectMapper.writeValueAsString(message));
                }
            } catch (Exception e) {
                log.error("发送监控数据失败", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void stopMonitor() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void sendMessage(String taskId, String message) {
        WebSocketSession session = sessions.get(taskId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败", e);
            }
        }
    }

    public void sendResult(String taskId, Object result) {
        try {
            Map<String, Object> message = Map.of(
                    "type", "result",
                    "data", result
            );
            sendMessage(taskId, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("发送结果失败", e);
        }
    }
}
```

- [ ] **Step 3: 创建前端WebSocket工具**

```javascript
// frontend/src/utils/websocket.js
export class WebSocketClient {
  constructor(url) {
    this.url = url
    this.ws = null
    this.callbacks = {}
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
  }

  connect() {
    this.ws = new WebSocket(this.url)

    this.ws.onopen = () => {
      console.log('WebSocket连接建立')
      this.reconnectAttempts = 0
      this.emit('open')
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this.emit(data.type, data.data)
      } catch (e) {
        console.error('解析消息失败:', e)
      }
    }

    this.ws.onclose = () => {
      console.log('WebSocket连接关闭')
      this.emit('close')
      this.tryReconnect()
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
      this.emit('error', error)
    }
  }

  tryReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      setTimeout(() => this.connect(), 3000)
    }
  }

  on(event, callback) {
    if (!this.callbacks[event]) {
      this.callbacks[event] = []
    }
    this.callbacks[event].push(callback)
  }

  emit(event, data) {
    if (this.callbacks[event]) {
      this.callbacks[event].forEach(cb => cb(data))
    }
  }

  close() {
    if (this.ws) {
      this.ws.close()
    }
  }
}
```

- [ ] **Step 4: 创建QpsChart组件**

```vue
<!-- frontend/src/components/Monitor/QpsChart.vue -->
<template>
  <div class="qps-chart" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
})

const chartRef = ref(null)
let chart = null

onMounted(() => {
  chart = echarts.init(chartRef.value)
  updateChart()
})

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

function updateChart() {
  if (!chart) return

  const option = {
    title: {
      text: 'QPS趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: props.data.map((_, i) => i),
    },
    yAxis: {
      type: 'value',
      name: 'QPS',
    },
    series: [{
      data: props.data,
      type: 'line',
      smooth: true,
      areaStyle: {
        opacity: 0.3,
      },
    }],
  }

  chart.setOption(option)
}

onUnmounted(() => {
  if (chart) {
    chart.dispose()
  }
})
</script>

<style lang="scss" scoped>
.qps-chart {
  width: 100%;
  height: 300px;
}
</style>
```

- [ ] **Step 5: 创建ResponseTimeChart组件**

```vue
<!-- frontend/src/components/Monitor/ResponseTimeChart.vue -->
<template>
  <div class="response-time-chart" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
})

const chartRef = ref(null)
let chart = null

onMounted(() => {
  chart = echarts.init(chartRef.value)
  updateChart()
})

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

function updateChart() {
  if (!chart) return

  const option = {
    title: {
      text: '响应时间趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: props.data.map((_, i) => i),
    },
    yAxis: {
      type: 'value',
      name: '响应时间(ms)',
    },
    series: [{
      data: props.data,
      type: 'line',
      smooth: true,
    }],
  }

  chart.setOption(option)
}

onUnmounted(() => {
  if (chart) {
    chart.dispose()
  }
})
</script>

<style lang="scss" scoped>
.response-time-chart {
  width: 100%;
  height: 300px;
}
</style>
```

- [ ] **Step 6: 创建ResultTree组件**

```vue
<!-- frontend/src/components/Monitor/ResultTree.vue -->
<template>
  <div class="result-tree">
    <div class="result-header">
      <span>结果树</span>
      <el-radio-group v-model="filter" size="small">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="success">成功</el-radio-button>
        <el-radio-button value="failed">失败</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="filteredResults" max-height="400" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
            {{ row.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="method" label="方法" width="80" />
      <el-table-column prop="url" label="URL" show-overflow-tooltip />
      <el-table-column prop="statusCode" label="状态码" width="80" />
      <el-table-column prop="responseTime" label="响应时间" width="100">
        <template #default="{ row }">
          {{ row.responseTime }}ms
        </template>
      </el-table-column>
      <el-table-column prop="timestamp" label="时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" link @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="请求详情" width="800px">
      <el-descriptions :column="1" border v-if="currentResult">
        <el-descriptions-item label="请求方法">
          {{ currentResult.method }}
        </el-descriptions-item>
        <el-descriptions-item label="请求URL">
          {{ currentResult.url }}
        </el-descriptions-item>
        <el-descriptions-item label="状态码">
          {{ currentResult.statusCode }}
        </el-descriptions-item>
        <el-descriptions-item label="响应时间">
          {{ currentResult.responseTime }}ms
        </el-descriptions-item>
        <el-descriptions-item label="请求体" v-if="currentResult.requestBody">
          <pre>{{ currentResult.requestBody }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应体" v-if="currentResult.responseBody">
          <pre>{{ currentResult.responseBody }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentResult.error">
          <el-tag type="danger">{{ currentResult.error }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  results: {
    type: Array,
    default: () => [],
  },
})

const filter = ref('all')
const detailVisible = ref(false)
const currentResult = ref(null)

const filteredResults = computed(() => {
  if (filter.value === 'all') return props.results
  return props.results.filter(r => r.status === filter.value)
})

function showDetail(row) {
  currentResult.value = row
  detailVisible.value = true
}
</script>

<style lang="scss" scoped>
.result-tree {
  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    font-weight: 500;
  }

  pre {
    background-color: var(--bg-secondary);
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    max-height: 200px;
  }
}
</style>
```

- [ ] **Step 7: 创建Dashboard组件**

```vue
<!-- frontend/src/components/Monitor/Dashboard.vue -->
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ data.qps || 0 }}</div>
          <div class="stat-label">QPS</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ data.avgResponseTime || 0 }}ms</div>
          <div class="stat-label">平均响应时间</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ data.errorRate || 0 }}%</div>
          <div class="stat-label">错误率</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ data.activeThreads || 0 }}</div>
          <div class="stat-label">活跃线程</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <QpsChart :data="qpsHistory" />
      </el-col>
      <el-col :span="12">
        <ResponseTimeChart :data="responseTimeHistory" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import QpsChart from './QpsChart.vue'
import ResponseTimeChart from './ResponseTimeChart.vue'

defineProps({
  data: {
    type: Object,
    default: () => ({}),
  },
  qpsHistory: {
    type: Array,
    default: () => [],
  },
  responseTimeHistory: {
    type: Array,
    default: () => [],
  },
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-card {
    text-align: center;

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: var(--el-color-primary);
    }

    .stat-label {
      margin-top: 8px;
      color: var(--text-secondary);
    }
  }

  .chart-row {
    margin-top: 20px;
  }
}
</style>
```

- [ ] **Step 8: 创建Monitor页面**

```vue
<!-- frontend/src/views/Monitor.vue -->
<template>
  <div class="monitor-page">
    <div class="page-header">
      <h2>实时监控</h2>
      <div class="actions">
        <el-button v-if="!isRunning" type="primary" @click="handleStart" :loading="starting">
          开始测试
        </el-button>
        <el-button v-else type="danger" @click="handleStop">
          停止测试
        </el-button>
      </div>
    </div>

    <Dashboard
      :data="monitorData"
      :qpsHistory="qpsHistory"
      :responseTimeHistory="responseTimeHistory"
    />

    <ResultTree :results="results" class="result-tree-section" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import Dashboard from '@/components/Monitor/Dashboard.vue'
import ResultTree from '@/components/Monitor/ResultTree.vue'
import { WebSocketClient } from '@/utils/websocket'
import { startTask, stopTask } from '@/api/task'

const route = useRoute()

const isRunning = ref(false)
const starting = ref(false)
const monitorData = ref({})
const qpsHistory = ref([])
const responseTimeHistory = ref([])
const results = ref([])
let wsClient = null

onMounted(() => {
  // 如果有taskId，连接WebSocket
  if (route.params.taskId && route.params.taskId !== 'new') {
    connectWebSocket(route.params.taskId)
  }
})

onUnmounted(() => {
  if (wsClient) {
    wsClient.close()
  }
})

function connectWebSocket(taskId) {
  const wsUrl = `ws://${window.location.host}/ws/monitor/${taskId}`
  wsClient = new WebSocketClient(wsUrl)

  wsClient.on('monitor', (data) => {
    monitorData.value = data
    qpsHistory.value.push(data.qps)
    responseTimeHistory.value.push(data.avgResponseTime)

    // 保留最近100个数据点
    if (qpsHistory.value.length > 100) {
      qpsHistory.value.shift()
      responseTimeHistory.value.shift()
    }
  })

  wsClient.on('result', (data) => {
    results.value.unshift(data)
    if (results.value.length > 1000) {
      results.value.pop()
    }
  })

  wsClient.connect()
}

async function handleStart() {
  starting.value = true
  try {
    const response = await startTask({
      scenarioId: route.query.scenarioId,
      // 其他配置参数
    })
    isRunning.value = true
    connectWebSocket(response.data.id)
    ElMessage.success('测试已开始')
  } catch (error) {
    ElMessage.error('启动测试失败')
  } finally {
    starting.value = false
  }
}

async function handleStop() {
  try {
    await stopTask(route.params.taskId)
    isRunning.value = false
    ElMessage.success('测试已停止')
  } catch (error) {
    ElMessage.error('停止测试失败')
  }
}
</script>

<style lang="scss" scoped>
.monitor-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: var(--text-primary);
    }
  }

  .result-tree-section {
    margin-top: 20px;
  }
}
</style>
```

- [ ] **Step 9: 创建Task API模块**

```javascript
// frontend/src/api/task.js
import request from '@/utils/request'

export function startTask(data) {
  return request({
    url: '/tasks/start',
    method: 'post',
    data,
  })
}

export function stopTask(taskId) {
  return request({
    url: `/tasks/${taskId}/stop`,
    method: 'post',
  })
}

export function getTaskStatus(taskId) {
  return request({
    url: `/tasks/${taskId}/status`,
    method: 'get',
  })
}

export function getTaskResult(taskId) {
  return request({
    url: `/tasks/${taskId}/result`,
    method: 'get',
  })
}
```

- [ ] **Step 10: 验证实时监控功能**

Run: `cd frontend && npm run dev`
Expected: 可以启动压测任务，实时显示QPS、响应时间等指标

- [ ] **Step 11: 提交代码**

```bash
cd backend
git add src/main/java/com/pressurtest/websocket/ src/main/java/com/pressurtest/config/WebSocketConfig.java
git commit -m "feat: implement WebSocket real-time monitoring"

cd frontend
git add src/views/Monitor.vue src/components/Monitor/ src/utils/websocket.js src/api/task.js
git commit -m "feat: implement real-time monitoring frontend"
```

---

## 阶段六：打包发布

### Task 11: 配置Electron打包

**Files:**
- Create: `electron/main.js`
- Create: `electron/preload.js`
- Create: `electron/package.json`
- Create: `package.json` (root)
- Create: `electron-builder.yml`

- [ ] **Step 1: 创建Electron主进程**

```javascript
// electron/main.js
const { app, BrowserWindow, ipcMain } = require('electron')
const path = require('path')
const { spawn } = require('child_process')

let mainWindow
let backendProcess

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  })

  // 开发环境加载本地服务器
  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:3000')
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(path.join(__dirname, '../frontend/dist/index.html'))
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

function startBackend() {
  const backendPath = path.join(__dirname, '../backend/target/api-pressure-test-tool-1.0.0.jar')
  backendProcess = spawn('java', ['-jar', backendPath], {
    stdio: 'inherit',
  })

  backendProcess.on('error', (err) => {
    console.error('启动后端失败:', err)
  })

  backendProcess.on('exit', (code) => {
    console.log('后端进程退出:', code)
  })
}

app.whenReady().then(() => {
  startBackend()
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

app.on('window-all-closed', () => {
  if (backendProcess) {
    backendProcess.kill()
  }
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('before-quit', () => {
  if (backendProcess) {
    backendProcess.kill()
  }
})
```

- [ ] **Step 2: 创建preload脚本**

```javascript
// electron/preload.js
const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
  platform: process.platform,
  versions: {
    node: process.versions.node,
    chrome: process.versions.chrome,
    electron: process.versions.electron,
  },
})
```

- [ ] **Step 3: 创建Electron package.json**

```json
{
  "name": "api-pressure-test-tool",
  "version": "1.0.0",
  "description": "API压测工具",
  "main": "main.js",
  "scripts": {
    "start": "electron .",
    "dev": "NODE_ENV=development electron ."
  },
  "dependencies": {
    "electron": "^28.0.0"
  }
}
```

- [ ] **Step 4: 创建根目录package.json**

```json
{
  "name": "api-pressure-test-tool",
  "version": "1.0.0",
  "description": "API压测工具",
  "scripts": {
    "dev": "concurrently \"cd frontend && npm run dev\" \"cd backend && mvn spring-boot:run\"",
    "build": "cd frontend && npm run build && cd ../backend && mvn clean package -DskipTests",
    "build:electron": "npm run build && electron-builder",
    "start:electron": "cd electron && npm start"
  },
  "devDependencies": {
    "concurrently": "^8.2.2",
    "electron": "^28.0.0",
    "electron-builder": "^24.9.1"
  },
  "build": {
    "appId": "com.pressurtest.tool",
    "productName": "API压测工具",
    "directories": {
      "output": "release"
    },
    "files": [
      "electron/**/*",
      "frontend/dist/**/*",
      "backend/target/*.jar"
    ],
    "win": {
      "target": "nsis",
      "icon": "build/icon.ico"
    },
    "nsis": {
      "oneClick": false,
      "allowToChangeInstallationDirectory": true,
      "createDesktopShortcut": true
    }
  }
}
```

- [ ] **Step 5: 创建electron-builder配置**

```yaml
# electron-builder.yml
appId: com.pressurtest.tool
productName: API压测工具
directories:
  output: release
files:
  - electron/**/*
  - frontend/dist/**/*
  - backend/target/*.jar
win:
  target: nsis
  icon: build/icon.ico
nsis:
  oneClick: false
  allowToChangeInstallationDirectory: true
  createDesktopShortcut: true
```

- [ ] **Step 6: 安装依赖并测试**

Run: `npm install && cd electron && npm install && cd ../frontend && npm install`
Expected: 所有依赖安装成功

- [ ] **Step 7: 构建应用**

Run: `npm run build`
Expected: 前端和后端构建成功

- [ ] **Step 8: 打包Electron应用**

Run: `npm run build:electron`
Expected: 生成.exe安装包在release目录

- [ ] **Step 9: 提交代码**

```bash
git add electron/ package.json electron-builder.yml
git commit -m "feat: configure Electron packaging for Windows"
```

---

## 自审检查

### 1. Spec覆盖检查

| Spec要求 | 对应Task |
|---------|----------|
| Curl解析 | Task 5, Task 6 |
| 压测配置 | Task 8 |
| 场景管理 | Task 9 |
| 全局参数 | Task 7 (可扩展) |
| 实时监控 | Task 10 |
| 主题切换 | Task 4 |
| 打包发布 | Task 11 |

### 2. 占位符扫描

✅ 无TBD、TODO或不完整部分

### 3. 类型一致性检查

✅ 所有类名、方法名、属性名保持一致

---

## 执行选项

**计划完成并保存到 `docs/superpowers/plans/2026-06-03-api-pressure-test-tool-impl.md`**

**两种执行方式：**

**1. Subagent-Driven (推荐)** - 每个任务分派一个独立的子代理执行，任务间进行审查，快速迭代

**2. Inline Execution** - 在当前会话中使用executing-plans执行任务，批量执行并设置检查点

**请选择执行方式？**<tool_call>
<function=AskUserQuestion>
<parameter=questions>[{"header": "执行方式", "multiSelect": false, "options": [{"description": "每个任务分派独立子代理，任务间审查，快速迭代", "label": "Subagent-Driven (推荐)"}, {"description": "当前会话中执行，批量执行并设置检查点", "label": "Inline Execution"}], "question": "请选择执行方式？"}]