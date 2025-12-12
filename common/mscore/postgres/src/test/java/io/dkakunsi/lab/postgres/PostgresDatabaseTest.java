package io.dkakunsi.lab.postgres;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.lab.common.Id;
import io.dkakunsi.lab.common.data.Query.Criteria;
import io.dkakunsi.lab.common.data.Query.Criteria.Operator;
import io.dkakunsi.lab.common.data.Schema;
import io.dkakunsi.lab.common.data.Schema.Index;

class PostgresDatabaseTest {

  private static EmbedPostgresInstance postgres;

  private PostgresDatabase<TestObject> database;

  private PostgresConfig config;

  @BeforeAll
  static void setupServer() throws Exception {
    postgres = new EmbedPostgresInstance();
    postgres.start();
  }

  @AfterAll
  static void stopServer() throws Exception {
    postgres.stop();
  }

  @BeforeEach
  void setup() {
    config = mock(PostgresConfig.class);
    when(config.getDatabaseSchema()).thenReturn("public");
    when(config.getDataSource()).thenReturn(postgres.getDataSource());

    var schema = mock(Schema.class);
    doReturn("domain").when(schema).getName();
    doReturn(List.of(new Index("id", true), new Index("code", true))).when(schema).getIndexes();

    database = PostgresDatabase.<TestObject>builder()
        .config(config)
        .schema(schema)
        .resultParser(TestObject::from)
        .build();
    database.initTable();
  }

  @AfterEach
  void tearDown() {
    new PostgresQueryExecutor(config).executeUpdate("DELETE FROM public.domain WHERE 1=1");
  }

  @Test
  void testSaveCreate() {
    TestObject entity = TestObject.builder()
        .id(Id.of("123"))
        .code("Code123")
        .name("Name123")
        .build();
    TestObject result = this.database.save(entity);

    assertNotNull(result);
    assertEquals("123", result.getId().value());
  }

