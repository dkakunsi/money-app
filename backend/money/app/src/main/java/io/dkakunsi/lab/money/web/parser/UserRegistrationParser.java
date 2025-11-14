package io.dkakunsi.lab.money.web.parser;

import java.util.Map;

import org.json.JSONObject;

import io.dkakunsi.lab.common.Context;
import io.dkakunsi.lab.common.process.ProcessInput;
import io.dkakunsi.lab.common.process.ProcessResult;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.process.RegisterUserInput;

public class UserRegistrationParser {
  public static ProcessInput<RegisterUserInput> parseRequest(String body, Map<String, String> pathParams,
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

  public static String parseResponse(ProcessResult<User> result) {
    var user = result.data().get();
    var json = new JSONObject();
    json.put("email", user.getId().value());
    json.put("name", user.getName());
    json.put("language", user.getLanguage());
    json.put("phone", user.getPhone());
    json.put("photoUrl", user.getPhotoUrl());
    return json.toString();
  }
}
