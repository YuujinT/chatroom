package com.example.chatroom;

import java.io.*;
import java.util.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

/**
 * 聊天室Servlet - 处理消息发送（支持公聊和私聊）
 */
@WebServlet(name = "chatServlet", value = "/ChatServlet")
public class ChatServlet extends HttpServlet {

    private static final String MESSAGES_KEY = "messages";

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
        String target = request.getParameter("target"); // 接收者：ALL 或 具体用户名

        // 服务器端验证
        if (message == null || message.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("请输入内容！");
            return;
        }

        message = message.trim();

        // 如果没有指定target，默认为ALL
        if (target == null || target.trim().isEmpty()) {
            target = "ALL";
        } else {
            target = target.trim();
        }

        ServletContext context = getServletContext();

        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) context.getAttribute(MESSAGES_KEY);

        if (messages == null) {
            messages = Collections.synchronizedList(new ArrayList<>());
            context.setAttribute(MESSAGES_KEY, messages);
        }

        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

        // 根据target构建不同格式的消息
        String formattedMessage;
        if ("ALL".equals(target)) {
            // 公聊格式: [时间] 用户名: 消息
            formattedMessage = "[" + timestamp + "] " + username + ": " + message;
        } else {
            // 私聊格式: [时间] 发送者 → 接收者: 消息
            formattedMessage = "[" + timestamp + "] " + username + " → " + target + ": " + message;
        }

        messages.add(formattedMessage);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("success");
    }
}
