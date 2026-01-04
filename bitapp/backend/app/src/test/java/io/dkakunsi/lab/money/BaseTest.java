package io.dkakunsi.lab.money;

import java.util.HashMap;
import java.util.Map;

import io.dkakunsi.lab.test.Postgres;

public abstract class BaseTest {
  static final String SCHEMA = "schema";
  static final String APP_PORT = "app.port";

  private Launcher launcher;

  protected void create() throws Exception {
    Postgres.startDb();
  }

  protected void destroy() throws Exception {
    stopServer();
    Postgres.stopDb();
  }

  protected void startServer(int port) throws Exception {
    while (Postgres.isNotRunning()) {
      System.out.println("Waiting for Postgres to start...");
      Thread.sleep(1000);
    }

    var postgresEnv = Postgres.getDbConfig();
    var appEnv = Map.of(
        SCHEMA, "src/test/resources/schema",
        APP_PORT, Integer.toString(port));

    var env = new HashMap<String, String>();
    env.putAll(postgresEnv);
    env.putAll(appEnv);

    launcher = new Launcher();
    launcher.launch(env::get);
  }

  protected void stopServer() {
    launcher.stop();
  }
}
