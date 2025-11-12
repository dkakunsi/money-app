package io.dkakunsi.lab.database;

import java.util.Collection;
import java.util.List;

import io.dkakunsi.lab.common.DefaultLogger;
import io.dkakunsi.lab.common.Logger;
import io.dkakunsi.lab.common.data.Schema;
import io.dkakunsi.lab.common.data.Schema.Index;

public final class PostgresDatabaseInit {

  private static final Logger LOGGER = DefaultLogger.getLogger(PostgresDatabaseInit.class);

  protected PostgresDatabaseExecutor executor;

  private String databaseSchema;

  public PostgresDatabaseInit(PostgresConfig config) {
    executor = new PostgresDatabaseExecutor(config);
    databaseSchema = config.getDatabaseSchema();
  }

  public void initDatabase(Collection<Schema> schemas) {
    LOGGER.debug("Initializing database '{}'", databaseSchema);
    schemas.forEach(this::initTable);
  }

  public void initTable(Schema schema) {
    LOGGER.debug("Initializing table '{}.{}'", databaseSchema, schema.getName());
    var tableName = databaseSchema + "." + schema.getName();
    createTable(tableName);
    addIndex(tableName, schema.getIndexes());
  }

  private void createTable(String tableName) {
    LOGGER.debug("Creating table '{}'", tableName);
    var query = Ddl.createTable(tableName);
    executor.executeUpdate(query);
  }

  private void addIndex(String tableName, List<Index> indexes) {
    LOGGER.debug("Adding index for table '{}': '{}'", tableName, indexes.size());
    indexes.forEach(i -> {
      LOGGER.debug("Creating index on '{}.{}'", tableName, i.getField());
      var query = i.isUnique()
          ? Ddl.createUniqueIndex(tableName, i.getField())
          : Ddl.createIndex(tableName, i.getField());
      executor.executeUpdate(query);
    });
  }

  private static class Ddl {

    private static final String DOT = ".";

    private static final String UNDERSCORE = "_";

    private static final String INDEX_NAME = "%s_%s_index";

    private static final String CREATE_TABLE_QUERY = """
        CREATE TABLE IF NOT EXISTS %s
        (
          id VARCHAR(64) PRIMARY KEY,
          data JSONB
        )
        """;

    private static final String CREATE_INDEX_QUERY = """
        CREATE INDEX IF NOT EXISTS %s ON %s ( %s )
        """;

    private static final String CREATE_UNIQUE_INDEX_QUERY = """
        CREATE UNIQUE INDEX IF NOT EXISTS %s ON %s ( %s )
        """;

    public static String createTable(String tableName) {
      return CREATE_TABLE_QUERY.formatted(tableName);
    }

    public static String createIndex(String tableName, String field) {
      var indexName = INDEX_NAME.formatted(tableName, field).replace(DOT, UNDERSCORE);
      var indexDefinition = isNestedField(field) ? getNestedJsonIndex(field) : getFlatJsonIndex(field);
      return CREATE_INDEX_QUERY.formatted(indexName, tableName, indexDefinition);
    }

    public static String createUniqueIndex(String tableName, String field) {
      var indexName = INDEX_NAME.formatted(tableName, field).replace(DOT, UNDERSCORE);
      var indexDefinition = isNestedField(field) ? getNestedJsonIndex(field) : getFlatJsonIndex(field);
      return CREATE_UNIQUE_INDEX_QUERY.formatted(indexName, tableName, indexDefinition);
    }

    private static boolean isNestedField(String field) {
      return field.contains(DOT);
    }

    private static String getNestedJsonIndex(String field) {
      var elements = field.split("\\.");
      var nestedIndex = new StringBuilder("(data");
      for (int i = 0; i < elements.length - 1; i++) {
        nestedIndex.append("->'%s'");
      }
      nestedIndex.append("->>'%s')");
      return nestedIndex.toString().formatted((Object[]) elements);
    }

    private static String getFlatJsonIndex(String field) {
      return "(data->>'%s')".formatted(field);
    }
  }
}
