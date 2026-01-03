package io.dkakunsi.lab.money;

import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;

public abstract class BaseTest {
  static final String POSTGRES_HOST = "postgres.host";
  static final String POSTGRES_PORT = "postgres.port";
  static final String POSTGRES_DBNAME = "postgres.dbname";
  static final String POSTGRES_USERNAME = "postgres.username";
  static final String POSTGRES_PASSWORD = "postgres.password";
  static final String SCHEMA = "schema";
  static final String APP_PORT = "app.port";

  protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      "postgres:16-alpine");

  protected void startDb() throws Exception {
    if (!postgres.isRunning()) {
      postgres.start();
    }
  }

  protected void stopDb() throws Exception {
    if (postgres.isRunning()) {
      postgres.stop();
    }
  }

  private Launcher launcher;

  protected void startServer(int port) throws Exception {
    while (!postgres.isRunning()) {
      System.out.println("Waiting for Postgres to start...");
      Thread.sleep(1000);
    }

    var env = Map.of(
        POSTGRES_HOST, postgres.getHost(),
        POSTGRES_PORT, postgres.getFirstMappedPort().toString(),
        POSTGRES_DBNAME, postgres.getDatabaseName(),
        POSTGRES_USERNAME, postgres.getUsername(),
        POSTGRES_PASSWORD, postgres.getPassword(),
        SCHEMA, "src/test/resources/schema",
        APP_PORT, Integer.toString(port));

    launcher = new Launcher();
    launcher.launch(env::get);
  }

  protected void stopServer() {
    launcher.stop();
  }
}
