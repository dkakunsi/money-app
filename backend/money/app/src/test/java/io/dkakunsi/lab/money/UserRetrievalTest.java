package io.dkakunsi.lab.money;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.Unirest;

public class UserRetrievalTest {

  private static final String BASE_URL = "http://localhost:20000";

  private static EmbedPostgresInstance postgres;

  private static Dotenv dotenv;

  private static Launcher launcher;

  @BeforeAll
  static void setupServer() throws Exception {
    postgres = new EmbedPostgresInstance();
    postgres.start();

    dotenv = Dotenv.configure()
        .directory("src/test/resources")
        .filename(".env")
        .ignoreIfMissing()
        .load();

    launcher = new Launcher();
    launcher.launch(dotenv::get);
  }

  @AfterAll
  static void stopServer() throws Exception {
    launcher.stop();
    postgres.stop();
  }

  @Test
  public void shouldReturnUserData_WhenUserExists() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;

    var postResponse = Unirest.post(BASE_URL + "/users").body(body).asString();
    assertEquals(200, postResponse.getStatus());
    var postResponseBody = new JSONObject(postResponse.getBody());
    assertEquals("John Doe", postResponseBody.getString("name"));
    assertEquals("john.doe@example.com", postResponseBody.getString("email"));
    assertEquals("1234567890", postResponseBody.getString("phone"));
    assertEquals("http://example.com/photo.jpg", postResponseBody.getString("photoUrl"));
    assertEquals("EN", postResponseBody.getString("language"));

    var getResponse = Unirest.get(BASE_URL + "/user/john.doe@example.com").asString();
    assertEquals(200, getResponse.getStatus());
    var getResponseBody = new JSONObject(getResponse.getBody());
    assertEquals("John Doe", getResponseBody.getString("name"));
    assertEquals("john.doe@example.com", getResponseBody.getString("email"));
    assertEquals("1234567890", getResponseBody.getString("phone"));
    assertEquals("http://example.com/photo.jpg", getResponseBody.getString("photoUrl"));
    assertEquals("EN", getResponseBody.getString("language"));
  }

  @Test
  public void shouldReturnEmpty_WhenUserNotExists() {
    var getResponse = Unirest.get(BASE_URL + "/user/notexist@example.com").asString();
    assertEquals(200, getResponse.getStatus());
    assertEquals("", getResponse.getBody());
  }
}
