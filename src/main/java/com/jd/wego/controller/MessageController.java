package com.jd.wego.controller;

import com.jd.wego.entity.Message;
import com.jd.wego.entity.User;
import com.jd.wego.service.MessageService;
import com.jd.wego.service.UserService;
import com.jd.wego.utils.CodeMsg;
import com.jd.wego.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author hbquan
 * @date 2021/5/20 16:54
 */
@Controller
@RequestMapping("/message")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    LoginController loginController;

    /**
     * 获取与某个用户的聊天记录
     */
    @GetMapping("/conversation")
    @ResponseBody
    public Result<Map<String, Object>> getConversation(HttpServletRequest request,
                                                  @RequestParam String otherUserId) {
        User user = loginController.getUserInfo(request);
        if (user == null) {
            return Result.error(CodeMsg.NOT_LOGIN);
        }
        String conversationId = getConversationId(user.getUserId(), otherUserId);
        List<Message> messages = messageService.selectMessageByConversationId(conversationId);

        // 标记消息为已读
        messageService.markAsRead(user.getUserId());

        // 查询对方用户的昵称
        User otherUser = userService.selectByUserId(otherUserId);
        String otherUserNickname = (otherUser != null && otherUser.getNickname() != null)
                ? otherUser.getNickname() : otherUserId;

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("otherUserNickname", otherUserNickname);

        return Result.success(result);
    }

    /**
     * 获取当前用户的所有会话列表（按对方用户去重，只显示最新消息）
     */
    @GetMapping("/conversations")
    @ResponseBody
    public Result<List<Map<String, Object>>> getConversations(HttpServletRequest request) {
        User user = loginController.getUserInfo(request);
        if (user == null) {
            return Result.error(CodeMsg.NOT_LOGIN);
        }

        // 获取当前用户参与的所有消息，按时间降序
        List<Message> allMessages = messageService.selectAllMessagesByUserId(user.getUserId());
        // 用 LinkedHashMap 按对方用户 ID 去重，保留首次出现（即最新消息）
        Map<String, Message> latestByOtherUser = new LinkedHashMap<>();

        for (Message msg : allMessages) {
            // 确定对方用户 ID
            String otherUserId;
            if (msg.getFromId().equals(user.getUserId())) {
                otherUserId = msg.getToId();
            } else {
                otherUserId = msg.getFromId();
            }

            // 如果这个对方的会话还没记录，则保存（因为消息已按 message_id desc 排序，第一条就是最新的）
            if (!latestByOtherUser.containsKey(otherUserId)) {
                latestByOtherUser.put(otherUserId, msg);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Message> entry : latestByOtherUser.entrySet()) {
            Message msg = entry.getValue();
            Map<String, Object> item = new HashMap<>();
            String otherUserId = entry.getKey();
            item.put("otherUserId", otherUserId);
            item.put("lastMessage", msg.getMessageContent());
            item.put("lastTime", msg.getCreatedTime());
            item.put("conversationId", msg.getConversationId());

            // 查询对方用户的昵称
            User otherUser = userService.selectByUserId(otherUserId);
            if (otherUser != null && otherUser.getNickname() != null) {
                item.put("otherUserNickname", otherUser.getNickname());
            } else {
                item.put("otherUserNickname", otherUserId);
            }
            result.add(item);
        }

        return Result.success(result);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread/count")
    @ResponseBody
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        User user = loginController.getUserInfo(request);
        if (user == null) {
            return Result.success(0);
        }
        return Result.success(messageService.countUnread(user.getUserId()));
    }

    /**
     * 通过 HTTP 发送消息（WebSocket 降级方案）
     */
    @PostMapping("/send")
    @ResponseBody
    public Result<Message> sendMessage(HttpServletRequest request, @RequestBody Message message) {
        User user = loginController.getUserInfo(request);
        if (user == null) {
            return Result.error(CodeMsg.NOT_LOGIN);
        }

        // 设置消息属性
        message.setHasRead(0);
        message.setCreatedTime(new Date());
        String conversationId = getConversationId(message.getFromId(), message.getToId());
        message.setConversationId(conversationId);

        // 保存到数据库
        messageService.insertMessage(message);

        return Result.success(message);
    }

    /**
     * 生成 conversationId（格式：较小userId_较大userId）
     */
    public static String getConversationId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }
}
