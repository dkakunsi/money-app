package io.dkakunsi.money.account.process;

import io.dkakunsi.common.Id;
import lombok.Builder;

@Builder
public final record CreateAccountInput(String name, String type, String themeColor, Id user) {
}
