package io.dkakunsi.common;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;

@Builder
public record Context(String requester) {

  private static ThreadLocal<Context> context = new InheritableThreadLocal<>();

  public static Context get() {
    return context.get();
  }

  public String requester() {
    return StringUtils.isNotEmpty(requester) ? requester : "N/A";
  }
}
