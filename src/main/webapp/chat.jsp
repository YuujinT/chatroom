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
            form {
                background-color: #f8f9fa;
                padding: 15px;
            font-family: Arial, sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 800px;
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
        .empty-message {
            text-align: center;
            color: #999;
            padding: 50px 0;
        }
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
    </style>
</head>
<body>
<div class="container">
    <h1>💬 简单聊天室</h1>

    <div class="current-user" id="currentUser">
        <div class="current-user-info">
            当前用户: <strong><%= loggedInUsername %></strong>
        </div>
        <a href="<%= request.getContextPath() %>/LoginServlet?action=logout" class="logout-btn">退出登录</a>
    </div>

    <h3>聊天消息</h3>
    <div class="chat-box" id="chatBox">
        <div class="loading">加载中...</div>
    </div>

    <h3>发送消息</h3>
    <form id="messageForm" action="ChatServlet" method="post">
        <div class="form-group">
            <label for="message">消息:</label>
            <input type="text" id="message" name="message"
                   required placeholder="输入你的消息...">
        </div>
        <input type="submit" id="submitBtn" value="发送">
    </form>
</div>

<script>
    const chatBox = document.getElementById('chatBox');
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('message');
    const submitBtn = document.getElementById('submitBtn');

    // 获取应用上下文路径
    const contextPath = '<%= request.getContextPath() %>';

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
            html += '<div class="message">' + escapeHtml(msg) + '</div>';
        });

        chatBox.innerHTML = html;
        // 滚动到底部
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    // 表单提交事件
    messageForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const message = messageInput.value.trim();

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

    // 初始化：加载消息
    fetchMessages();

    // 定期刷新消息（每3秒）
    setInterval(fetchMessages, 3000);
</script>
</body>
</html>
