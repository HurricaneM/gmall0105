package com.hur.gmall.manage.mapper;

import com.hur.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    List<PmsBaseAttrInfo> selectByPmsBaseAttrIdSet(@Param("valueIdStr") String valueIdStr);
}
