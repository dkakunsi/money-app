package io.dkakunsi.lab.common.process;

public interface Process<IN, OUT> {
  ProcessResult<OUT> process(ProcessInput<IN> input);
}
