package io.dkakunsi.javalin.endpoint;

import java.util.Map;

import io.dkakunsi.common.Context;
import io.dkakunsi.common.process.ProcessInput;
import io.dkakunsi.common.process.ProcessResult;
import io.dkakunsi.javalin.JavalinEndpoint;

public class TestEndpoint extends JavalinEndpoint<TestObjectInput, TestObject> {

  public TestEndpoint(io.dkakunsi.common.process.Process<TestObjectInput, TestObject> process, Method method,
      String path) {
    super(process, method, path);
  }

  @Override
  protected ProcessInput<TestObjectInput> parseRequest(String body, Map<String, String> pathParams, Context context) {
    var object = TestObjectInput.builder()
        .code("code-123")
        .name("Test Name")
        .build();
    return new ProcessInput<TestObjectInput>(object, context);
  }

  @Override
  protected String parseResponse(ProcessResult<TestObject> result) {
    var obj = result.data().get();
    return """
          {"code":"%s","name":"%s"}
        """.formatted(obj.getCode(), obj.getName());
  }
}
