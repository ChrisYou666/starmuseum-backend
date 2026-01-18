package com.starmuseum.modules.feed.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeedMapper {

    @Select("""
        SELECT COUNT(1)
        FROM post p
        JOIN user_follow f ON f.followee_id = p.user_id
        WHERE f.follower_id = #{userId}
          AND p.deleted_at IS NULL
          AND p.visibility = 'PUBLIC'
          AND (#{invisibleSize} = 0 OR p.user_id NOT IN (${invisibleCsv}))
    """)
    long countFollowFeed(@Param("userId") Long userId,
                         @Param("invisibleSize") int invisibleSize,
                         @Param("invisibleCsv") String invisibleCsv);

    @Select("""
        SELECT p.id
        FROM post p
        JOIN user_follow f ON f.followee_id = p.user_id
        WHERE f.follower_id = #{userId}
          AND p.deleted_at IS NULL
          AND p.visibility = 'PUBLIC'
          AND (#{invisibleSize} = 0 OR p.user_id NOT IN (${invisibleCsv}))
        ORDER BY p.created_at DESC
        LIMIT #{limit} OFFSET #{offset}
    """)
    List<Long> selectFollowFeedPostIds(@Param("userId") Long userId,
                                       @Param("invisibleSize") int invisibleSize,
                                       @Param("invisibleCsv") String invisibleCsv,
                                       @Param("limit") int limit,
                                       @Param("offset") long offset);

    @Select("""
        SELECT COUNT(1)
        FROM post p
        WHERE p.deleted_at IS NULL
          AND p.visibility = 'PUBLIC'
          AND (#{invisibleSize} = 0 OR p.user_id NOT IN (${invisibleCsv}))
    """)
    long countHotFeed(@Param("invisibleSize") int invisibleSize,
                      @Param("invisibleCsv") String invisibleCsv);

    @Select("""
        SELECT p.id
        FROM post p
        WHERE p.deleted_at IS NULL
          AND p.visibility = 'PUBLIC'
          AND (#{invisibleSize} = 0 OR p.user_id NOT IN (${invisibleCsv}))
        ORDER BY
          (
            (COALESCE(p.like_count,0) * 2 + COALESCE(p.comment_count,0) * 3)
            / POW((TIMESTAMPDIFF(HOUR, p.created_at, NOW()) + 2), 1.2)
          ) DESC,
          p.created_at DESC
        LIMIT #{limit} OFFSET #{offset}
    """)
    List<Long> selectHotFeedPostIds(@Param("invisibleSize") int invisibleSize,
                                    @Param("invisibleCsv") String invisibleCsv,
                                    @Param("limit") int limit,
                                    @Param("offset") long offset);
}
