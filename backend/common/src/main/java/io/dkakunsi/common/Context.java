package io.dkakunsi.common;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Context {

  private static ThreadLocal<Context> context = new InheritableThreadLocal<>();

  private String requester;
}
