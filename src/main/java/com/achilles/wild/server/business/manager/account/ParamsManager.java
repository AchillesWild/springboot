package com.achilles.wild.server.business.manager.account;

import com.achilles.wild.server.business.entity.info.Params;

import java.util.List;

public interface ParamsManager {

    List<Params> selectAll();
}