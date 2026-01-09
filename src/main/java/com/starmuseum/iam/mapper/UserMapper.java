package com.starmuseum.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starmuseum.iam.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
