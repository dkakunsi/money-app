package io.dkakunsi.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dkakunsi.lab.common.EnvironmentConfiguration;
import io.dkakunsi.lab.common.data.Entity;
import io.dkakunsi.lab.common.data.Schema;
import io.dkakunsi.lab.common.data.Schema.Reference;

public class JSONSchemaTest {

  private EnvironmentConfiguration config;

  private static String createTempSchemaDirectory(String schemaContent) throws IOException {
    Path tempDir = Files.createTempDirectory("test_schemas");
    Path schemaFile = tempDir.resolve("test_schema.json");
    Files.writeString(schemaFile, schemaContent);

    return tempDir.toString();
  }

  private static void cleanupTempSchemaDirectory(EnvironmentConfiguration config) throws IOException {
    String schemaLocation = config.get(Schema.LOCATION_CONFIG).get();
    if (schemaLocation != null) {
      Files.walk(Paths.get(schemaLocation))
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
      System.clearProperty(Schema.LOCATION_CONFIG);
    }
  }

  @BeforeEach
  void setUp() {
    config = mock(EnvironmentConfiguration.class);
  }

  @AfterEach
  void tearDown() throws IOException {
    cleanupTempSchemaDirectory(config);
  }

  @Test
  void buildSchemas_withValidSchemaFile_shouldReturnListOfSchemas() throws IOException {
    String schemaContent = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "$id": "customer",
          "name": "customer",
          "description": "The Customer Data Schema",
          "type": "object",
          "properties": {
            "id": {
              "type": "string",
              "maxLength": 36
            },
            "name": {
              "type": "string"
            }
          }
        }
        """;
    var location = createTempSchemaDirectory(schemaContent);
    when(config.get(Schema.LOCATION_CONFIG)).thenReturn(Optional.of(location));

    Map<String, Schema> schemas = JSONSchema.buildSchemas(config);
    assertNotNull(schemas);
    assertFalse(schemas.isEmpty());
    assertEquals(1, schemas.size());

    var customerSchema = schemas.get("customer");
    assertEquals("customer", customerSchema.getName());
  }

  @Test
  void buildSchemas_withValidSchemaFile_shouldReturnListOfSchemasWithIndexes() throws IOException {
    String schemaContent = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "$id": "customer",
          "name": "customer",
          "description": "The Customer Data Schema",
          "type": "object",
          "properties": {
            "id": {
              "type": "string",
              "maxLength": 36
            },
            "name": {
              "type": "string"
            },
            "index1": {
              "type": "string"
            },
            "index2": {
              "type": "string"
            }
          },
          "index": [
            {
              "field": "id",
              "unique": true
            },
            {
              "field": "index1",
              "unique": false
            },
            {
              "field": "index2"
            }
          ]
        }
        """;
    var location = createTempSchemaDirectory(schemaContent);
    when(config.get(Schema.LOCATION_CONFIG)).thenReturn(Optional.of(location));

    Map<String, Schema> schemas = JSONSchema.buildSchemas(config);
    assertNotNull(schemas);
    assertFalse(schemas.isEmpty());
    assertEquals(1, schemas.size());

    var customerSchema = schemas.get("customer");
    assertEquals("customer", customerSchema.getName());
    var customerIndexes = customerSchema.getIndexes();
    assertEquals(3, customerIndexes.size());

    assertEquals("id", customerIndexes.get(0).getField());
    assertTrue(customerIndexes.get(0).isUnique());
    assertEquals("index1", customerIndexes.get(1).getField());
    assertFalse(customerIndexes.get(1).isUnique());
    assertEquals("index2", customerIndexes.get(2).getField());
    assertFalse(customerIndexes.get(2).isUnique());
  }

  @Test
  void buildSchemas_withValidSchemaFile_shouldReturnListOfSchemasWithReferences() throws IOException {
    String schemaContent = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "$id": "customer",
          "name": "customer",
          "description": "The Customer Data Schema",
          "type": "object",
          "properties": {
            "id": {
              "type": "string",
              "maxLength": 36
            },
            "name": {
              "type": "string"
            },
            "user": {
              "type": "string",
              "reference": {
                "schema": "user_schema"
              }
            },
            "branch": {
              "oneOf": [
                {
                  "type": "null"
                },
                {
                  "type": "array",
                  "items": {
                    "type": "string"
                  }
                }
              ],
              "reference": {
                "schema": "branch"
              }
            }
          }
        }
        """;
    var location = createTempSchemaDirectory(schemaContent);
    when(config.get(Schema.LOCATION_CONFIG)).thenReturn(Optional.of(location));

    Map<String, Schema> schemas = JSONSchema.buildSchemas(config);
    assertNotNull(schemas);
    assertFalse(schemas.isEmpty());
    assertEquals(1, schemas.size());

    var customerSchema = schemas.get("customer");
    assertEquals("customer", customerSchema.getName());

    var customerReferences = customerSchema.getReferences();
    assertEquals(2, customerReferences.size());

    Reference userReference = customerReferences.get("user");
    assertEquals("user", userReference.getAttribute());
    assertEquals("schema", userReference.getType());
    assertEquals("user_schema", userReference.getReference());
    Reference branchReference = customerReferences.get("branch");
    assertEquals("branch", branchReference.getAttribute());
    assertEquals("schema", branchReference.getType());
    assertEquals("branch", branchReference.getReference());
  }

  @Test
  void validateJson_withValidData_shouldSuccess() throws IOException {
    String schemaContent = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "$id": "customer",
          "name": "customer",
          "description": "The Customer Data Schema",
          "type": "object",
          "properties": {
            "id": {
              "type": "string",
              "maxLength": 36
            },
            "name": {
              "type": "string"
            }
          }
        }
        """;
    var location = createTempSchemaDirectory(schemaContent);
    when(config.get(Schema.LOCATION_CONFIG)).thenReturn(Optional.of(location));

    Map<String, Schema> schemas = JSONSchema.buildSchemas(config);
    assertNotNull(schemas);
    assertFalse(schemas.isEmpty());
    assertEquals(1, schemas.size());
    var customerSchema = schemas.get("customer");
    assertEquals("customer", customerSchema.getName());

    var json = new JSONObject().put("id", "123456789");
    json.put("name", "John Doe");
    var entity = mock(Entity.class);
    when(entity.toString()).thenReturn(json.toString());

    customerSchema.validate(entity.toString());
  }

  @Test
  void validateJson_withAdditionalData_shouldSuccess() throws IOException {
    String schemaContent = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "$id": "customer",
          "name": "customer",
          "description": "The Customer Data Schema",
          "type": "object",
          "properties": {
            "id": {
              "type": "string",
              "maxLength": 36
            },
            "name": {
              "type": "string"
            }
          },
          "additionalProperties": false
        }
        """;
    var location = createTempSchemaDirectory(schemaContent);
    when(config.get(Schema.LOCATION_CONFIG)).thenReturn(Optional.of(location));

    Map<String, Schema> schemas = JSONSchema.buildSchemas(config);
    assertNotNull(schemas);
    assertFalse(schemas.isEmpty());
    assertEquals(1, schemas.size());
    var customerSchema = schemas.get("customer");
    assertEquals("customer", customerSchema.getName());

    var json = new JSONObject().put("id", "123456789");
    json.put("name", "John Doe");
    json.put("additional", "Additional");
    var entity = mock(Entity.class);
    when(entity.toString()).thenReturn(json.toString());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> customerSchema.validate(entity.toString()));
    assertTrue(exception.getMessage().contains("Invalid json data"));
  }
}
