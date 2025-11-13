package io.dkakunsi.money.user.model;

import java.util.Objects;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.money.user.process.RegisterUserInput;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class User {

  private static final Language DEFAULT_LANGUAGE = Language.EN;

  // email as Id
  private Id id;
  private String name;
  private String phone;
  private String photoUrl;
  private Language language;

  public static enum Language {
    EN,
    ID,
  }

  public User name(String name) {
    this.name = name;
    return this;
  }

  public User phone(String phone) {
    this.phone = phone;
    return this;
  }

  public User photoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
    return this;
  }

  public boolean needUpdate(RegisterUserInput userModel) {
    return !Objects.equals(this.name, userModel.name())
        || !Objects.equals(this.phone, userModel.phone())
        || !Objects.equals(this.photoUrl, userModel.photoUrl());
  }

  public User update(RegisterUserInput userInput) {
    this.name = userInput.name();
    this.phone = userInput.phone();
    this.photoUrl = userInput.photoUrl();
    return this;
  }

  public static User createNew(RegisterUserInput userInput) {
    return User.builder()
        .id(Id.of(userInput.email()))
        .name(userInput.name())
        .phone(userInput.phone())
        .photoUrl(userInput.photoUrl())
        .language(DEFAULT_LANGUAGE)
        .build();
  }
}
