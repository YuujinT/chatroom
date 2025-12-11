package com.example.chatroom;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户列表API - 返回当前在线用户列表（用于私聊选择）
 */
@WebServlet(name = "onlineUsersApiServlet", value = "/api/onlineUsers")
public class OnlineUsersApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        ConcurrentHashMap<String, Long> onlineUsers =
            OnlineUserManager.getOnlineUsers(getServletContext());

        PrintWriter out = response.getWriter();
        out.print("{\"users\":[");

        if (onlineUsers != null && !onlineUsers.isEmpty()) {
            int i = 0;
            for (String username : onlineUsers.keySet()) {
                if (i > 0) {
                    out.print(",");
                }
                out.print("\"" + escapeJson(username) + "\"");
                i++;
            }
        }

        out.print("]}");
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

