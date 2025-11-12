package io.dkakunsi.money.account.process;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.common.Id;
import io.dkakunsi.common.Process;
import io.dkakunsi.common.ProcessInput;
import io.dkakunsi.common.ProcessResult;
import io.dkakunsi.money.account.model.Account;
import io.dkakunsi.money.account.model.User;
import io.dkakunsi.money.account.port.AccountPort;

public final class CreateAccountProcess implements Process<CreateAccountInput, Account> {

  private final AccountPort accountRepository;

  public CreateAccountProcess(AccountPort accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public ProcessResult<Account> process(ProcessInput<CreateAccountInput> input) {
    final var user = User.builder().id(input.data().user()).build();
    final var now = LocalDateTime.now();
    final var executor = input.requester();
    final var account = Account.builder()
        .id(Id.generate())
        .name(input.data().name())
        .type(Account.Type.valueOf(input.data().type()))
        .themeColor(input.data().themeColor())
        .user(user)
        .balance(BigDecimal.ZERO)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
    var result = this.accountRepository.create(account);
    return ProcessResult.success(result);
  }
}
