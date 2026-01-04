package io.dkakunsi.lab.javalin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dkakunsi.lab.common.Context;
import io.dkakunsi.lab.common.process.ProcessError;
import io.dkakunsi.lab.common.process.ProcessInput;
import io.dkakunsi.lab.common.process.ProcessResult;
import io.dkakunsi.lab.common.security.AuthorizedPrincipal;
import io.dkakunsi.lab.common.security.Authorizer;
import io.dkakunsi.lab.common.web.Endpoint.Method;
import io.dkakunsi.lab.test.TestObject;
import kong.unirest.Unirest;

class JavalinWebTest {

  private static final String BASE_URL = "http://localhost:20000";

  private static io.dkakunsi.lab.common.process.Process<TestObjectInput, TestObject> process;

  private static JavalinServer server;

  private static Authorizer authorizer;

  private static class Parser {

    static ProcessInput<TestObjectInput> parseRequest(String body, Map<String, String> pathParams,
        Context context) {
      var object = TestObjectInput.builder()
          .code("code-123")
          .name("Test Name")
          .build();
      return new ProcessInput<TestObjectInput>(object, context);
    }

    static String parseResponse(ProcessResult<TestObject> result) {
      var obj = result.data().get();
      return """
            {"code":"%s","name":"%s"}
          """.formatted(obj.getCode(), obj.getName());
    }
  }

  @SuppressWarnings("unchecked")
  @BeforeAll
  static void setup() throws Exception {
    process = (io.dkakunsi.lab.common.process.Process<TestObjectInput, TestObject>) mock(
        io.dkakunsi.lab.common.process.Process.class);
    authorizer = mock(Authorizer.class);
    var postEndpoint = JavalinEndpoint.<TestObjectInput, TestObject>builder()
        .process(process)
        .method(Method.POST)
        .path("/test")
        .requestParser(Parser::parseRequest)
        .responseParser(Parser::parseResponse)
        .build();
    var putEndpoint = JavalinEndpoint.<TestObjectInput, TestObject>builder()
        .process(process)
        .method(Method.PUT)
        .path("/test/{code}")
        .requestParser(Parser::parseRequest)
        .responseParser(Parser::parseResponse)
        .authorizer(authorizer)
        .build();

    server = JavalinServer.of(20000);
    server.addEndpoint(postEndpoint).addEndpoint(putEndpoint);
    server.start();
  }

  @AfterAll
  static void destroy() {
    server.stop();
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRequest_WhenRequested_ThenShouldOkAndReturnObject() {
    // Given
    var body = """
        {"code":"code","name":"name"}
        """;
    var result = mock(ProcessResult.class);
    when(result.isSuccess()).thenReturn(true);
    when(result.isEmpty()).thenReturn(false);
    when(result.isFailed()).thenReturn(false);
    when(result.data()).thenReturn(Optional.of(TestObject.builder()
        .code("code")
        .name("name")
        .build()));
    when(process.process(any(ProcessInput.class))).thenReturn(result);

    // When
    var response = Unirest.post(BASE_URL + "/test").body(body).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"code\":\"code\""));
    assertTrue(responseBody.contains("\"name\":\"name\""));
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRequestAndEmptyOutput_WhenRequested_ThenShouldOkWithEmptyResponse() {
    // Given
    var body = """
        {"code":"code","name":"name"}
        """;
    var output = mock(ProcessResult.class);
    when(output.isSuccess()).thenReturn(true);
    when(output.isEmpty()).thenReturn(true);
    when(output.isFailed()).thenReturn(false);
    when(process.process(any(ProcessInput.class))).thenReturn(output);

    // When
    var response = Unirest.post(BASE_URL + "/test").body(body).asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("", responseBody);
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidRequestAndProcessReturnsError_WhenRequested_ThenShouldReturnProperMessage() {
    // Given
    var body = """
        {"code":"code","name":"name"}
        """;
    var output = mock(ProcessResult.class);
    when(output.isSuccess()).thenReturn(false);
    when(output.isFailed()).thenReturn(true);
    when(output.error()).thenReturn(Optional.of(new ProcessError(ProcessError.Code.SERVER_ERROR, "Invalid input")));
    when(process.process(any(ProcessInput.class))).thenReturn(output);
    // When
    var response = Unirest.post(BASE_URL + "/test").body(body).asString();

    // Then
    assertEquals(500, response.getStatus());

    var responseBody = response.getBody();
    assertEquals("Invalid input", responseBody);
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidAndAuthorizedRequestAnd_WhenRequested_ThenShouldOk() {
    // Given
    var body = """
        {"code":"code","name":"name"}
        """;
    var output = mock(ProcessResult.class);
    when(output.isSuccess()).thenReturn(true);
    when(output.isFailed()).thenReturn(false);
    when(output.data()).thenReturn(Optional.of(TestObject.builder()
        .code("code")
        .name("name")
        .build()));
    when(process.process(any(ProcessInput.class))).thenReturn(output);
    when(authorizer.verify("JWT_Token")).thenReturn(new AuthorizedPrincipal("Requester"));

    // When
    var response = Unirest.put(BASE_URL + "/test/id")
        .header("Authorization", "JWT_Token")
        .body(body)
        .asString();

    // Then
    assertEquals(200, response.getStatus());

    var responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains("\"code\":\"code\""));
    assertTrue(responseBody.contains("\"name\":\"name\""));
  }

  @SuppressWarnings("unchecked")
  @Test
  void givenValidAndUnauthorizedRequestAnd_WhenRequested_ThenShouldReturnUnauthorized() {
    // Given
    var body = """
        {"code":"code","name":"name"}
        """;
    var output = mock(ProcessResult.class);
    when(output.isSuccess()).thenReturn(true);
    when(output.isFailed()).thenReturn(false);
    when(output.data()).thenReturn(Optional.of(TestObject.builder()
        .code("code")
        .name("name")
        .build()));
    when(process.process(any(ProcessInput.class))).thenReturn(output);
    when(authorizer.verify("JWT_Token2")).thenThrow(new IllegalArgumentException());

    // When
    var response = Unirest.put(BASE_URL + "/test/id")
        .header("Authorization", "JWT_Token2")
        .body(body)
        .asString();

    // Then
    assertEquals(401, response.getStatus());
  }

  @Test
  void givenInvalidTargetUrl_WhenRequested_ThenShouldReturnNotFound() {
    // Given
    var body = """
        {"code":"code","name":"name"}
        """;

    // When
    var response = Unirest.delete(BASE_URL + "/test/id").body(body).asString();

    // Then
    assertEquals(404, response.getStatus());
  }
}
