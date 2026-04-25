package com.jd.wego.dao;

import com.jd.wego.entity.AiTopicItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiTopicItemDao {

    @Insert("insert into ai_topic_item(topic_id, article_id, score, created_at) values(#{topicId}, #{articleId}, #{score}, #{createdAt})")
    void insert(AiTopicItem item);

    @Select("select * from ai_topic_item where topic_id = #{topicId}")
    List<AiTopicItem> selectByTopicId(int topicId);
}
