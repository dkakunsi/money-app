package io.dkakunsi.lab.money.parser;

import java.util.Map;

import org.json.JSONObject;

import io.dkakunsi.lab.common.Context;
import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.common.process.ProcessInput;
import io.dkakunsi.lab.common.process.ProcessResult;
import io.dkakunsi.money.user.adapter.postgres.UserEntity;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.process.RegisterUserInput;

public class UserParser {
  public static UserEntity toEntity(String data) {
    var json = new JSONObject(data);
    return UserEntity.builder()
        .id(Id.of(json.getString("id")))
        .name(json.getString("name"))
        .language(User.Language.valueOf(json.getString("language")))
        .phone(json.optString("phone", null))
        .photoUrl(json.optString("photoUrl", null))
        .build();
  }

  public static String fromEntity(UserEntity entity) {
    var json = new JSONObject();
    json.put("id", entity.getId().value());
    json.put("name", entity.getName());
    json.put("language", entity.getLanguage());
    json.put("phone", entity.getPhone());
    json.put("photoUrl", entity.getPhotoUrl());
    return json.toString();
  }

  public static ProcessInput<RegisterUserInput> fromRequest(String body, Map<String, String> pathParams,
      Context context) {
    var json = new JSONObject(body);
    var userInput = RegisterUserInput.builder()
        .name(json.getString("name"))
        .email(json.getString("email"))
        .phone(json.optString("phone", null))
        .photoUrl(json.optString("photoUrl", null))
        .build();
    return new ProcessInput<>(userInput, context);
  }

  public static String toResponse(ProcessResult<User> result) {
    var user = result.data().get();
    var json = new JSONObject();
    json.put("id", user.getId().value());
    json.put("name", user.getName());
    json.put("language", user.getLanguage());
    json.put("phone", user.getPhone());
    json.put("photoUrl", user.getPhotoUrl());
    return json.toString();
  }
}
