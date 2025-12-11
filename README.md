# 简单网页聊天室

## 项目概述

这是一个基于 Jakarta EE 的简单网页聊天室应用，使用 Servlet、JSP 和原生 JavaScript 技术实现。该项目实现了用户登录/注册、在线消息发送和展示等功能。

## 技术栈

### 后端技术
- **Java**: JDK 21
- **Jakarta Servlet API**: 6.1.0
- **JSP**: Java Server Pages
- **构建工具**: Maven 
- **服务器**: Apache Tomcat 11

### 前端技术
- **Fetch API**

## 核心功能

### 1. 用户认证系统

#### 登录与注册
- 用户首次访问自动跳转到登录页面（`login.jsp`）
- 支持用户名和密码登录
- 如果用户名不存在，系统自动注册新用户
- 使用 `HttpSession` 维持用户登录状态
- 用户数据存储在Servlet实例的 `ServletContext` 中（应用级别共享）

#### 用户输入验证
**前端验证：**
- 用户名不能为空
- 用户名只能包含中英文、数字和下划线（正则表达式：`/^[\u4e00-\u9fa5a-zA-Z0-9_]+$/`）
- 用户名长度限制：最多 100 个字符
- 密码不能为空
- 密码长度限制：6-50 个字符

**后端验证：**
- 在 `LoginServlet` 中进行相同的验证
- 密码匹配验证（已注册用户）
- 使用 `trim()` 方法去除首尾空格

### 2. 消息发送与展示

#### 消息发送 (`ChatServlet`)
- 用户必须登录才能发送消息
- 每条消息自动附加时间戳（格式：`HH:mm:ss`）
- 消息格式：`[时间] 用户名: 消息内容`
- 消息存储在 `ServletContext` 中的同步列表中
- 前端验证消息不能为空或仅包含空格
- 消息长度限制：最多 100 个字符

#### 消息展示 (`MessageApiServlet`)
- 提供 RESTful API (`/api/messages`) 给前端js返回 JSON 格式数据
- 返回内容包括：当前登录用户名和所有消息列表
- 前端使用 Fetch API 异步获取消息
- 自动刷新：每 2 秒轮询一次服务器获取最新消息
- HTML 转义处理防止 XSS 攻击

### 3. 会话管理

#### Session 使用
- 登录成功后在 Session 中存储 `loggedInUsername` 属性
- 聊天页面加载时检查 Session，未登录则重定向到登录页
- 退出登录功能（通过用户手动点击按钮触发）

#### ServletContext 使用
- 存储所有用户信息（`registeredUsers`）：`Map<String, User>`
- 存储所有聊天消息（`messages`）：`List<String>`
- 使用同步集合保证线程安全：
  - `Collections.synchronizedMap()` 
  - `Collections.synchronizedList()`

## 核心类说明

### 1. User.java
用户实体类，实现了 `Serializable` 接口。

**属性：**
- `username`: 用户名
- `password`: 密码

**方法：**
- 标准的 Getter/Setter 方法
- 重写 `equals()` 和 `hashCode()` 方法（基于用户名）

### 2. LoginServlet.java
处理用户登录和自动注册逻辑。

**URL 映射：** `/LoginServlet`

**主要功能：**
- 接收 POST 请求（用户名和密码）
- 从 ServletContext 获取/创建用户 Map
- 验证用户名格式和长度
- 验证密码长度（6-50字符）
- 检查用户是否存在：
  - 已存在：验证密码
  - 不存在：自动注册新用户
- 登录成功后设置 Session 属性
- 返回 HTTP 状态码表示成功或失败

### 3. ChatServlet.java
处理消息发送请求。

**URL 映射：** `/ChatServlet`

**主要功能：**
- 接收 POST 请求（消息内容）
- 检查用户登录状态（Session验证）
- 验证消息不为空
- 从 Session 获取当前用户名
- 从 ServletContext 获取/创建消息列表
- 添加时间戳并存储消息
- 返回成功或失败状态

### 4. MessageApiServlet.java
提供消息列表查询 API。

**URL 映射：** `/api/messages`

**主要功能：**
- 接收 GET 请求
- 从 ServletContext 获取消息列表
- 从 Session 获取当前登录用户名
- JSON 特殊字符转义处理

**JavaScript 功能：**
- `fetchMessages()`: 异步获取消息列表
  - 使用 Fetch API 调用 `/api/messages`
  - 解析 JSON 响应
  - 调用 `displayMessages()` 显示消息
  
- `displayMessages(messages)`: 渲染消息列表
  - HTML 转义处理（`escapeHtml()`）
  - 空消息提示
  - 自动滚动到底部
  
- 表单提交处理：
  - 消息验证（不能为空或仅空格）
  - 长度限制（100字符）
  - 使用 Fetch API 发送 POST 请求到 `/ChatServlet`
  - 发送成功后清空输入框并刷新消息列表
  - 未登录自动跳转到登录页
  
- 自动刷新机制：
  - 使用 `setInterval()` 每 2 秒自动刷新消息
  - 页面加载时立即获取消息

### 关键技术点

#### 1. 异步编程（Async/Await）
```javascript
async function fetchMessages() {
    const response = await fetch(contextPath + '/api/messages');
    const data = await response.json();
    displayMessages(data.messages);
}
```
- 使用 `async/await` 简化异步代码
- `await fetch()`: 等待网络请求完成
- `await response.json()`: 等待响应体解析完成
- `await response.text()`: 等待文本内容读取完成

#### 2. XSS 防护
- 前端使用 `escapeHtml()` 函数转义 HTML 特殊字符
- 后端 `MessageApiServlet` 中使用 `escapeJson()` 转义 JSON 特殊字符

#### 3. 上下文路径处理
```javascript
const contextPath = '<%= request.getContextPath() %>';
```
- 动态获取应用上下文路径
- 确保在不同部署环境下 URL 正确

#### 4. URLSearchParams 使用
```javascript
const params = new URLSearchParams();
params.append('username', username);
params.append('password', password);
```
- 构建 `application/x-www-form-urlencoded` 格式的请求体
- 自动处理 URL 编码

## 数据存储

### ServletContext 存储结构

**用户数据：**
- Key: `registeredUsers`
- Type: `Map<String, User>`
- 线程安全：`Collections.synchronizedMap()`

**消息数据：**
- Key: `messages`
- Type: `List<String>`
- 线程安全：`Collections.synchronizedList()`

### Session 存储
- Key: `loggedInUsername`
- Type: `String`
- 存储当前登录用户的用户名

## 安全特性

### 输入验证
- 前后端双重验证
- 正则表达式验证用户名格式
- 长度限制防止过长输入

### XSS 防护
- HTML 转义输出内容
- JSON 特殊字符转义

### 会话管理
- 使用 HttpSession 进行身份验证
- 页面访问前检查登录状态
- 未登录自动重定向

## 用户体验特性

### 实时性
- 自动刷新消息（2秒轮询）
- 发送消息后立即刷新
- 自动加载滑动到最新消息

### 访问方式
1. 访问应用根路径，自动跳转到登录页面
2. 输入用户名和密码登录（首次自动注册）
3. 登录成功后进入聊天室页面
4. 发送消息并实时查看聊天内容

## 限制与约束

### 功能限制
- 用户名最多 100 个字符
- 密码 6-50 个字符
- 单条消息最多 100 个字符
- 用户名只能包含中英文、数字和下划线

### 技术限制
- 数据存储在内存中（ServletContext）
- 服务器重启后数据丢失
- 使用轮询方式刷新消息（非 WebSocket）
- 不支持私聊和群组功能
