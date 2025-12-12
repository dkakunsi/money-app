package io.dkakunsi.lab.common.data;

import java.util.List;
import java.util.Map;

public interface Schema {

  String LOCATION_CONFIG = "schema";

  String getName();

  public List<Index> getIndexes();

  public Map<String, Reference> getReferences();

  boolean hasReference(String key);

  public Reference getReference(String attribute);

  void validate(String data);

  public static final class Index {

    private String field;

    private boolean isUnique;

    public Index(String field) {
      this.field = field;
    }

    public Index(String field, boolean isUnique) {
      this(field);
      this.isUnique = isUnique;
    }

    public String getField() {
      return field;
    }

    public boolean isUnique() {
      return isUnique;
    }
  }

  public final class Reference {

    private final String attribute;

    private final String type;

    private final String reference;

    public Reference(String attribute, String type, String reference) {
      this.attribute = attribute;
      this.type = type;
      this.reference = reference;
    }

    public String getAttribute() {
      return attribute;
    }

    public String getType() {
      return type;
    }

    public String getReference() {
      return reference;
    }
  }
}
