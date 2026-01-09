package com.starmuseum.modules.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starmuseum.modules.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 点赞数 +1（原子更新）
     */
    @Update("""
        UPDATE post
        SET like_count = like_count + 1,
            updated_at = NOW()
        WHERE id = #{postId}
          AND deleted_at IS NULL
    """)
    int incLikeCount(@Param("postId") Long postId);

    /**
     * 点赞数 -1（不允许小于0，原子更新）
     */
    @Update("""
        UPDATE post
        SET like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END,
            updated_at = NOW()
        WHERE id = #{postId}
          AND deleted_at IS NULL
    """)
    int decLikeCount(@Param("postId") Long postId);
}
