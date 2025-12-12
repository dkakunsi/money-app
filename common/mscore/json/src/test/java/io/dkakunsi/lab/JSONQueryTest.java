package io.dkakunsi.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.dkakunsi.lab.common.data.Query;
import io.dkakunsi.lab.common.data.Query.Criteria;
import io.dkakunsi.lab.common.data.Query.Criteria.Operator;

class JSONQueryTest {

  @Test
  void constructor_withValidSimpleCriteria_shouldParseCorrectly() {
    String json = """
        {"criteria": [{"attribute": "name", "operator": "equals", "value": "test"}]}
        """;
    JSONQuery query = JSONQuery.create(json);

    List<Criteria> criteriaList = query.getCriteria();
    assertNotNull(criteriaList);
    assertEquals(1, criteriaList.size());

    Criteria criteria = criteriaList.get(0);
    assertEquals("name", criteria.getAttribute());
    assertEquals(Operator.EQUALS, criteria.getOperator());
    assertEquals("test", criteria.getValue());
  }

  @Test
  void constructor_withMultipleCriteria_shouldParseCorrectly() {
    String json = """
        {"criteria": [
          {"attribute": "name", "operator": "contains", "value": "sub"},
          {"attribute": "age", "operator": "greaterThan", "value": "30"}
        ]}
        """;
    JSONQuery query = JSONQuery.create(json);

    List<Criteria> criteriaList = query.getCriteria();
    assertNotNull(criteriaList);
    assertEquals(2, criteriaList.size());

    Criteria criteria1 = criteriaList.get(0);
    assertEquals("name", criteria1.getAttribute());
    assertEquals(Operator.CONTAINS, criteria1.getOperator());
    assertEquals("sub", criteria1.getValue());

    Criteria criteria2 = criteriaList.get(1);
    assertEquals("age", criteria2.getAttribute());
    assertEquals(Operator.GREATER_THAN, criteria2.getOperator());
    assertEquals("30", criteria2.getValue());
  }

  @Test
  void constructor_withNoCriteriaField_shouldThrowIllegalArgumentException() {
    String json = "{}";
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> JSONQuery.create(json));
    assertEquals("No criteria", exception.getMessage());
  }

  @Test
  void constructor_withEmptyCriteriaArray_shouldThrowIllegalArgumentException() {
    String json = """
        {"criteria": []}
        """;
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> JSONQuery.create(json));
    assertEquals("No criteria", exception.getMessage());
  }

  @Test
  void constructor_withMissingAttributeInCriteria_shouldThrowJSONException() {
    String json = """
        {"criteria": [{"operator": "equals", "value": "test"}]}
        """;
    assertThrows(JSONException.class, () -> JSONQuery.create(json));
  }

  @Test
  void constructor_withMissingOperatorInCriteria_shouldThrowJSONException() {
    String json = """
        {"criteria": [{"attribute": "name", "value": "test"}]}
        """;
    assertThrows(JSONException.class, () -> JSONQuery.create(json));
  }

  @Test
  void constructor_withMissingValueInCriteria_shouldThrowJSONException() {
    String json = """
        {"criteria": [{"attribute": "name", "operator": "equals"}]}
        """;
    assertThrows(JSONException.class, () -> JSONQuery.create(json));
  }

  @Test
  void constructor_withInvalidOperator_shouldThrowIllegalArgumentException() {
    String json = """
        {"criteria": [{"attribute": "name", "operator": "invalidOp", "value": "test"}]}
        """;
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> JSONQuery.create(json));
    assertTrue(exception.getMessage().contains("Invalid operator: invalidOp"));
  }

  @Test
  void getProjection_whenNoProjectionInJson_shouldReturnEmptyProjection() {
    String json = """
        {"criteria": [{"attribute": "name", "operator": "equals", "value": "test"}]}
        """;
    JSONQuery query = JSONQuery.create(json);

    Query.Projection projection = query.getProjection();
    assertNotNull(projection);
    assertTrue(projection.getElements().isEmpty());
  }

  @Test
  void getProjection_whenEmptyProjectionInJson_shouldReturnEmptyProjection() {
    String json = """
        {"criteria": [{"attribute": "name", "operator": "equals", "value": "test"}], "projection": {}}
        """;
    JSONQuery query = JSONQuery.create(json);

    Query.Projection projection = query.getProjection();
    assertNotNull(projection);
    assertTrue(projection.getElements().isEmpty());
  }

  @Test
  void getProjection_withSimpleProjection_shouldParseCorrectly() {
    String json = """
        {"criteria": [{"attribute": "name", "operator": "equals", "value": "test"}],
         "projection": {"name": 1, "email": 1}}
        """;
    JSONQuery query = JSONQuery.create(json);

    Query.Projection projection = query.getProjection();
    assertNotNull(projection);
    assertFalse(projection.getElements().isEmpty());
    assertEquals(2, projection.getElements().size());
    assertTrue(projection.getElements().containsKey("name"));
    assertTrue(projection.getElements().containsKey("email"));
    assertNotNull(projection.getElements().get("name"));
    assertTrue(projection.getElements().get("name").getElements().isEmpty());
  }

  @Test
  void getProjection_withNestedProjection_shouldParseCorrectly() {
    String json = """
        {"criteria": [{"attribute": "name", "operator": "equals", "value": "test"}],
         "projection": {"name": 1, "address": {"street": 1, "city": 1}}}
        """;
    JSONQuery query = JSONQuery.create(json);

    Query.Projection projection = query.getProjection();
    assertNotNull(projection);
    assertEquals(2, projection.getElements().size());
    assertTrue(projection.getElements().containsKey("name"));
    assertTrue(projection.getElements().containsKey("address"));

    Query.Projection nameProjection = projection.getElements().get("name");
    assertNotNull(nameProjection);
    assertTrue(nameProjection.getElements().isEmpty());

    Query.Projection addressProjection = projection.getElements().get("address");
    assertNotNull(addressProjection);
    assertEquals(2, addressProjection.getElements().size());
    assertTrue(addressProjection.getElements().containsKey("street"));
    assertTrue(addressProjection.getElements().containsKey("city"));

    Query.Projection streetProjection = addressProjection.getElements().get("street");
    assertNotNull(streetProjection);
    assertTrue(streetProjection.getElements().isEmpty());

    Query.Projection cityProjection = addressProjection.getElements().get("city");
    assertNotNull(cityProjection);
    assertTrue(cityProjection.getElements().isEmpty());
  }
}
