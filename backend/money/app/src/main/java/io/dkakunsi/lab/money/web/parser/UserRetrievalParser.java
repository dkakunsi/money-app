package io.dkakunsi.lab.money.web.parser;

import java.util.Map;

import io.dkakunsi.lab.common.Context;
import io.dkakunsi.lab.common.process.ProcessInput;
import io.dkakunsi.lab.common.process.ProcessResult;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.process.UserRetrievalInput;

public class UserRetrievalParser {
  public static ProcessInput<UserRetrievalInput> parseRequest(String body, Map<String, String> pathParams,
      Context context) {
    var input = UserRetrievalInput.builder()
        .email(pathParams.get("email"))
        .build();
    return new ProcessInput<>(input, context);
  }

  public static String parseResponse(ProcessResult<User> result) {
    return UserRegistrationParser.parseResponse(result);
  }
}
