package hrytsenko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import java.io.ByteArrayInputStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    class Resource {

      static final String REQUEST_SCHEMA = """
          type: object
          properties:
            foo:
              type: string
          required:
            - foo
          """;

      @ValidateRequest(schema = REQUEST_SCHEMA)
      public void operation(String request) {
      }

    }

    var request = """
        {
          "foo": "bar"
        }
        """;
    var context = prepareContext(request, Resource.class);

    interceptor.aroundReadFrom(context);

    verify(validator).validate(eq(request), eq(Resource.REQUEST_SCHEMA));
  }

  @Test
  void invalidRequest() {
    class Resource {

      static final String REQUEST_SCHEMA = """
          type: object
          properties:
            foo:
              type: string
          required:
            - foo
          """;

      @ValidateRequest(schema = REQUEST_SCHEMA)
      public void operation(String request) {
      }

    }

    var request = """
        {
        }
        """;
    var context = prepareContext(request, Resource.class);

    var exception = assertThrows(WebApplicationException.class,
        () -> interceptor.aroundReadFrom(context));
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());

    verify(validator).validate(eq(request), eq(Resource.REQUEST_SCHEMA));
  }

  @SneakyThrows
  private ReaderInterceptorContext prepareContext(String requestBody, Class<?> resourceClass) {
    var context = mock(ReaderInterceptorContext.class);
    doReturn(new ByteArrayInputStream(requestBody.getBytes()))
        .when(context).getInputStream();

    var resource = mock(ResourceInfo.class);
    doReturn(resourceClass)
        .when(resource).getResourceClass();
    doReturn(resourceClass.getMethod("operation", String.class))
        .when(resource).getResourceMethod();

    interceptor.resource = resource;

    return context;
  }

}
