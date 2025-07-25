package schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.Priority;
import jakarta.enterprise.util.Nonbinding;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateBySchema {

  @Nonbinding String schema() default "";

  @Slf4j
  @Provider
  @ValidateBySchema
  @Priority(Priorities.ENTITY_CODER)
  class WebInterceptor implements ReaderInterceptor {

    @Context
    ResourceInfo resourceInfo;

    @SneakyThrows
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) {
      var jsonRequest = new String(context.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

      var yamlSchema = resourceInfo.getResourceMethod().getAnnotation(ValidateBySchema.class).schema();
      var jsonSchema = convertToJsonSchema(yamlSchema);

      validate(jsonRequest, jsonSchema);

      context.setInputStream(new ByteArrayInputStream(jsonRequest.getBytes(StandardCharsets.UTF_8)));
      return context.proceed();
    }

    private void validate(String jsonRequest, String jsonSchema) {
      try {
        SchemaLoader.load(new JSONObject(jsonSchema))
            .validate(new JSONObject(jsonRequest));
      } catch (ValidationException exception) {
        log.error("Validation failed: {}", exception.getAllMessages());
        throw new WebApplicationException(
            Response.status(Response.Status.BAD_REQUEST).build());
      }
    }

    @SneakyThrows
    private static String convertToJsonSchema(String yamlSchema) {
      var yamlReader = new ObjectMapper(new YAMLFactory());
      var jsonWriter = new ObjectMapper();
      return jsonWriter.writeValueAsString(
          yamlReader.readValue(yamlSchema, Object.class)
      );
    }

  }


}
