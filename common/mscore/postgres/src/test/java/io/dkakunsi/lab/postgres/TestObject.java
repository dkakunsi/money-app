package io.dkakunsi.lab.postgres;

import org.json.JSONObject;

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

  public String toData() {
    var json = new JSONObject();
    json.put("id", id.value());
    json.put("code", code);
    json.put("name", name);
    return json.toString();
  }

  public static TestObject from(String data) {
    var json = new JSONObject(data);
    return TestObject.builder()
        .id(Id.of(json.getString("id")))
        .code(json.getString("code"))
        .name(json.getString("name"))
        .build();
  }
}
