package io.dkakunsi.money.account.process;

import io.dkakunsi.common.Process;
import io.dkakunsi.common.ProcessInput;
import io.dkakunsi.common.ProcessResult;
import io.dkakunsi.money.account.model.Account;
import io.dkakunsi.money.account.repository.AccountRepository;

public final class UpsertAccountProcess implements Process<UpsertAccountInput, Account> {

  private final AccountRepository accountRepository;

  public UpsertAccountProcess(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public ProcessResult<Account> process(ProcessInput<UpsertAccountInput> input) {
    final var account = input.getData().toModel();
    if (!account.hasId()) {
      account.initializeNewAccount(input.getContext().getRequester());
    }
    var result = this.accountRepository.upsert(account);
    return ProcessResult.success(result);
  }
}
