package io.dkakunsi.lab.money;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.github.cdimascio.dotenv.Dotenv;

public abstract class BaseTest {

  protected static final String BASE_URL = "http://localhost:20000";

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
}
