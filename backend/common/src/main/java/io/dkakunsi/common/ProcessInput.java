package io.dkakunsi.common;

public final record ProcessInput<DATA>(DATA data, Context context) {
  public ProcessInput {
    if (data == null) {
      throw new IllegalArgumentException("Data cannot be null");
    }
    if (context == null) {
      throw new IllegalArgumentException("Context cannot be null");
    }
  }

  public String requester() {
    return context.requester();
  }
}
