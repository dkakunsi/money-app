package io.dkakunsi.javalin.endpoint;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TestObject {
  private String code;
  private String name;
}
