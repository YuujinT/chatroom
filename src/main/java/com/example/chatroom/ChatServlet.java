package com.example.chatroom;

import java.io.*;
import java.util.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

/**
 * 聊天室Servlet - 处理消息发送
 */
@WebServlet(name = "chatServlet", value = "/ChatServlet")
public class ChatServlet extends HttpServlet {

    private static final String MESSAGES_KEY = "messages";
    private static final String USERNAME_KEY = "username";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        // 检查登录状态
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUsername") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("未登录，请先登录");
            return;
        }

        String username = (String) session.getAttribute("loggedInUsername");
        String message = request.getParameter("message");

        // 服务器端验证
        if (message == null || message.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("请输入内容！");
            return;
        }

        message = message.trim();

        ServletContext context = getServletContext();

        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) context.getAttribute(MESSAGES_KEY);

        if (messages == null) {
            messages = Collections.synchronizedList(new ArrayList<>());
            context.setAttribute(MESSAGES_KEY, messages);
        }

        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        messages.add("[" + timestamp + "] " + username + ": " + message);


        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("success");
    }
}
