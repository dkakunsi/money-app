package io.dkakunsi.common.process;

public interface Process<IN, OUT> {
  ProcessResult<OUT> process(ProcessInput<IN> input);
}