  @Test
  void testSaveDuplicatedKey() {
    TestObject originalEntity = TestObject.builder()
        .id(Id.of("234"))
        .code("Code234")
        .name("Name234")
        .build();
    database.save(originalEntity);

    Optional<TestObject> optResult = this.database.get(Id.of("234"));
    assertTrue(optResult.isPresent());
    TestObject result = optResult.get();
    assertEquals("234", result.getId().value());
    assertEquals("Code234", result.getCode());
    assertEquals("Name234", result.getName());

    TestObject duplicatingEntity = TestObject.builder()
        .id(Id.of("234Duplicate"))
        .code("Code234")
        .name("Name234Updated")
        .build();
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> database.save(duplicatingEntity));
    assertThat(ex.getMessage(), is("Key is duplicated"));
  }

  @Test
  void testDelete() {
    TestObject existingEntity = TestObject.builder()
        .id(Id.of("345"))
        .code("Code345")
        .name("Name345")
        .build();
    database.save(existingEntity);
    database.delete(Id.of("345"));
    var result = this.database.get(Id.of("345"));
    assertTrue(result.isEmpty());
  }

  @Test
  void testGet() {
    TestObject existingEntity = TestObject.builder()
        .id(Id.of("456"))
        .code("Code456")
        .name("Name456")
        .build();
    TestObject result = database.save(existingEntity);
    assertEquals("456", result.getId().value());
    assertEquals("Code456", result.getCode());
    assertEquals("Name456", result.getName());

    Optional<TestObject> optResult = this.database.get(Id.of("456"));
    assertTrue(optResult.isPresent());
    result = optResult.get();
    assertEquals("456", result.getId().value());
    assertEquals("Code456", result.getCode());
    assertEquals("Name456", result.getName());
  }

  @Test
  void testFindByCode() {
    TestObject existingEntity = TestObject.builder()
        .id(Id.of("567"))
        .code("Code567")
        .name("Name567")
        .build();
    TestObject result = database.save(existingEntity);
    assertEquals("567", result.getId().value());
    assertEquals("Code567", result.getCode());
    assertEquals("Name567", result.getName());

    List<TestObject> resultArr = this.database.get("code", "Code567");
    assertFalse(resultArr.isEmpty());
    result = resultArr.get(0);
    assertEquals("567", result.getId().value());
    assertEquals("Code567", result.getCode());
    assertEquals("Name567", result.getName());
  }

  @Test
  void testFindKeysNotFound() {
    var resultOpt = this.database.get(Id.of("not-found"));
    assertTrue(resultOpt.isEmpty());
  }

  @Test
  void testFindBySingleCriteria() {
    TestObject existingEntity = TestObject.builder()
        .id(Id.of("678"))
        .code("Code678")
        .name("Name678")
        .build();
    TestObject result = database.save(existingEntity);
    assertEquals("678", result.getId().value());
    assertEquals("Code678", result.getCode());
    assertEquals("Name678", result.getName());

    List<Criteria> criteria = List.of(new Criteria("code", Operator.EQUALS, "Code678"));
    List<TestObject> resultArr = this.database.search(criteria);
    assertFalse(resultArr.isEmpty());
    result = resultArr.get(0);
    assertEquals("678", result.getId().value());
    assertEquals("Code678", result.getCode());
    assertEquals("Name678", result.getName());
  }

  @Test
  void testSearchByInCriteria() {
    TestObject entity1 = TestObject.builder()
        .id(Id.of("678In1"))
        .code("Code678In1")
        .name("Name678In1")
        .build();
    TestObject result = database.save(entity1);
    assertEquals("678In1", result.getId().value());
    assertEquals("Code678In1", result.getCode());
    assertEquals("Name678In1", result.getName());

    TestObject entity2 = TestObject.builder()
        .id(Id.of("678In2"))
        .code("Code678In2")
        .name("Name678In2")
        .build();
    result = database.save(entity2);
    assertEquals("678In2", result.getId().value());
    assertEquals("Code678In2", result.getCode());
    assertEquals("Name678In2", result.getName());

    List<Criteria> criteria = List.of(new Criteria("code", Operator.IN, "Code678In1,Code678In2"));
    List<TestObject> resultArr = this.database.search(criteria);
    assertFalse(resultArr.isEmpty());
    assertEquals(2, resultArr.size());
    result = resultArr.get(0);
    assertEquals("678In1", result.getId().value());
    assertEquals("Code678In1", result.getCode());
    assertEquals("Name678In1", result.getName());
  }

  @Test
  void testSearchByCompareCriteria() {
    TestObject entity1 = TestObject.builder()
        .id(Id.of("678Compare1"))
        .code("1")
        .name("Name678Compare1")
        .build();
    TestObject result = database.save(entity1);
    assertEquals("678Compare1", result.getId().value());
    assertEquals("1", result.getCode());
    assertEquals("Name678Compare1", result.getName());

    TestObject entity2 = TestObject.builder()
        .id(Id.of("678Compare2"))
        .code("2")
        .name("Name678Compare2")
        .build();
    result = database.save(entity2);
    assertEquals("678Compare2", result.getId().value());
    assertEquals("2", result.getCode());
    assertEquals("Name678Compare2", result.getName());

    TestObject entity3 = TestObject.builder()
        .id(Id.of("678Compare3"))
        .code("3")
        .name("Name678Compare3")
        .build();
    result = database.save(entity3);
    assertEquals("678Compare3", result.getId().value());
    assertEquals("3", result.getCode());
    assertEquals("Name678Compare3", result.getName());

    TestObject entity4 = TestObject.builder()
        .id(Id.of("678Compare4"))
        .code("4")
        .name("Name678Compare4")
        .build();
    result = database.save(entity4);
    assertEquals("678Compare4", result.getId().value());
    assertEquals("4", result.getCode());
    assertEquals("Name678Compare4", result.getName());

    List<Criteria> lessThan2 = List.of(new Criteria("code", Operator.LESS_THAN, "2"));
    List<TestObject> resultArr = this.database.search(lessThan2);
    assertFalse(resultArr.isEmpty());
    assertEquals(1, resultArr.size());

    List<Criteria> lessThanOrEquals2 = List.of(new Criteria("code", Operator.LESS_THAN_OR_EQUALS, "2"));
    resultArr = this.database.search(lessThanOrEquals2);
    assertFalse(resultArr.isEmpty());
    assertEquals(2, resultArr.size());

    List<Criteria> greaterThan3 = List.of(new Criteria("code", Operator.GREATER_THAN, "3"));
    resultArr = this.database.search(greaterThan3);
    assertFalse(resultArr.isEmpty());
    assertEquals(1, resultArr.size());

    List<Criteria> greaterThanOrEquals3 = List.of(new Criteria("code", Operator.GREATER_THAN_OR_EQUALS, "3"));
    resultArr = this.database.search(greaterThanOrEquals3);
    assertFalse(resultArr.isEmpty());
    assertEquals(2, resultArr.size());

    List<Criteria> rangeBetween2And4 = List.of(new Criteria("code", Operator.RANGE, "2,4"));
    resultArr = this.database.search(rangeBetween2And4);
    assertFalse(resultArr.isEmpty());
    assertEquals(3, resultArr.size());
  }

  @Test
  void testSearchByMultipleCriteria() {
    TestObject entity1 = TestObject.builder()
        .id(Id.of("789"))
        .code("Code789")
        .name("Name789")
        .build();
    TestObject result = database.save(entity1);
    assertEquals("789", result.getId().value());
    assertEquals("Code789", result.getCode());
    assertEquals("Name789", result.getName());

    List<Criteria> query = List.of(
        new Criteria("id", Operator.EQUALS, "789"),
        new Criteria("code", Operator.EQUALS, "Code789"));
    List<TestObject> resultArr = this.database.search(query);
    assertFalse(resultArr.isEmpty());
    result = resultArr.get(0);
    assertEquals("789", result.getId().value());
    assertEquals("Code789", result.getCode());
    assertEquals("Name789", result.getName());
  }
}
