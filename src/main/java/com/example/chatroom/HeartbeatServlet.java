package com.example.chatroom;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 心跳检测Servlet - 接收客户端心跳
 */
@WebServlet(name = "heartbeatServlet", value = "/api/heartbeat")
public class HeartbeatServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUsername") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"unauthorized\"}");
            return;
        }

        String username = (String) session.getAttribute("loggedInUsername");

        // 更新心跳时间
        OnlineUserManager.updateHeartbeat(getServletContext(), username);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"status\":\"ok\"}");
    }
}

