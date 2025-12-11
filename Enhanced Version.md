# 网页聊天室 - Enhanced Version

## 额外功能

### 1. 用户登录与退出提示  

- 每有新的用户登录，就在 `ServletContext` 中的 `onlineUsers`（一个 `ConcurrentHashMap`）中增加一项，记录**用户名**和**登录时间**。  
- 服务器聊天室界面会自动发送一条 **“XXX 加入了聊天室”** 的信息。  
- 前端也会展示在线用户列表。  

当用户**手动退出登录**时：

- 在 `onlineUsers` 中删除该用户对象；
- 注销 session；
- 聊天室自动发送一条 **“XXX 离开了聊天室”** 的消息。

---

### 2. 用户非正常关闭窗口的处理（心跳检测）  

**问题：** 有用户不会点击退出，而是直接关闭浏览器。

**解决方案：心跳检测机制**

- 每 **5 秒**，前端 `chat.jsp` 使用 `fetch` 向 `/api/heartbeat` 发送一条 POST 请求，通知服务器更新用户最新的“存活时间”。
- 服务器的在线用户检测 Servlet 每 **10 秒** 检查一次在线用户列表。
  - 一旦发现用户超时未发送心跳，则执行与手动登出相同的处理。  
- `chat.jsp` 还会定时发送请求来更新在线用户列表显示。

---

### 3. 私聊功能实现  

- 所有聊天信息（包括私聊）统一放在 ServletContext 的 `messages` 这个 List 中。

- 私聊信息格式从原来的：  

  ```
  [time] username : message
  ```

  改为：  

  ```
  [time] userA → userB : private message
  ```

**可见性控制逻辑：**

在 `MessageApiServlet` 的 `doGET` 中：  

- Servlet 能获取当前用户的用户名；  

- 构建 JSON 响应时遍历 `messages` List：  

  - 当发现记录的格式为  

    ```
    [time] userA → userB : ...
    ```

  - 只有当 **当前用户是 userA 或 userB** 时，才将该条消息写入输出；  

  - 否则跳过该条消息，确保私聊消息不会泄露给其他用户。
