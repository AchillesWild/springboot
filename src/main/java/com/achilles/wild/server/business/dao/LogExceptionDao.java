package com.achilles.wild.server.business.dao;

import com.achilles.wild.server.entity.common.LogException;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogExceptionDao {

    int insertSelective(LogException logException);
}