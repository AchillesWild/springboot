package com.achilles.wild.server.business.dao.account;

import com.achilles.wild.server.entity.account.AccountTransactionFlowInterAdd;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AccountTransactionFlowInterAddDao {

    int insertSelective(AccountTransactionFlowInterAdd record);
}