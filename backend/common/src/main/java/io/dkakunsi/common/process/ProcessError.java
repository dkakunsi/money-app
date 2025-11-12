package io.dkakunsi.common.process;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final record ProcessError(@NotNull Code code, @NotBlank String message) {
  public static enum Code {
    SERVER_ERROR,
  }
}
