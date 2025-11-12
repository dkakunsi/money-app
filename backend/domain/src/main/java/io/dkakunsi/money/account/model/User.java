package io.dkakunsi.money.account.model;

import io.dkakunsi.common.Id;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class User {

  private Id id;
}
