package com.achilles.wild.server.business.dao.common;

import com.achilles.wild.server.entity.common.LogExceptionInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogExceptionInfoDao {

    int insertSelective(LogExceptionInfo logExceptionInfo);
}
