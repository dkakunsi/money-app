package io.dkakunsi.lab.postgres;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import io.dkakunsi.lab.common.data.EntityParser;

/**
 * Root class of mongo repository implementation.
 *
 * @author dkakunsi
 */
final class PostgresDatabaseExecutor {

  private PostgresConfig config;

  private DataSource dataSource;

  PostgresDatabaseExecutor(PostgresConfig config) {
    this.config = config;
    this.dataSource = config.getDataSource();
  }

  public String getTableName(String domain) {
    return config.getDatabaseSchema() + "." + domain;
  }

  void executeUpdate(String query) {
    try (
        var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      statement.executeUpdate(query);
    } catch (SQLException ex) {
      if (ex.getMessage().contains("ERROR: duplicate key value")) {
        throw new IllegalArgumentException("Key is duplicated", ex);
      }
      throw new RuntimeException(ex);
    }
  }

  <T> Optional<T> executeSingleResultQuery(String query, EntityParser<T> parser) {
    try (
        var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      var result = statement.executeQuery(query);
      if (!result.next()) {
        return Optional.empty();
      }
      var data = result.getString("data");
      return Optional.of(parser.parse(data));
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  <T> List<T> executeListResultQuery(String query, EntityParser<T> parser) {
    try (
        var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      var result = statement.executeQuery(query);
      List<T> array = new ArrayList<>();
      while (result.next()) {
        var data = result.getString("data");
        array.add(parser.parse(data));
      }
      return array;
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
