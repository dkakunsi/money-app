package io.dkakunsi.common.process;

import io.dkakunsi.common.Context;
import jakarta.validation.constraints.NotNull;

public final record ProcessInput<DATA>(@NotNull DATA data, @NotNull Context context) {
  public String requester() {
    return context.requester();
  }
}
