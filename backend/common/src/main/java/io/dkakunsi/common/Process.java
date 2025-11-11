package io.dkakunsi.common;

public interface Process<IN, OUT> {
  ProcessResult<OUT> process(IN input);
}
