package com.starmuseum.modules.stats.mapper;

import com.starmuseum.modules.stats.vo.MethodCountVO;
import com.starmuseum.modules.stats.vo.TargetHotItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 阶段5C：统计聚合查询
 *
 * 数据来源：
 * - observation_log：观测次数/成功/评分/方式分布/发布动态数（published=1）
 * - observation_log_target：目标 TopN（个人/社区）
 *
 * 注意：所有查询默认排除 deleted_at 不为空的日志
 */
@Mapper
public interface StatsMapper {

    // ===== 我的统计：基础计数 =====

    @Select("""
        SELECT COUNT(1)
        FROM observation_log
        WHERE user_id = #{userId}
          AND deleted_at IS NULL
          AND observed_at >= #{from}
          AND observed_at < #{to}
        """)
    Long countMyLogs(@Param("userId") Long userId,
                     @Param("from") LocalDateTime from,
                     @Param("to") LocalDateTime to);

    @Select("""
        SELECT COUNT(1)
        FROM observation_log
        WHERE user_id = #{userId}
          AND deleted_at IS NULL
          AND published = 1
          AND observed_at >= #{from}
          AND observed_at < #{to}
        """)
    Long countMyPublishedLogs(@Param("userId") Long userId,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);

    @Select("""
        SELECT COUNT(1)
        FROM observation_log
        WHERE user_id = #{userId}
          AND deleted_at IS NULL
          AND success = 1
          AND observed_at >= #{from}
          AND observed_at < #{to}
        """)
    Long countMySuccessLogs(@Param("userId") Long userId,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    @Select("""
        SELECT AVG(rating)
        FROM observation_log
        WHERE user_id = #{userId}
          AND deleted_at IS NULL
          AND rating IS NOT NULL
          AND observed_at >= #{from}
          AND observed_at < #{to}
        """)
    Double avgMyRating(@Param("userId") Long userId,
                       @Param("from") LocalDateTime from,
                       @Param("to") LocalDateTime to);

    // ===== 我的统计：方式分布 =====
    @Select("""
        SELECT method AS method, COUNT(1) AS cnt
        FROM observation_log
        WHERE user_id = #{userId}
          AND deleted_at IS NULL
          AND observed_at >= #{from}
          AND observed_at < #{to}
        GROUP BY method
        ORDER BY cnt DESC
        """)
    List<MethodCountVO> myMethodDistribution(@Param("userId") Long userId,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    // ===== 我的统计：Top 目标 =====
    @Select("""
        SELECT
          t.target_type AS targetType,
          t.target_id AS targetId,
          t.target_name AS targetName,
          t.body_type AS bodyType,
          COUNT(1) AS cnt
        FROM observation_log_target t
        INNER JOIN observation_log l
          ON l.id = t.log_id
         AND l.user_id = t.user_id
        WHERE t.user_id = #{userId}
          AND l.deleted_at IS NULL
          AND l.observed_at >= #{from}
          AND l.observed_at < #{to}
        GROUP BY t.target_type, t.target_id, t.target_name, t.body_type
        ORDER BY cnt DESC
        LIMIT #{limit}
        """)
    List<TargetHotItemVO> myTopTargets(@Param("userId") Long userId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to,
                                       @Param("limit") int limit);

    // ===== 社区热门：Top 目标 =====
    @Select("""
        SELECT
          t.target_type AS targetType,
          t.target_id AS targetId,
          t.target_name AS targetName,
          t.body_type AS bodyType,
          COUNT(1) AS cnt
        FROM observation_log_target t
        INNER JOIN observation_log l
          ON l.id = t.log_id
         AND l.user_id = t.user_id
        WHERE l.deleted_at IS NULL
          AND l.observed_at >= #{from}
          AND l.observed_at < #{to}
        GROUP BY t.target_type, t.target_id, t.target_name, t.body_type
        ORDER BY cnt DESC
        LIMIT #{limit}
        """)
    List<TargetHotItemVO> hotTargets(@Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to,
                                     @Param("limit") int limit);
}
