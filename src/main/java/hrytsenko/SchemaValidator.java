package hrytsenko;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.SneakyThrows;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

import java.util.List;

@ApplicationScoped
public class SchemaValidator {

  public static final ObjectMapper YAML_READER = new ObjectMapper(new YAMLFactory());
  public static final ObjectMapper JSON_WRITER = new ObjectMapper();

  @SneakyThrows
  public void validate(String jsonRequest, String yamlSchema) {
    String jsonSchema = JSON_WRITER.writeValueAsString(
        YAML_READER.readValue(yamlSchema, Object.class));

    try {
      SchemaLoader.load(new JSONObject(jsonSchema))
          .validate(new JSONObject(jsonRequest));
    } catch (ValidationException exception) {
      throw new Exception("Validation failed", exception.getAllMessages());
    }

  }

  public static class Exception extends RuntimeException {

    @Getter
    private final List<String> violations;

    public Exception(String message, List<String> violations) {
      super(message);
      this.violations = violations;
    }

  }

}
