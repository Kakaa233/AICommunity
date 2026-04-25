package com.jd.wego.dao;

import com.jd.wego.entity.AiTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiTaskDao {

    @Insert("insert into ai_task(task_type, biz_id, status, retry_count, max_retries, " +
            "payload, result, error_msg, next_run_at, created_at, updated_at) " +
            "values(#{taskType}, #{bizId}, #{status}, #{retryCount}, #{maxRetries}, " +
            "#{payload}, #{result}, #{errorMsg}, #{nextRunAt}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiTask aiTask);

    @Update("update ai_task set status = #{status}, retry_count = #{retryCount}, " +
            "result = #{result}, error_msg = #{errorMsg}, next_run_at = #{nextRunAt}, " +
            "updated_at = #{updatedAt} where id = #{id}")
    void update(AiTask aiTask);

    @Select("select * from ai_task where id = #{id}")
    AiTask selectById(int id);

    @Select("select * from ai_task where status = #{status} " +
            "and (next_run_at is null or next_run_at <= now()) " +
            "order by created_at asc limit #{limit}")
    List<AiTask> selectPendingTasks(@Param("status") String status, @Param("limit") int limit);

    @Select("select * from ai_task where task_type = #{taskType} and biz_id = #{bizId} " +
            "and status = 'pending'")
    List<AiTask> selectByBizId(@Param("taskType") String taskType, @Param("bizId") String bizId);
}
