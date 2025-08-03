package hrytsenko;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ValidateResponseTest {

  ValidateResponse.Interceptor interceptor;
  SchemaValidator validator;

  @BeforeEach
  void init() {
    validator = Mockito.spy(new SchemaValidator());
    interceptor = new ValidateResponse.Interceptor();
    interceptor.validator = validator;
  }

  @Test
  void validResponse() {
    var response = """
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
      @ValidateResponse(schema = schema)
      public String operation() {
        return response;
      }
    }

    var context = prepareContext(response, Resource.class);

    interceptor.aroundWriteTo(context);

    verify(validator).validate(eq(response), eq(schema));
  }

  @Test
  void invalidResponse() {
    var response = """
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
      @ValidateResponse(schema = schema)
      public String operation() {
        return response;
      }
    }

    var context = prepareContext(response, Resource.class);

    var exception = assertThrows(WebApplicationException.class, () -> interceptor.aroundWriteTo(context));
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getResponse().getStatus());

    verify(validator).validate(eq(response), eq(schema));
  }

  @SneakyThrows
  private WriterInterceptorContext prepareContext(String response, Class<?> resourceClass) {
    class StreamWrapper {
      OutputStream outputStream;
    }

    var wrapper = new StreamWrapper();
    wrapper.outputStream = new ByteArrayOutputStream();

    var context = Mockito.mock(WriterInterceptorContext.class);
    doAnswer(it -> wrapper.outputStream)
        .when(context).getOutputStream();
    doAnswer(it -> {
      wrapper.outputStream = it.getArgument(0, OutputStream.class);
      return null;
    })
        .when(context).setOutputStream(Mockito.any());
    doAnswer(it -> {
      context.getOutputStream().write(response.getBytes());
      context.getOutputStream().flush();
      return null;
    })
        .when(context).proceed();

    var resourceInfo = Mockito.mock(ResourceInfo.class);
    doReturn(resourceClass)
        .when(resourceInfo).getResourceClass();
    doReturn(resourceClass.getMethod("operation"))
        .when(resourceInfo).getResourceMethod();

    interceptor.resourceInfo = resourceInfo;

    return context;
  }

}
