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
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import java.io.ByteArrayInputStream;
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
public @interface ValidateRequest {

  @Nonbinding String schema() default "";

  @Slf4j
  @Provider
  @ValidateRequest
  @ConstrainedTo(RuntimeType.SERVER)
  class Interceptor implements ReaderInterceptor {

    @Context
    ResourceInfo resource;
    @Inject
    SchemaValidator validator;

    @SneakyThrows
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) {
      log.debug("Validate request for '{}:{}'",
          resource.getResourceClass().getSimpleName(),
          resource.getResourceMethod().getName());

      var originalContent = context.getInputStream().readAllBytes();

      var request = new String(originalContent, StandardCharsets.UTF_8);
      var schema = resource.getResourceMethod()
          .getAnnotation(ValidateRequest.class).schema();
      try {
        validator.validate(request, schema);
      } catch (SchemaValidator.Exception exception) {
        log.error("Invalid request: {}", exception.getViolations());
        throw new WebApplicationException(
            Response.status(Response.Status.BAD_REQUEST).build());
      }

      context.setInputStream(new ByteArrayInputStream(originalContent));
      return context.proceed();
    }

  }


}
