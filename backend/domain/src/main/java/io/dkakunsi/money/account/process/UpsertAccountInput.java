package io.dkakunsi.money.account.process;

import io.dkakunsi.common.Id;
import io.dkakunsi.money.account.model.Account;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class UpsertAccountInput {

  private Id id;
  private String name;
  private String type;
  private String themeColor;
  private Id user;

  public Account toModel() {
    return Account.builder()
        .id(this.id)
        .name(this.name)
        .type(Account.Type.valueOf(this.type))
        .themeColor(this.themeColor)
        .build();
  }
}
