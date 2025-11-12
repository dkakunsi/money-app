package io.dkakunsi.common;

import java.util.Optional;

public final record ProcessResult<DATA>(
    Optional<DATA> data,
    Optional<ProcessError> error) {

  public boolean isSuccess() {
    return error.isEmpty();
  }

  public static <DATA> ProcessResult<DATA> success() {
    return new ProcessResult<>(Optional.empty(), Optional.empty());
  }

  public static <DATA> ProcessResult<DATA> success(DATA data) {
    return new ProcessResult<>(Optional.of(data), Optional.empty());
  }
}
