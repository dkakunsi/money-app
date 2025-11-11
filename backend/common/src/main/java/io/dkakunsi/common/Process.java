package io.dkakunsi.common;

public interface Process<IN, OUT> {
  ProcessResult<OUT> process(ProcessInput<IN> input);
}
