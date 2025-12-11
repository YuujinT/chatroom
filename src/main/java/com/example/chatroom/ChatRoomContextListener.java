package com.example.chatroom;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天室上下文监听器 - 初始化在线用户列表和消息列表
 */
@WebListener
public class ChatRoomContextListener implements ServletContextListener {

    public static final String ONLINE_USERS_KEY = "onlineUsers";
    public static final String MESSAGES_KEY = "messages";
    public static final String REGISTERED_USERS_KEY = "registeredUsers";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // 初始化在线用户Map (用户名 -> 登录时间)
        ConcurrentHashMap<String, Long> onlineUsers = new ConcurrentHashMap<>();
        context.setAttribute(ONLINE_USERS_KEY, onlineUsers);

        // 初始化消息列表
        List<String> messages = Collections.synchronizedList(new ArrayList<>());
        context.setAttribute(MESSAGES_KEY, messages);

        // 初始化注册用户Map
        Map<String, User> registeredUsers = Collections.synchronizedMap(new HashMap<>());
        context.setAttribute(REGISTERED_USERS_KEY, registeredUsers);

        System.out.println("聊天室初始化完成");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("聊天室关闭");
    }
}

