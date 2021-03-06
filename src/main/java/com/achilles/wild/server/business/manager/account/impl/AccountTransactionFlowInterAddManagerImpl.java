package com.achilles.wild.server.business.manager.account.impl;

import com.achilles.wild.server.common.constans.AccountConstant;
import com.achilles.wild.server.business.dao.account.AccountTransactionFlowInterAddDao;
import com.achilles.wild.server.entity.account.AccountTransactionFlowInterAdd;
import com.achilles.wild.server.business.manager.account.AccountTransactionFlowInterAddManager;
import com.achilles.wild.server.tool.date.DateUtil;
import com.achilles.wild.server.tool.generate.unique.GenerateUniqueUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountTransactionFlowInterAddManagerImpl implements AccountTransactionFlowInterAddManager {

    @Autowired
    private AccountTransactionFlowInterAddDao accountTransactionFlowInterDao;

    @Override
    public boolean addFlow(AccountTransactionFlowInterAdd accountTransactionFlowInter) {

        if(accountTransactionFlowInter==null){
            return false;
        }

        //todo  lock
        Integer tradeDay = DateUtil.getIntDateFormat(DateUtil.FORMAT_YYYYMMDD,accountTransactionFlowInter.getTradeDate());
        accountTransactionFlowInter.setTradeDay(tradeDay);
        int insert = accountTransactionFlowInterDao.insertSelective(accountTransactionFlowInter);
        if(insert==0){
            return false;
        }

        return true;
    }

    @Override
    public String getFlowNo() {
        String code = AccountConstant.FLOW_INTER_ADD_PREFIX+ GenerateUniqueUtil.getUuId()+"_"+DateUtil.getCurrentStr(DateUtil.YYYY_MM_DD_HH_MM_SS_SSS)+"_"+Thread.currentThread().getId();
        return code;
    }
}
