package io.dkakunsi.lab.postgres;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.common.data.Entity;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TestObject implements Entity {
  private Id id;
  private String code;
  private String name;
}
