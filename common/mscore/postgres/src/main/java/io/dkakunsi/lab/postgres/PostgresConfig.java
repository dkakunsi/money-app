package io.dkakunsi.lab.postgres;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import io.dkakunsi.lab.common.Configuration;

public class PostgresConfig {

  private static final String HOST = "postgres.host";

  private static final String PORT = "postgres.port";

  private static final String DATABASE_NAME = "postgres.dbname";

  private static final String USERNAME = "postgres.username";

  private static final String PASSWORD = "postgres.password";

  private static final String SCHEMA = "postgres.schema";

  private static final String MIN_IDLE = "postgres.minIdle";

  private static final String MAX_IDLE = "postgres.maxIdle";

  private static final String DEFAULT_MINIMUM_IDLE_CONNECTION = "5";

  private static final String DEFAULT_MAXIMUM_IDLE_CONNECTION = "10";

  private static final String DEFAULT_SCHEMA = "public";

  private static final String CONNECTION_TEMPLATE = "jdbc:postgresql://%s:%s/%s";

  private Configuration configuration;

  public PostgresConfig(Configuration configuration) {
    this.configuration = configuration;
  }

  String get(String key) {
    return configuration.get(key).orElseThrow(() -> new RuntimeException("Postgres [" + key + "] is not configured"));
  }

  String get(String key, String defaultValue) {
    return configuration.get(key).orElse(defaultValue);
  }

  String getDatabaseSchema() {
    return get(SCHEMA, DEFAULT_SCHEMA);
  }

  public DataSource getDataSource() {
    var dataSource = new BasicDataSource();
    dataSource.setUrl(getUrl());
    dataSource.setUsername(get(USERNAME));
    dataSource.setPassword(get(PASSWORD));
    dataSource.setMinIdle(Integer.parseInt(get(MIN_IDLE, DEFAULT_MINIMUM_IDLE_CONNECTION)));
    dataSource.setMaxIdle(Integer.parseInt(get(MAX_IDLE, DEFAULT_MAXIMUM_IDLE_CONNECTION)));
    return dataSource;
  }

  private String getUrl() {
    var host = get(HOST);
    var port = get(PORT);
    var database = get(DATABASE_NAME);
    return String.format(CONNECTION_TEMPLATE, host, port, database);
  }
}
