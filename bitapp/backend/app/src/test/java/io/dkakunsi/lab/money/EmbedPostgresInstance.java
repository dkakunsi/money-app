package io.dkakunsi.lab.money;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

class EmbedPostgresInstance {

  private javax.sql.DataSource pgDatabase;

  private volatile EmbeddedPostgres epg;

  private volatile Connection postgresConnection;

  public void start(int port) throws Exception {
    epg = pg(port);
    pgDatabase = epg.getPostgresDatabase();
    postgresConnection = pgDatabase.getConnection();
  }

  private EmbeddedPostgres pg(int port) throws IOException {
    final EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder().setPort(port);
    return builder.start();
  }

  public void stop() throws Exception {
    try {
      postgresConnection.close();
    } catch (SQLException e) {
      throw new AssertionError(e);
    }
    try {
      epg.close();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public javax.sql.DataSource getDataSource() {
    return pgDatabase;
  }
}