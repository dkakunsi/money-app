package io.dkakunsi.common;

import java.util.Optional;

public final class ProcessResult<DATA> {

  private Optional<ProcessError> error;

  private ProcessResult() {
    this.error = Optional.empty();
  }

  public boolean isSuccess() {
    return error.isEmpty();
  }

  public static <DATA> ProcessResult<DATA> success() {
    return new ProcessResult<>();
  }
}
