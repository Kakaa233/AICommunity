package com.jd.wego.dao;

import com.jd.wego.entity.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hbquan
 * @date 2021/5/20 16:54
 */
@Mapper
public interface MessageDao {

    @Insert("insert into message(from_id, to_id, message_content, has_read, conversation_id, created_time) " +
            "values(#{fromId}, #{toId}, #{messageContent}, #{hasRead}, #{conversationId}, #{createdTime})")
    void insertMessage(Message message);

    @Select("select * from message where conversation_id = #{conversationId} order by message_id asc")
    List<Message> selectMessageByConversationId(@Param("conversationId") String conversationId);

    @Select("select * from message where (from_id = #{userId} or to_id = #{userId}) " +
            "and message_id > (select ifnull(max(message_id),0) from message where (from_id = #{userId} or to_id = #{userId}) " +
            "and conversation_id != (select conversation_id from message where (from_id = #{userId} or to_id = #{userId}) order by message_id desc limit 1)) " +
            "order by message_id desc")
    List<Message> selectRecentConversations(@Param("userId") String userId);

    @Select("select conversation_id from message where from_id = #{userId} or to_id = #{userId} " +
            "group by conversation_id order by max(message_id) desc")
    List<String> selectConversationIds(@Param("userId") String userId);

    @Select("select * from message where (from_id = #{userId} or to_id = #{userId}) " +
            "order by message_id desc limit 1")
    Message selectLastMessage(@Param("userId") String userId);

    @Update("update message set has_read = 1 where to_id = #{userId} and has_read = 0")
    void markAsRead(@Param("userId") String userId);

    @Select("select count(*) from message where to_id = #{userId} and has_read = 0")
    int countUnread(@Param("userId") String userId);

    @Select("select * from message where (from_id = #{userId} or to_id = #{userId}) " +
            "order by message_id desc")
    List<Message> selectAllMessagesByUserId(@Param("userId") String userId);
}
