package io.dkakunsi.common.process;

import java.util.Optional;

import io.dkakunsi.common.process.ProcessError.Code;

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

  public static <DATA> ProcessResult<DATA> failure(Code serverError, String message) {
    final var error = new ProcessError(serverError, message);
    return new ProcessResult<>(Optional.empty(), Optional.of(error));
  }
}
