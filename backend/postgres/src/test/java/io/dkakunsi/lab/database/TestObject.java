package io.dkakunsi.lab.database;

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

  @Override
  public String toData() {
    return """
        {
          "id": "%s",
          "code": "%s",
          "name": "%s"
        }
        """.formatted(id.value(), code, name);
  }
}
