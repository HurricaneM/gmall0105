package com.hur.gmall.user.mapper;

import com.hur.gmall.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

//@Mapper
public interface UserMapper extends Mapper<UmsMember> {
    List<UmsMember> selectAllUsers();
}
