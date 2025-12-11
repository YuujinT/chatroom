<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>登录 - 简单聊天室</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .login-container {
            background-color: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
            max-width: 400px;
            width: 90%;
        }
        h1 {
            text-align: center;
            color: #333;
            margin-top: 0;
            margin-bottom: 30px;
            font-size: 28px;
        }
        .icon {
            text-align: center;
            font-size: 60px;
            margin-bottom: 20px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #555;
        }
        input[type="text"],
        input[type="password"] {
            width: 100%;
            padding: 12px;
            border: 2px solid #ddd;
            border-radius: 5px;
            font-size: 16px;
            box-sizing: border-box;
            transition: border-color 0.3s;
        }
        input[type="text"]:focus,
        input[type="password"]:focus {
            outline: none;
            border-color: #667eea;
        }
        input[type="submit"] {
            width: 100%;
            background-color: #667eea;
            color: white;
            padding: 14px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 18px;
            font-weight: bold;
            transition: background-color 0.3s;
        }
        input[type="submit"]:hover {
            background-color: #5568d3;
        }
        input[type="submit"]:disabled {
            background-color: #ccc;
            cursor: not-allowed;
        }
        .info-text {
            text-align: center;
            color: #666;
            font-size: 14px;
            margin-top: 20px;
            line-height: 1.6;
        }
        .error-message {
            background-color: #ffe6e6;
            color: #d8000c;
            padding: 10px;
            border-radius: 5px;
            margin-bottom: 15px;
            display: none;
        }
    </style>
</head>
<body>
<div class="login-container">
    <div class="icon">💬</div>
    <h1>欢迎来到聊天室</h1>

    <div id="errorMessage" class="error-message"></div>

    <form id="loginForm">
        <div class="form-group">
            <label for="username">请输入你的昵称</label>
            <input type="text" id="username" name="username"
                   required
                   placeholder="输入昵称"
                   autocomplete="username">
        </div>
        <div class="form-group">
            <label for="password">请输入密码</label>
            <input type="password" id="password" name="password"
                   required
                   placeholder="输入密码（6-50位字符）"
                   autocomplete="current-password">
        </div>
        <input type="submit" id="submitBtn" value="登录 / 注册">
    </form>

    <div class="info-text">
        💡 首次使用将自动注册新账号<br>
        用户名只能包含中英文、数字和下划线<br>
        密码长度为6-50个字符
    </div>
</div>

<script>
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const submitBtn = document.getElementById('submitBtn');
    const errorMessage = document.getElementById('errorMessage');
    const contextPath = '<%= request.getContextPath() %>';

    // 检查是否已登录
    fetch(contextPath + '/api/messages')
        .then(response => response.json())
        .then(data => {
            if (data.username && data.username !== '') {
                // 已登录，跳转到聊天室
                window.location.href = contextPath + '/chat.jsp';
            }
        })
        .catch(() => {});

    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
        setTimeout(() => {
            errorMessage.style.display = 'none';
        }, 5000);
    }

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        // 前端验证用户名
        if (username === '') {
            showError('用户名不能为空');
            return;
        }

        // 验证用户名格式
        const usernamePattern = /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/;
        if (!usernamePattern.test(username)) {
            showError('用户名不能包含特殊符号，只能使用中英文、数字和下划线');
            return;
        }

        // 验证用户名长度
        if (username.length > 100) {
            showError('用户名长度不能超过100个字符');
            return;
        }

        // 前端验证密码
        if (password === '') {
            showError('密码不能为空');
            return;
        }

        if (password.length < 6) {
            showError('密码长度不能少于6个字符');
            return;
        }

        if (password.length > 50) {
            showError('密码长度不能超过50个字符');
            return;
        }

        submitBtn.disabled = true;
        submitBtn.value = '登录中...';

        try {
            const params = new URLSearchParams();
            params.append('username', username);
            params.append('password', password);

            const response = await fetch(contextPath + '/LoginServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                },
                body: params.toString()
            });

            if (response.ok) {
                // 登录成功，跳转到聊天室
                window.location.href = contextPath + '/chat.jsp';
            } else {
                const errorText = await response.text();
                showError(errorText);
                submitBtn.disabled = false;
                submitBtn.value = '登录 / 注册';
            }

        } catch (error) {
            showError('登录失败，请重试');
            submitBtn.disabled = false;
            submitBtn.value = '登录 / 注册';
        }
    });
</script>
</body>
</html>

