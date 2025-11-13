package io.dkakunsi.lab.money;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.Unirest;

public class RegisterUserTest {

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
  public void givenValidRegisterRequest_WhenSent_ThenShouldSuccess() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(BASE_URL + "/users").body(body).asString();

    assertEquals(200, response.getStatus());

    var responseBody = new JSONObject(response.getBody());
    assertEquals("John Doe", responseBody.getString("name"));
    assertEquals("john.doe@example.com", responseBody.getString("email"));
    assertEquals("1234567890", responseBody.getString("phone"));
    assertEquals("http://example.com/photo.jpg", responseBody.getString("photoUrl"));
    assertEquals("EN", responseBody.getString("language"));
  }

  @Test
  public void givenInvalidEmailOnRegisterRequest_WhenSent_ThenShouldFailWithBadRequest() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(BASE_URL + "/users").body(body).asString();

    assertEquals(400, response.getStatus());
    assertEquals("Invalid data", response.getBody());
  }

  @Test
  public void givenValidRegisterRequest_WhenThePhoneNoIsDuplicated_ThenShouldFailWithBadRequest() {
    var body = """
        {
          "name": "John Doe",
          "email": "john.doe@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response = Unirest.post(BASE_URL + "/users").body(body).asString();

    assertEquals(200, response.getStatus());

    var responseBody = new JSONObject(response.getBody());
    assertEquals("John Doe", responseBody.getString("name"));
    assertEquals("john.doe@example.com", responseBody.getString("email"));
    assertEquals("1234567890", responseBody.getString("phone"));
    assertEquals("http://example.com/photo.jpg", responseBody.getString("photoUrl"));
    assertEquals("EN", responseBody.getString("language"));

    var body2 = """
        {
          "name": "Alicia Key",
          "email": "alice@example.com",
          "phone": "1234567890",
          "photoUrl": "http://example.com/photo.jpg"
        }
        """;
    var response2 = Unirest.post(BASE_URL + "/users").body(body2).asString();

    assertEquals(400, response2.getStatus());
    assertEquals("Key is duplicated", response2.getBody());
  }
}
