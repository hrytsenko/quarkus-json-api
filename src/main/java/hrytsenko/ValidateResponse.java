package hrytsenko;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Inject;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateResponse {

  @Nonbinding String schema() default "";

  @Slf4j
  @Provider
  @ValidateResponse
  @ConstrainedTo(RuntimeType.SERVER)
  class Interceptor implements WriterInterceptor {

    @Context
    ResourceInfo resource;
    @Inject
    SchemaValidator validator;

    @SneakyThrows
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) {
      log.debug("Validate response for '{}:{}'",
          resource.getResourceClass().getSimpleName(),
          resource.getResourceMethod().getName());

      var originalStream = context.getOutputStream();
      var interceptorStream = new ByteArrayOutputStream();
      context.setOutputStream(interceptorStream);

      context.proceed();

      var response = interceptorStream.toString(StandardCharsets.UTF_8);
      var schema = resource.getResourceMethod()
          .getAnnotation(ValidateResponse.class).schema();
      try {
        validator.validate(response, schema);
      } catch (SchemaValidator.Exception exception) {
        log.error("Invalid response: {}", exception.getViolations());
        throw new WebApplicationException(
            Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
      }

      originalStream.write(interceptorStream.toByteArray());
      originalStream.flush();
    }

  }

}
