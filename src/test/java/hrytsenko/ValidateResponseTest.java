package hrytsenko;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidateResponseTest {

  ValidateResponse.Interceptor interceptor;
  SchemaValidator validator;

  @BeforeEach
  void init() {
    validator = spy(new SchemaValidator());
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

    class Resource {

      static final String RESPONSE_SCHEMA = """
          type: object
          properties:
            foo:
              type: string
          required:
            - foo
          """;

      @ValidateResponse(schema = RESPONSE_SCHEMA)
      public String operation() {
        return response;
      }

    }

    var context = prepareContext(response, Resource.class);

    interceptor.aroundWriteTo(context);

    verify(validator).validate(eq(response), eq(Resource.RESPONSE_SCHEMA));
  }

  @Test
  void invalidResponse() {
    var response = """
        {
        }
        """;

    class Resource {

      static final String RESPONSE_SCHEMA = """
          type: object
          properties:
            foo:
              type: string
          required:
            - foo
          """;

      @ValidateResponse(schema = RESPONSE_SCHEMA)
      public String operation() {
        return response;
      }

    }

    var context = prepareContext(response, Resource.class);

    var exception = assertThrows(WebApplicationException.class,
        () -> interceptor.aroundWriteTo(context));
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
        exception.getResponse().getStatus());

    verify(validator).validate(eq(response), eq(Resource.RESPONSE_SCHEMA));
  }

  @SneakyThrows
  private WriterInterceptorContext prepareContext(String responseBody, Class<?> resourceClass) {
    class StreamWrapper {

      OutputStream stream;
    }

    var wrapper = new StreamWrapper();
    wrapper.stream = new ByteArrayOutputStream();

    var context = mock(WriterInterceptorContext.class);
    doAnswer(it -> wrapper.stream).when(context).getOutputStream();
    doAnswer(it -> {
      wrapper.stream = it.getArgument(0, OutputStream.class);
      return null;
    }).when(context).setOutputStream(any());
    doAnswer(it -> {
      context.getOutputStream().write(responseBody.getBytes());
      context.getOutputStream().flush();
      return null;
    }).when(context).proceed();

    var resource = mock(ResourceInfo.class);
    doReturn(resourceClass).when(resource).getResourceClass();
    doReturn(resourceClass.getMethod("operation")).when(resource).getResourceMethod();

    interceptor.resource = resource;

    return context;
  }

}
