package com.jd.wego.service;

import com.jd.wego.entity.Message;

import java.util.List;

/**
 * @author hbquan
 * @date 2021/5/20 16:54
 */
public interface MessageService {

    void insertMessage(Message message);

    List<Message> selectMessageByConversationId(String conversationId);

    List<Message> selectAllMessagesByUserId(String userId);

    void markAsRead(String userId);

    int countUnread(String userId);

    List<String> selectConversationIds(String userId);

    Message selectLastMessage(String userId);
}
