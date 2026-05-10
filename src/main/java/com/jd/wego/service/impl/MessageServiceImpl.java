package com.jd.wego.service.impl;

import com.jd.wego.dao.MessageDao;
import com.jd.wego.entity.Message;
import com.jd.wego.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hbquan
 * @date 2021/5/20 16:54
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired(required = false)
    MessageDao messageDao;

    @Override
    public void insertMessage(Message message) {
        messageDao.insertMessage(message);
    }

    @Override
    public List<Message> selectMessageByConversationId(String conversationId) {
        return messageDao.selectMessageByConversationId(conversationId);
    }

    @Override
    public List<Message> selectAllMessagesByUserId(String userId) {
        return messageDao.selectAllMessagesByUserId(userId);
    }

    @Override
    public void markAsRead(String userId) {
        messageDao.markAsRead(userId);
    }

    @Override
    public int countUnread(String userId) {
        return messageDao.countUnread(userId);
    }

    @Override
    public List<String> selectConversationIds(String userId) {
        return messageDao.selectConversationIds(userId);
    }

    @Override
    public Message selectLastMessage(String userId) {
        return messageDao.selectLastMessage(userId);
    }
}
