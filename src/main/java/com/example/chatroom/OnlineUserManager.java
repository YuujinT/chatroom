package com.example.chatroom;

import jakarta.servlet.ServletContext;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户管理器 - 处理用户登录、登出和心跳
 */
public class OnlineUserManager {

    private static final String ONLINE_USERS_KEY = "onlineUsers";
    private static final String MESSAGES_KEY = "messages";

    /**
     * 用户登录 - 添加到在线列表，发送加入消息
     */
    public static void userLogin(ServletContext context, String username) {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Long> onlineUsers =
            (ConcurrentHashMap<String, Long>) context.getAttribute(ONLINE_USERS_KEY);

        if (onlineUsers == null) {
            onlineUsers = new ConcurrentHashMap<>();
            context.setAttribute(ONLINE_USERS_KEY, onlineUsers);
        }

        // 检查用户是否已在线（避免重复登录消息）
        if (!onlineUsers.containsKey(username)) {
            onlineUsers.put(username, System.currentTimeMillis());

            // 发送加入消息
            addSystemMessage(context, username + " 加入了聊天室");
        } else {
            // 更新登录时间
            onlineUsers.put(username, System.currentTimeMillis());
        }
    }

    /**
     * 用户登出 - 从在线列表移除，发送离开消息
     */
    public static void userLogout(ServletContext context, String username) {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Long> onlineUsers =
            (ConcurrentHashMap<String, Long>) context.getAttribute(ONLINE_USERS_KEY);

        if (onlineUsers != null && onlineUsers.containsKey(username)) {
            onlineUsers.remove(username);

            // 发送离开消息
            addSystemMessage(context, username + " 离开了聊天室");
        }
    }

    /**
     * 更新用户心跳时间
     */
    public static void updateHeartbeat(ServletContext context, String username) {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Long> onlineUsers =
            (ConcurrentHashMap<String, Long>) context.getAttribute(ONLINE_USERS_KEY);

        if (onlineUsers != null) {
            onlineUsers.put(username, System.currentTimeMillis());
        }
    }

    /**
     * 获取在线用户列表
     */
    @SuppressWarnings("unchecked")
    public static ConcurrentHashMap<String, Long> getOnlineUsers(ServletContext context) {
        return (ConcurrentHashMap<String, Long>) context.getAttribute(ONLINE_USERS_KEY);
    }

    /**
     * 添加系统消息
     */
    private static void addSystemMessage(ServletContext context, String message) {
        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) context.getAttribute(MESSAGES_KEY);

        if (messages == null) {
            messages = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
            context.setAttribute(MESSAGES_KEY, messages);
        }

        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        messages.add("[" + timestamp + "] 🔔 " + message);
    }
}

