package io.dkakunsi.common;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class Id {

  private String value;

  public static Id generate() {
    final var uuid = UUID.randomUUID().toString();
    return new Id(uuid);
  }
}
