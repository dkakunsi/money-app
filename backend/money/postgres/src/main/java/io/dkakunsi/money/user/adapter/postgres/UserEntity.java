package io.dkakunsi.money.user.adapter.postgres;

import org.json.JSONObject;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.common.data.Entity;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.model.User.Language;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserEntity implements Entity {

  private Id id;
  private String name;
  private Language language;
  private String phone;
  private String photoUrl;

  @Override
  public Id getId() {
    return id;
  }

  @Override
  public String toData() {
    var json = new JSONObject();
    json.put("id", this.getId().value());
    json.put("name", this.getName());
    json.put("language", this.getLanguage());
    json.put("phone", this.getPhone());
    json.put("photoUrl", this.getPhotoUrl());
    return json.toString();
  }

  public User toUser() {
    return User.builder()
        .id(this.id)
        .name(this.name)
        .language(this.language)
        .phone(this.phone)
        .photoUrl(this.photoUrl)
        .build();
  }

  public static UserEntity from(String data) {
    var json = new JSONObject(data);
    return UserEntity.builder()
        .id(Id.of(json.getString("id")))
        .name(json.getString("name"))
        .language(User.Language.valueOf(json.getString("language")))
        .phone(json.optString("phone", null))
        .photoUrl(json.optString("photoUrl", null))
        .build();
  }
}
