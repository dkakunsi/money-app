package io.dkakunsi.common;

import java.util.Optional;

import lombok.Getter;

@Getter
public final class ProcessResult<DATA> {

  private Optional<DATA> data;
  private Optional<ProcessError> error;

  private ProcessResult() {
    this.error = Optional.empty();
  }

  private ProcessResult(DATA data) {
    this.data = Optional.of(data);
    this.error = Optional.empty();
  }

  public boolean isSuccess() {
    return error.isEmpty();
  }

  public static <DATA> ProcessResult<DATA> success() {
    return new ProcessResult<>();
  }

  public static <DATA> ProcessResult<DATA> success(DATA data) {
    return new ProcessResult<>(data);
  }
}
