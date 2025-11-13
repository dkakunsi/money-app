package io.dkakunsi.money.user.adapter.postgres;

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

  public User toUser() {
    return User.builder()
        .id(this.id)
        .name(this.name)
        .language(this.language)
        .phone(this.phone)
        .photoUrl(this.photoUrl)
        .build();
  }
}
