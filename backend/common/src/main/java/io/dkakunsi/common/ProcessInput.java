package io.dkakunsi.common;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class ProcessInput<DATA> {

  private DATA data;
  private Context context;
}
