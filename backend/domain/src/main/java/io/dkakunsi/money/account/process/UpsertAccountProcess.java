package io.dkakunsi.money.account.process;

import io.dkakunsi.common.Process;
import io.dkakunsi.common.ProcessResult;
import io.dkakunsi.money.account.model.Account;

public final class UpsertAccountProcess implements Process<UpsertAccountInput, Account> {

  @Override
  public ProcessResult<Account> process(UpsertAccountInput input) {
    return ProcessResult.success();
  }
}
