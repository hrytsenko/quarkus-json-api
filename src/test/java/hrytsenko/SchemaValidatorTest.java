package hrytsenko;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaValidatorTest {

  SchemaValidator validator;

  @BeforeEach
  void init() {
    validator = new SchemaValidator();
  }

  @Test
  void validContent() {
    String content = """
        {
          "foo": "bar"
        }
        """;

    final var schema = """
        type: object
        properties:
          foo:
            type: string
        required:
          - foo
        """;

    validator.validate(content, schema);
  }

  @Test
  void invalidContent() {
    String content = """
        {
        }
        """;

    final var schema = """
        type: object
        properties:
          foo:
            type: string
        required:
          - foo
        """;

    assertThrows(SchemaValidator.Exception.class, () -> validator.validate(content, schema));
  }

}
