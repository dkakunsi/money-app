package io.dkakunsi.lab.money;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseTest {
  static final String POSTGRES_HOST = "postgres.host";
  static final String POSTGRES_PORT = "postgres.port";
  static final String POSTGRES_DBNAME = "postgres.dbname";
  static final String POSTGRES_USERNAME = "postgres.username";
  static final String POSTGRES_PASSWORD = "postgres.password";
  static final String SCHEMA = "schema";
  static final String APP_PORT = "app.port";

  static Servers getServers() {
    return new Servers();
  }

  static class Servers {

    private EmbedPostgresInstance postgres;

    private Launcher launcher;

    private final Map<String, String> env;

    public Servers() {
      env = new HashMap<>(Map.of(
          POSTGRES_HOST, "localhost",
          POSTGRES_PORT, "5432",
          POSTGRES_DBNAME, "postgres",
          POSTGRES_USERNAME, "postgres",
          POSTGRES_PASSWORD, "postgres",
          SCHEMA, "src/test/resources/schema",
          APP_PORT, "20000"));
    }

    void setEnv(String key, String value) {
      env.put(key, value);
    }

    void startDb(int port) throws Exception {
      postgres = new EmbedPostgresInstance();
      postgres.start(port);
    }

    void stopDb() throws Exception {
      postgres.stop();
    }

    void startServer() throws Exception {
      launcher = new Launcher();
      launcher.launch(env::get);
    }

    void stopServer() {
      launcher.stop();
    }

    void start() throws Exception {
      startDb(Integer.parseInt(env.get(POSTGRES_PORT)));
      startServer();
    }

    void stop() throws Exception {
      stopServer();
      stopDb();
    }

    String getBaseUrl() {
      return "http://localhost:" + env.get(APP_PORT);
    }

  }
}
