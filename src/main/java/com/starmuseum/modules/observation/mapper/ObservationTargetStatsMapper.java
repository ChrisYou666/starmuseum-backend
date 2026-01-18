package com.starmuseum.modules.observation.mapper;

import com.starmuseum.modules.observation.vo.TargetCountRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ObservationTargetStatsMapper {

    /**
     * 个人偏好：某用户在一段时间内观测过哪些天体（按次数排序）
     * 仅统计 target_type = CELESTIAL_BODY
     */
    @Select("""
        SELECT
            t.target_id AS targetId,
            MAX(t.target_name) AS targetName,
            MAX(t.body_type) AS bodyType,
            COUNT(1) AS cnt
        FROM observation_log_target t
        JOIN observation_log l ON l.id = t.log_id
        WHERE l.user_id = #{userId}
          AND l.deleted_at IS NULL
          AND t.target_type = 'CELESTIAL_BODY'
          AND l.observed_at >= #{from}
          AND l.observed_at <  #{to}
        GROUP BY t.target_id
        ORDER BY cnt DESC
        LIMIT #{limit}
    """)
    List<TargetCountRow> selectUserTopTargets(@Param("userId") Long userId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to,
                                              @Param("limit") int limit);

    /**
     * 社区热门：一段时间内大家都在观测哪些天体（按次数排序）
     */
    @Select("""
        SELECT
            t.target_id AS targetId,
            MAX(t.target_name) AS targetName,
            MAX(t.body_type) AS bodyType,
            COUNT(1) AS cnt
        FROM observation_log_target t
        JOIN observation_log l ON l.id = t.log_id
        WHERE l.deleted_at IS NULL
          AND t.target_type = 'CELESTIAL_BODY'
          AND l.observed_at >= #{from}
          AND l.observed_at <  #{to}
        GROUP BY t.target_id
        ORDER BY cnt DESC
        LIMIT #{limit}
    """)
    List<TargetCountRow> selectCommunityTopTargets(@Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to,
                                                   @Param("limit") int limit);
}
