package com.achilles.wild.server.business.dao;

import com.achilles.wild.server.entity.TimeLogs;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimeLogsDao {

    int insertSelective(TimeLogs timeLogs);
}
