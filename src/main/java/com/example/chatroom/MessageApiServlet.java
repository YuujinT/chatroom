package com.example.chatroom;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

/**
 * 消息API Servlet - 消息列表的展示（支持私聊消息过滤）
 */

@WebServlet(name = "messageApiServlet", value = "/api/messages")
public class MessageApiServlet extends HttpServlet {

    // 私聊消息的正则表达式: [时间] 发送者 → 接收者: 消息
    private static final Pattern PRIVATE_MSG_PATTERN =
        Pattern.compile("^\\[[^\\]]+\\]\\s+(.+?)\\s+→\\s+(.+?):\\s+.*$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // 获取消息列表
        ServletContext context = getServletContext();
        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) context.getAttribute("messages");

        // 获取当前登录用户名
        HttpSession session = request.getSession(false);
        String currentUser = "";
        if (session != null) {
            currentUser = (String) session.getAttribute("loggedInUsername");
            if (currentUser == null) currentUser = "";
        }

        // 构建JSON响应
        PrintWriter out = response.getWriter();
        out.print("{");
        out.print("\"username\":\"" + escapeJson(currentUser) + "\",");
        out.print("\"messages\":[");

        if (messages != null && !messages.isEmpty()) {
            boolean first = true;
            for (String msg : messages) {
                // 检查是否是私聊消息
                if (isPrivateMessage(msg)) {
                    // 验证当前用户是否有权查看此私聊消息
                    if (!canViewPrivateMessage(msg, currentUser)) {
                        continue; // 跳过此条消息
                    }
                }

                if (!first) {
                    out.print(",");
                }
                out.print("\"" + escapeJson(msg) + "\"");
                first = false;
            }
        }

        out.print("]}");
        out.flush();
    }

    /**
     * 判断消息是否为私聊消息
     */
    private boolean isPrivateMessage(String msg) {
        return PRIVATE_MSG_PATTERN.matcher(msg).matches();
    }

    /**
     * 验证用户是否可以查看私聊消息
     * 只有发送者或接收者可以查看
     */
    private boolean canViewPrivateMessage(String msg, String currentUser) {
        if (currentUser == null || currentUser.isEmpty()) {
            return false;
        }

        Matcher matcher = PRIVATE_MSG_PATTERN.matcher(msg);
        if (matcher.matches()) {
            String sender = matcher.group(1);
            String receiver = matcher.group(2);

            // 只有发送者或接收者可以查看
            return currentUser.equals(sender) || currentUser.equals(receiver);
        }

        return false;
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

