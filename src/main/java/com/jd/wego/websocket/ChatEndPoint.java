package com.jd.wego.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.wego.entity.Message;
import com.jd.wego.service.MessageService;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hbquan
 * @date 2021/5/20 13:36
 */
@Component
@ServerEndpoint(value = "/chat/{userId}")
public class ChatEndPoint {

    private Session session;

    private static Map<String, ChatEndPoint> onlineUsers = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(ChatEndPoint.class);

    private static int onlineCount = 0;

    private String userId = "";

    private static MessageService messageService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        ChatEndPoint.messageService = messageService;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        // 这里是将其加入到线程安全的Map中
        if (onlineUsers.containsKey(userId)) {
            onlineUsers.remove(userId);
            onlineUsers.put(userId, this);
        } else {
            onlineUsers.put(userId, this);
            logger.info(userId + "成功上线");
            addOnlineCount();
        }
    }

    @OnMessage
    /**
     * 用户之间一对一消息发送
     */
    public void onMessage(String message, Session session) {
        try {
            // 将message字符串进行反序列化
            Message messA = JSONObject.parseObject(message, Message.class);
            String toId = messA.getToId();
            String messageContent = messA.getMessageContent();
            String fromId = messA.getFromId();
            logger.info(fromId + "向" + toId + "发送消息：" + messageContent);

            // 设置消息属性并持久化到数据库
            messA.setHasRead(0);
            messA.setCreatedTime(new Date());
            // 生成 conversationId
            String conversationId;
            if (fromId.compareTo(toId) < 0) {
                conversationId = fromId + "_" + toId;
            } else {
                conversationId = toId + "_" + fromId;
            }
            messA.setConversationId(conversationId);

            // 保存到数据库
            if (messageService != null) {
                messageService.insertMessage(messA);
            }

            // 发送消息给接收方（如果在线）
            ChatEndPoint receiver = onlineUsers.get(toId);
            if (receiver != null) {
                receiver.session.getBasicRemote().sendText(JSON.toJSONString(messA));
            }

            // 发送消息给发送方（确认消息已发送）
            ChatEndPoint sender = onlineUsers.get(fromId);
            if (sender != null) {
                sender.session.getBasicRemote().sendText(JSON.toJSONString(messA));
            }

        } catch (Exception e) {
            logger.error("WebSocket消息处理异常", e);
        }
    }

    @OnClose
    public void onClose() {
        if (onlineUsers.containsKey(userId)) {
            // 移除该客户端对象
            onlineUsers.remove(userId);
            // 在线用户数减1
            subOnlineCount();
        }
    }

    public static synchronized void addOnlineCount() {
        ChatEndPoint.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        ChatEndPoint.onlineCount--;
    }

    public static synchronized int onlineCount() {
        return onlineCount;
    }
}
