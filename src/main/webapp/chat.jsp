<%@ page contentType="text/html;charset=UTF-8" %>
<%
    // 检查登录状态
    String loggedInUsername = (String) session.getAttribute("loggedInUsername");
    if (loggedInUsername == null || loggedInUsername.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>简单聊天室</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        h1 {
            text-align: center;
            color: #333;
            margin-top: 0;
        }
        h3 {
            color: #333;
            border-bottom: 2px solid #007bff;
            padding-bottom: 5px;
        }
        .main-content {
            display: flex;
            gap: 20px;
        }
        .chat-section {
            flex: 1;
        }
        .sidebar {
            width: 200px;
        }
        .chat-box {
            border: 1px solid #ccc;
            height: 400px;
            overflow-y: auto;
            padding: 10px;
            margin-bottom: 20px;
            background-color: #fafafa;
            border-radius: 5px;
        }
        .message {
            margin-bottom: 10px;
            background-color: #fff;
            padding: 8px 12px;
            border-radius: 5px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            word-wrap: break-word;
        }
        .message.private-msg {
            background-color: #fff3cd;
            border-left: 3px solid #ffc107;
        }
        .message.system-msg {
            background-color: #e7f3ff;
            border-left: 3px solid #007bff;
            font-style: italic;
        }
        .empty-message {
            text-align: center;
            color: #999;
            padding: 50px 0;
        }
        form {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
        }
        .form-group {
            margin-bottom: 10px;
        }
        label {
            display: inline-block;
            width: 80px;
            font-weight: bold;
        }
        input[type="text"] {
            width: calc(100% - 100px);
            padding: 8px;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 14px;
        }
        select {
            width: calc(100% - 100px);
            padding: 8px;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 14px;
        }
        input[type="submit"] {
            background-color: #007bff;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
        }
        input[type="submit"]:hover {
            background-color: #0056b3;
        }
        input[type="submit"]:disabled {
            background-color: #ccc;
            cursor: not-allowed;
        }
        .current-user {
            background-color: #e7f3ff;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .current-user-info {
            font-size: 16px;
        }
        .logout-btn {
            background-color: #dc3545;
            color: white;
            padding: 8px 16px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            text-decoration: none;
            display: inline-block;
        }
        .logout-btn:hover {
            background-color: #c82333;
        }
        .loading {
            text-align: center;
            color: #666;
            padding: 20px;
        }
        .online-users-box {
            border: 1px solid #ccc;
            background-color: #fafafa;
            border-radius: 5px;
            padding: 10px;
            max-height: 400px;
            overflow-y: auto;
        }
        .online-user {
            padding: 8px 10px;
            margin-bottom: 5px;
            background-color: #fff;
            border-radius: 4px;
            font-size: 14px;
        }
        .online-user.current {
            background-color: #d4edda;
            font-weight: bold;
        }
        .online-count {
            font-size: 12px;
            color: #666;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>💬 简单聊天室</h1>

    <div class="current-user" id="currentUser">
        <div class="current-user-info">
            当前用户: <strong><%= loggedInUsername %></strong>
        </div>
        <a href="<%= request.getContextPath() %>/LogoutServlet" class="logout-btn">退出登录</a>
    </div>

    <div class="main-content">
        <div class="chat-section">
            <h3>聊天消息</h3>
            <div class="chat-box" id="chatBox">
                <div class="loading">加载中...</div>
            </div>

            <h3>发送消息</h3>
            <form id="messageForm" action="ChatServlet" method="post">
                <div class="form-group">
                    <label for="target">发送给:</label>
                    <select id="target" name="target">
                        <option value="ALL">所有人</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="message">消息:</label>
                    <input type="text" id="message" name="message"
                           required placeholder="输入你的消息...">
                </div>
                <input type="submit" id="submitBtn" value="发送">
            </form>
        </div>

        <div class="sidebar">
            <h3>在线用户</h3>
            <div class="online-users-box" id="onlineUsersBox">
                <div class="loading">加载中...</div>
            </div>
        </div>
    </div>
</div>

<script>
    const chatBox = document.getElementById('chatBox');
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('message');
    const targetSelect = document.getElementById('target');
    const submitBtn = document.getElementById('submitBtn');
    const onlineUsersBox = document.getElementById('onlineUsersBox');

    // 获取应用上下文路径
    const contextPath = '<%= request.getContextPath() %>';
    const currentUsername = '<%= loggedInUsername %>';

    // HTML 转义函数
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 获取消息列表
    async function fetchMessages() {
        try {
            const response = await fetch(contextPath + '/api/messages');
            if (!response.ok) {
                throw new Error('获取消息失败');
            }

            const data = await response.json();
            displayMessages(data.messages);

        } catch (error) {
            chatBox.innerHTML = '<div class="empty-message">加载失败，请刷新页面</div>';
        }
    }

    // 显示消息列表
    function displayMessages(messages) {
        if (!messages || messages.length === 0) {
            chatBox.innerHTML = '<div class="empty-message">暂无消息，快来发送第一条消息吧！</div>';
            return;
        }

        let html = '';
        messages.forEach(msg => {
            let msgClass = 'message';

            // 判断是否是私聊消息（包含 →）
            if (msg.includes(' → ')) {
                msgClass += ' private-msg';
            }
            // 判断是否是系统消息（包含 🔔）
            else if (msg.includes('🔔')) {
                msgClass += ' system-msg';
            }

            html += '<div class="' + msgClass + '">' + escapeHtml(msg) + '</div>';
        });

        chatBox.innerHTML = html;
        // 滚动到底部
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    // 获取在线用户列表
    async function fetchOnlineUsers() {
        try {
            const response = await fetch(contextPath + '/api/onlineUsers');
            if (!response.ok) {
                throw new Error('获取在线用户失败');
            }

            const data = await response.json();
            displayOnlineUsers(data.users);
            updateTargetSelect(data.users);

        } catch (error) {
            onlineUsersBox.innerHTML = '<div class="loading">加载失败</div>';
        }
    }

    // 显示在线用户列表
    function displayOnlineUsers(users) {
        if (!users || users.length === 0) {
            onlineUsersBox.innerHTML = '<div class="loading">暂无在线用户</div>';
            return;
        }

        let html = '<div class="online-count">当前在线: ' + users.length + ' 人</div>';
        users.forEach(user => {
            let userClass = 'online-user';
            if (user === currentUsername) {
                userClass += ' current';
            }
            html += '<div class="' + userClass + '">🟢 ' + escapeHtml(user) + '</div>';
        });

        onlineUsersBox.innerHTML = html;
    }

    // 更新私聊选择框
    function updateTargetSelect(users) {
        // 保存当前选择
        const currentValue = targetSelect.value;

        // 清空选项（保留第一个"所有人"）
        while (targetSelect.options.length > 1) {
            targetSelect.remove(1);
        }

        // 添加在线用户选项（排除自己）
        if (users) {
            users.forEach(user => {
                if (user !== currentUsername) {
                    const option = document.createElement('option');
                    option.value = user;
                    option.textContent = user + ' (私聊)';
                    targetSelect.appendChild(option);
                }
            });
        }

        // 恢复之前的选择（如果还存在）
        for (let i = 0; i < targetSelect.options.length; i++) {
            if (targetSelect.options[i].value === currentValue) {
                targetSelect.value = currentValue;
                break;
            }
        }
    }

    // 发送心跳
    async function sendHeartbeat() {
        try {
            await fetch(contextPath + '/api/heartbeat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                }
            });
        } catch (error) {
            // 心跳失败，可能是网络问题或session过期
            console.log('心跳发送失败');
        }
    }

    // 检测超时用户
    async function checkOnlineUsers() {
        try {
            await fetch(contextPath + '/api/onlineCheck');
        } catch (error) {
            // 忽略错误
        }
    }

    // 表单提交事件
    messageForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const message = messageInput.value.trim();
        const target = targetSelect.value;

        // 验证不能为空格
        if (message === '') {
            alert('消息不能为空');
            return;
        }

        // 验证长度限制
        if (message.length > 100) {
            alert('消息长度不能超过100个字符');
            return;
        }

        submitBtn.disabled = true;
        submitBtn.value = '发送中...';

        try {
            const params = new URLSearchParams();
            params.append('message', message);
            params.append('target', target);

            const response = await fetch(contextPath + '/ChatServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                },
                body: params.toString()
            });

            if (response.ok) {
                messageInput.value = '';
                await fetchMessages();
            } else {
                const errorText = await response.text();
                alert('发送失败: ' + errorText);
                // 如果是未登录错误，跳转到登录页
                if (response.status === 401) {
                    window.location.href = contextPath + '/login.jsp';
                }
            }

        } catch (error) {
            alert('发送失败，请重试');
        } finally {
            submitBtn.disabled = false;
            submitBtn.value = '发送';
        }
    });

    // 初始化：加载消息和在线用户
    fetchMessages();
    fetchOnlineUsers();
    sendHeartbeat();

    // 定期刷新消息（每2秒）
    setInterval(fetchMessages, 2000);

    // 定期刷新在线用户列表（每3秒）
    setInterval(fetchOnlineUsers, 3000);

    // 定期发送心跳（每5秒）
    setInterval(sendHeartbeat, 5000);

    // 定期检测超时用户（每10秒）
    setInterval(checkOnlineUsers, 10000);
</script>
</body>
</html>
