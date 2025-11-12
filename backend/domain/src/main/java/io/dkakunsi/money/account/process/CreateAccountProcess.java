package io.dkakunsi.money.account.process;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.dkakunsi.common.Id;
import io.dkakunsi.common.Process;
import io.dkakunsi.common.ProcessError;
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
    final var account = toModel(input.data(), input.activeUser(), input.requester());
    try {
      var result = this.accountRepository.create(account);
      return ProcessResult.success(result);
    } catch (Exception e) {
      return ProcessResult.failure(ProcessError.Code.SERVER_ERROR, e.getMessage());
    }
  }

  private static Account toModel(CreateAccountInput input, String activeUser, String requester) {
    final var user = User.builder().id(Id.of(activeUser)).build();
    final var now = LocalDateTime.now();
    final var executor = requester;
    return Account.builder()
        .id(Id.generate())
        .name(input.name())
        .type(input.type())
        .themeColor(input.themeColor())
        .user(user)
        .balance(BigDecimal.ZERO)
        .createdAt(now)
        .updatedAt(now)
        .createdBy(executor)
        .updatedBy(executor)
        .build();
  }
}
