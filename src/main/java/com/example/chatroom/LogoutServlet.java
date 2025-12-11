package com.example.chatroom;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 登出Servlet - 处理用户手动退出登录
 */
@WebServlet(name = "logoutServlet", value = "/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            String username = (String) session.getAttribute("loggedInUsername");

            if (username != null) {
                // 从在线用户列表中移除，发送离开消息
                OnlineUserManager.userLogout(getServletContext(), username);
            }

            // 注销session
            session.invalidate();
        }

        // 重定向到登录页面
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}

