package io.dkakunsi.lab.common.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class Query {

  protected List<Criteria> criteria;

  protected Projection projection;

  public Query(List<Criteria> criteria, Projection projection) {
    this.criteria = criteria;
    this.projection = projection;
  }

  public Query(List<Criteria> criteria) {
    this(criteria, null);
  }

  public List<Criteria> getCriteria() {
    return criteria;
  }

  public Projection getProjection() {
    return projection;
  }

  public boolean hasProjection() {
    return projection != null;
  }

  public static class Criteria {

    private String attribute;

    private Operator operator;

    private Object value;

    public Criteria(String attribute, Operator operator, Object value) {
      this.operator = operator;
      this.attribute = attribute;
      this.value = value;
    }

    public Criteria validate() {
      if (operator == null) {
        throw new IllegalArgumentException("Query operator is not specified");
      }
      if (StringUtils.isBlank(attribute)) {
        throw new IllegalArgumentException("Query attribute is not specified");
      }
      if (ObjectUtils.isEmpty(value)) {
        throw new IllegalArgumentException("Query value is not specified");
      }
      return this;
    }

    public Operator getOperator() {
      return operator;
    }

    public String getAttribute() {
      return attribute;
    }

    public Object getValue() {
      return value;
    }

    public List<String> getValues() {
      if (!(value instanceof String)) {
        return List.of();
      }
      var values = List.of(value.toString().split(","));
      if (values.size() < 2) {
        throw new IllegalArgumentException("Invalid value for operator " + operator);
      }
      return values;
    }

    public boolean isReferenceCriteria() {
      return attribute.contains(".");
    }

    public String getReferenceRootAttribute() {
      return StringUtils.substringBefore(attribute, ".");
    }

    public String getReferenceNestedAttribute() {
      return StringUtils.substringAfter(attribute, ".");
    }

    public Criteria getReferenceNestedCriteria() {
      return new Criteria(getReferenceNestedAttribute(), operator, value);
    }

    @Override
    public String toString() {
      return "Criteria [operator=" + operator + ", attribute=" + attribute + ", value=" + value + "]";
    }

    public static enum Operator {
      EQUALS("equals"),
      CONTAINS("contains"),
      IN("in"),
      RANGE("range"),
      GREATER_THAN("greaterThan"),
      GREATER_THAN_OR_EQUALS("greaterThanOrEquals"),
      LESS_THAN("lessThan"),
      LESS_THAN_OR_EQUALS("lessThanOrEquals");

      private String name;

      Operator(String name) {
        this.name = name;
      }

      @Override
      public String toString() {
        return name;
      }

      public static Operator valueOfIgnoreCase(String name) {
        for (var op : Operator.values()) {
          if (op.name.equalsIgnoreCase(name)) {
            return op;
          }
        }
        throw new IllegalArgumentException("Invalid operator: " + name);
      }
    }
  }

  public static class Projection {

    private final Map<String, Projection> elements;

    public Projection() {
      elements = new HashMap<>();
    }

    public Projection(Map<String, Projection> elements) {
      this.elements = new HashMap<>(elements);
    }

    public Projection addElement(String attribute) {
      addElement(attribute, null);
      return this;
    }

    public Projection addElement(String attribute, Projection projection) {
      elements.put(attribute, projection);
      return this;
    }

    public Map<String, Projection> getElements() {
      return elements;
    }

    public Projection getProjection(String attribute) {
      return elements.get(attribute);
    }
  }
}
