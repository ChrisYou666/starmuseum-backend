package com.starmuseum.modules.follow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starmuseum.modules.follow.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    @Select("""
        SELECT followee_id
        FROM user_follow
        WHERE follower_id = #{followerId}
        ORDER BY created_at DESC
    """)
    List<Long> selectFolloweeIds(@Param("followerId") Long followerId);

    @Select("""
        SELECT follower_id
        FROM user_follow
        WHERE followee_id = #{followeeId}
        ORDER BY created_at DESC
    """)
    List<Long> selectFollowerIds(@Param("followeeId") Long followeeId);
}
