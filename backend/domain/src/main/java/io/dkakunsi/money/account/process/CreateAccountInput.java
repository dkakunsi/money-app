package io.dkakunsi.money.account.process;

import io.dkakunsi.common.Id;
import io.dkakunsi.money.account.model.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public final record CreateAccountInput(
    @NotBlank String name,
    @NotBlank String themeColor,
    @NotNull Id user,
    Account.Type type) {
}
