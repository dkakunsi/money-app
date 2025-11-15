package io.dkakunsi.lab.postgres;

import java.util.List;
import java.util.Optional;

import io.dkakunsi.lab.common.DefaultLogger;
import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.common.Logger;
import io.dkakunsi.lab.common.data.Database;
import io.dkakunsi.lab.common.data.Entity;
import io.dkakunsi.lab.common.data.Query.Criteria;
import io.dkakunsi.lab.common.data.Query.Criteria.Operator;
import io.dkakunsi.lab.common.data.ResultParser;
import io.dkakunsi.lab.common.data.Schema;
import io.dkakunsi.lab.common.data.Schema.Index;
import lombok.Builder;

/**
 * Root class of PostgreSQL repository implementation.
 *
 * @author dkakunsi
 */
public final class PostgresDatabase<T extends Entity> extends Database<T> {

  private static final Logger LOGGER = DefaultLogger.getLogger(PostgresDatabase.class);

  private PostgresQueryExecutor executor;

  private PostgresConfig config;

  private Schema schema;

  @Builder
  public PostgresDatabase(
      PostgresConfig config,
      Schema schema,
      ResultParser<T> resultParser) {
    super(resultParser);
    this.config = config;
    this.schema = schema;
    this.executor = new PostgresQueryExecutor(config);
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  private String getTableName() {
    return config.getDatabaseSchema() + "." + schema.getName();
  }

  @Override
  public PostgresDatabase<T> copy() {
    return PostgresDatabase.<T>builder()
        .config(this.config)
        .schema(this.schema)
        .resultParser(this.resultParser)
        .build();
  }

  public void initTable() {
    LOGGER.debug("Initializing table '{}'", getTableName());
    var tableName = getTableName();
    createTable(tableName);
    createIndex(tableName, schema.getIndexes());
  }

  private void createTable(String tableName) {
    LOGGER.debug("Creating table '{}'", tableName);
    var query = Ddl.createTable(tableName);
    executor.executeUpdate(query);
  }

  private void createIndex(String tableName, List<Index> indexes) {
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

  @Override
  public T save(T entity) {
    var data = entity.toData();
    schema.validate(data);
    var query = Dml.upsert(getTableName(), entity.getId(), data);
    executor.executeUpdate(query);
    return entity;
  }

  @Override
  public void delete(Id id) {
    var query = Dml.delete(getTableName(), id);
    executor.executeUpdate(query);
  }

  @Override
  public Optional<T> get(Id id) {
    var query = Dml.selectById(getTableName(), id);
    return executor.executeSingleResultQuery(query, resultParser);
  }

  @Override
  public List<T> get(String field, Object value) {
    var query = Dml.selectByField(getTableName(), field, value);
    return executor.executeListResultQuery(query, resultParser);
  }

  @Override
  public List<T> search(List<Criteria> criteria) {
    var searchQuery = Dml.selectByCriteria(getTableName(), criteria);
    return executor.executeListResultQuery(searchQuery, resultParser);
  }

  private static class Dml {

    private static final String UPSERT = """
        INSERT INTO
          %s (id, data)
          VALUES ('%s', '%s'::jsonb)
          ON CONFLICT (id) DO UPDATE
            SET data = '%s'::jsonb
        """;

    private static final String DELETE = """
        DELETE FROM %s
          WHERE id = '%s'
        """;

    private static final String SELECT_BY_ID = """
        SELECT data
          FROM %s
          WHERE id = '%s'
        """;

    private static final String SELECT_BY_FIELD = """
        SELECT data
          FROM %s
          WHERE data->>'%s' = '%s'
        """;

    private static final String SELECT_BY_CRITERIA = """
        SELECT data
          FROM %s
          WHERE %s
        """;

    public static <T extends Entity> String upsert(String tableName, Id id, String data) {
      return UPSERT.formatted(tableName, id.value(), data, data);
    }

    public static String delete(String tableName, Id id) {
      return DELETE.formatted(tableName, id.value());
    }

    public static String selectById(String tableName, Id id) {
      return SELECT_BY_ID.formatted(tableName, id.value());
    }

    public static String selectByField(String tableName, String field, Object value) {
      return SELECT_BY_FIELD.formatted(tableName, field, value);
    }

    public static String selectByCriteria(String tableName, List<Criteria> criteria) {
      var whereClause = buildWhereClauseForCriteria(criteria);
      return SELECT_BY_CRITERIA.formatted(tableName, whereClause);
    }

    private static String buildWhereClauseForCriteria(List<Criteria> criteria) {
      var whereElements = criteria.stream().map(c -> {
        var operator = c.getOperator();
        if (operator.equals(Operator.EQUALS)) {
          return "(data->>'%s' = '%s')".formatted(c.getAttribute(), c.getValue());
        } else if (operator.equals(Operator.IN)) {
          var quotedValues = String.join(",", quoteElements(c.getValues()));
          return "(data->>'%s' IN (%s))".formatted(c.getAttribute(), quotedValues);
        } else if (operator.equals(Operator.CONTAINS)) {
          return "(data->>'%s' LIKE '%%s%')".formatted(c.getAttribute(), c.getValue());
        } else if (operator.equals(Operator.LESS_THAN)) {
          return "(data->>'%s' < '%s')".formatted(c.getAttribute(), c.getValue());
        } else if (operator.equals(Operator.LESS_THAN_OR_EQUALS)) {
          return "(data->>'%s' <= '%s')".formatted(c.getAttribute(), c.getValue());
        } else if (operator.equals(Operator.GREATER_THAN)) {
          return "(data->>'%s' > '%s')".formatted(c.getAttribute(), c.getValue());
        } else if (operator.equals(Operator.GREATER_THAN_OR_EQUALS)) {
          return "(data->>'%s' >= '%s')".formatted(c.getAttribute(), c.getValue());
        } else if (operator.equals(Operator.RANGE)) {
          var values = c.getValues();
          return "(data->>'%s' BETWEEN '%s' AND '%s')".formatted(c.getAttribute(), values.get(0), values.get(1));
        } else {
          throw new RuntimeException("Operator is not supported by PostgrsRepository: " + operator);
        }
      }).toList();

      if (whereElements.size() == 1) {
        return whereElements.get(0);
      } else {
        // Only support AND junction between each criteria
        return String.join(" AND ", whereElements);
      }
    }

    private static List<String> quoteElements(List<String> elements) {
      return elements.stream().map(e -> "'" + e + "'").toList();
    }
  }
}
