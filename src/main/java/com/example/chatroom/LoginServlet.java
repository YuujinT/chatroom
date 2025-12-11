package com.example.chatroom;

import java.io.*;
import java.util.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

/**
 * 登录Servlet - 处理用户登录和自动注册
 */
@WebServlet(name = "loginServlet", value = "/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final String USERS_KEY = "registeredUsers";
    private static final String LOGGED_IN_USERNAME = "loggedInUsername";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("用户名不能为空");
            return;
        }

        // 验证密码
        if (password == null || password.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("密码不能为空");
            return;
        }

        username = username.trim();
        password = password.trim();

        // 验证用户名格式（只允许中英文、数字、下划线）
        if (!username.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("用户名不能包含特殊符号，只能使用中英文、数字和下划线");
            return;
        }

        // 验证用户名长度
        if (username.length() > 100) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("用户名长度不能超过100个字符");
            return;
        }

        // 验证密码长度
        if (password.length() < 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("密码长度不能少于6个字符");
            return;
        }

        if (password.length() > 50) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("密码长度不能超过50个字符");
            return;
        }

        ServletContext context = getServletContext();

        // 获取或创建用户Map
        @SuppressWarnings("unchecked")
        Map<String, User> users = (Map<String, User>) context.getAttribute(USERS_KEY);
        if (users == null) {
            users = Collections.synchronizedMap(new HashMap<>());
            context.setAttribute(USERS_KEY, users);
        }

        // 检查用户是否存在
        if (users.containsKey(username)) {
            // 用户已存在，验证密码
            User user = users.get(username);
            if (!user.getPassword().equals(password)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("密码错误");
                return;
            }
        } else {
            // 用户不存在，自动注册
            User newUser = new User(username, password);
            users.put(username, newUser);
        }

        // 设置session
        HttpSession session = request.getSession(true);
        session.setAttribute(LOGGED_IN_USERNAME, username);

        // 用户登录成功，添加到在线用户列表
        OnlineUserManager.userLogin(context, username);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("success");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 处理登出
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}

