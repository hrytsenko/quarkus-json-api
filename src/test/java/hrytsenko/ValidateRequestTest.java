package hrytsenko;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ValidateRequestTest {

  ValidateRequest.Interceptor interceptor;
  SchemaValidator validator;

  @BeforeEach
  void init() {
    validator = spy(new SchemaValidator());
    interceptor = new ValidateRequest.Interceptor();
    interceptor.validator = validator;
  }

  @Test
  void validRequest() {
    var request = """
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

    class Resource {
      @ValidateRequest(schema = schema)
      public void operation(String request) {
      }
    }

    var context = prepareContext(request, Resource.class);

    interceptor.aroundReadFrom(context);

    verify(validator).validate(eq(request), eq(schema));
  }

  @Test
  void invalidRequest() {
    var request = """
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

    class Resource {
      @ValidateRequest(schema = schema)
      public void operation(String request) {
      }
    }

    var context = prepareContext(request, Resource.class);

    var exception = assertThrows(WebApplicationException.class, () -> interceptor.aroundReadFrom(context));
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());

    verify(validator).validate(eq(request), eq(schema));
  }

  @SneakyThrows
  private ReaderInterceptorContext prepareContext(String requestBody, Class<?> resourceClass) {
    var context = mock(ReaderInterceptorContext.class);
    doReturn(new ByteArrayInputStream(requestBody.getBytes()))
        .when(context).getInputStream();

    var resourceInfo = mock(ResourceInfo.class);
    doReturn(resourceClass)
        .when(resourceInfo).getResourceClass();
    doReturn(resourceClass.getMethod("operation", String.class))
        .when(resourceInfo).getResourceMethod();

    interceptor.resourceInfo = resourceInfo;

    return context;
  }

}
