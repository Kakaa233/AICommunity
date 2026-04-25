package com.jd.wego.dao;

import com.jd.wego.entity.AiTopic;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiTopicDao {

    @Insert("insert into ai_topic(title, summary, score, created_at) values(#{title}, #{summary}, #{score}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiTopic topic);

    @Select("select * from ai_topic order by score desc limit 20")
    List<AiTopic> selectLatest();

    @Select("select * from ai_topic where id = #{id}")
    AiTopic selectById(int id);
}
