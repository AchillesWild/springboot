package com.achilles.wild.server.business.service.account;

import com.achilles.wild.server.entity.account.Account;
import com.achilles.wild.server.model.request.account.BalanceRequest;
import com.achilles.wild.server.model.response.DataResult;

/**
 * �ո���
 */
public interface BalanceService {

    DataResult<String> consumeUserBalance(Account account, BalanceRequest request);

    DataResult<String> addInterBalance(BalanceRequest request);

    DataResult<String> consumeInterBalance(BalanceRequest request);

    Long getBalance(String userId);

}
