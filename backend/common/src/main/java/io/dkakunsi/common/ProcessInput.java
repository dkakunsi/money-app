package io.dkakunsi.common;

import jakarta.validation.constraints.NotNull;

public final record ProcessInput<DATA>(@NotNull DATA data, @NotNull Context context) {
  public String requester() {
    return context.requester();
  }
}
