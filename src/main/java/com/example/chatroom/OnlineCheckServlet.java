package com.example.chatroom;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户检测Servlet - 检测超时用户并移除
 */
@WebServlet(name = "onlineCheckServlet", value = "/api/onlineCheck")
public class OnlineCheckServlet extends HttpServlet {

    // 超时时间：10秒（心跳间隔5秒，检测间隔10秒）
    private static final long TIMEOUT_MS = 10000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        ServletContext context = getServletContext();
        ConcurrentHashMap<String, Long> onlineUsers = OnlineUserManager.getOnlineUsers(context);

        PrintWriter out = response.getWriter();

        if (onlineUsers == null || onlineUsers.isEmpty()) {
            out.print("{\"checked\":0,\"removed\":[]}");
            return;
        }

        long currentTime = System.currentTimeMillis();
        List<String> removedUsers = new ArrayList<>();

        // 检测超时用户
        for (Map.Entry<String, Long> entry : onlineUsers.entrySet()) {
            String username = entry.getKey();
            Long lastHeartbeat = entry.getValue();

            if (currentTime - lastHeartbeat > TIMEOUT_MS) {
                // 用户超时，执行登出操作
                OnlineUserManager.userLogout(context, username);
                removedUsers.add(username);
            }
        }

        // 返回检测结果
        out.print("{\"checked\":" + onlineUsers.size() + ",\"removed\":[");
        for (int i = 0; i < removedUsers.size(); i++) {
            out.print("\"" + escapeJson(removedUsers.get(i)) + "\"");
            if (i < removedUsers.size() - 1) {
                out.print(",");
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

