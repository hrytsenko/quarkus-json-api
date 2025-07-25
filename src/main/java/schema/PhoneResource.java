package schema;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/phones")
public class PhoneResource {

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ValidateBySchema(
      schema = """
          type: object
          properties:
            phone:
              type: string
              pattern: '^\\d{12}$'
          required:
            - phone
          additionalProperties: false
          """
  )
  public void updatePhone(Request request) {
    log.info("Request received: {}", request);
  }

  public record Request(String phone) {
  }

}
