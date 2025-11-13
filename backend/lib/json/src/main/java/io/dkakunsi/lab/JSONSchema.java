package io.dkakunsi.lab;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.github.erosb.jsonsKema.FormatValidationPolicy;
import com.github.erosb.jsonsKema.JsonParser;
import com.github.erosb.jsonsKema.SchemaLoader;
import com.github.erosb.jsonsKema.Validator;
import com.github.erosb.jsonsKema.ValidatorConfig;

import io.dkakunsi.lab.common.DefaultLogger;
import io.dkakunsi.lab.common.EnvironmentConfiguration;
import io.dkakunsi.lab.common.Logger;
import io.dkakunsi.lab.common.data.Schema;

public final class JSONSchema implements Schema {

  private static final Logger LOGGER = DefaultLogger.getLogger(JSONSchema.class);

  private final JSONObject content;

  private final List<Index> indexes;

  private final Map<String, Reference> references;

  private final com.github.erosb.jsonsKema.Schema validationSchema;

  private JSONSchema(String content) {
    this.content = new JSONObject(content);
    validationSchema = new SchemaLoader(content).load();
    indexes = initIndexes(this.content);
    references = initReferences(this.content);
  }

  private static List<Index> initIndexes(JSONObject schema) {
    var indexes = new ArrayList<Index>();
    var indexDefinition = schema.optJSONArray("index");
    if (indexDefinition == null) {
      return List.of();
    }

    indexDefinition.forEach(i -> {
      var json = (JSONObject) i;
      var indexField = json.getString("field");
      var isUnique = json.optBoolean("unique");
      indexes.add(new Index(indexField, isUnique));
    });
    return indexes;
  }

  private static Map<String, Reference> initReferences(JSONObject schema) {
    var references = new HashMap<String, Reference>();
    var properties = schema.getJSONObject("properties");
    properties.keySet().forEach(attributeName -> {
      var reference = properties.getJSONObject(attributeName).optJSONObject("reference");
      if (reference == null) {
        return;
      }
      if (reference.has("schema")) {
        references.put(attributeName, new Reference(attributeName, "schema", reference.getString("schema")));
      } else {
        LOGGER.info("Invalid reference. {}", reference.toString());
        throw new IllegalArgumentException("Invalid reference");
      }
    });
    return references;
  }

  @Override
  public void validate(String data) {
    var validator = Validator.create(validationSchema, new ValidatorConfig(FormatValidationPolicy.ALWAYS));
    var instance = new JsonParser(data).parse();
    var failure = validator.validate(instance);
    if (failure != null) {
      LOGGER.error("Invalid json data. {}", failure.toString());
      throw new IllegalArgumentException("Invalid json data");
    }
  }

  @Override
  public String getName() {
    return content.getString("$id");
  }

  @Override
  public List<Index> getIndexes() {
    return indexes;
  }

  @Override
  public boolean hasReference(String key) {
    return references.containsKey(key);
  }

  @Override
  public Map<String, Reference> getReferences() {
    return this.references;
  }

  @Override
  public Reference getReference(String attribute) {
    return references.get(attribute);
  }

  public static Map<String, Schema> buildSchemas() {
    var configuration = EnvironmentConfiguration.of();
    return buildSchemas(configuration);
  }

  public static Map<String, Schema> buildSchemas(EnvironmentConfiguration configuration) {
    var schemaLocation = configuration.get(LOCATION_CONFIG)
        .orElseThrow(() -> new IllegalArgumentException("Schema location not found"));
    var directory = Paths.get(schemaLocation).toFile();
    File[] files = null;
    if (!directory.exists() || !directory.isDirectory() || (files = directory.listFiles()).length <= 0) {
      throw new IllegalArgumentException("Schema directory not found");
    }

    LOGGER.debug("Found {} resources", files.length);
    return List.of(files).stream().map(JSONSchema::createSchemaObject)
        .collect(Collectors.toMap(item -> item.getName(), Function.identity()));
  }

  private static JSONSchema createSchemaObject(File file) {
    try {
      var content = Files.readString(file.toPath());
      return new JSONSchema(content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
