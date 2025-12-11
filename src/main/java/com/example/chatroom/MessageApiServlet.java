package com.example.chatroom;

import java.io.*;
import java.util.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

/**
 * 消息API Servlet - 消息列表的展示
 */

@WebServlet(name = "messageApiServlet", value = "/api/messages")
public class MessageApiServlet extends HttpServlet {

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
        String username = "";
        if (session != null) {
            username = (String) session.getAttribute("loggedInUsername");
            if (username == null) username = "";
        }

        // 构建JSON响应
        PrintWriter out = response.getWriter();
        out.print("{");
        out.print("\"username\":\"" + escapeJson(username) + "\",");
        out.print("\"messages\":[");

        if (messages != null && !messages.isEmpty()) {
            for (int i = 0; i < messages.size(); i++) {
                out.print("\"" + escapeJson(messages.get(i)) + "\"");
                if (i < messages.size() - 1) {
                    out.print(",");
                }
            }
        }

        out.print("]}");
        out.flush();
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

