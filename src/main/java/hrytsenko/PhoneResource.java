package hrytsenko;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/phones/{username}")
public class PhoneResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ValidateResponse(
      schema = """
          type: object
          properties:
            phone:
              type: string
              pattern: '^\\d{12}$'
          required:
            - phone
          additionalProperties: false
          """)
  public ApiResponse getPhone(@PathParam("username") String username) {
    log.info("Get phone for '{}'", username);
    return switch (username) {
      case "alice" -> new ApiResponse("380950000000");
      case "bob" -> new ApiResponse("+380950000000");
      default -> throw new BadRequestException("Unknown username");
    };
  }

  public record ApiResponse(String phone) {
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ValidateRequest(
      schema = """
          type: object
          properties:
            phone:
              type: string
              pattern: '^\\d{12}$'
          required:
            - phone
          additionalProperties: false
          """)
  public void updatePhone(@PathParam("username") String username, ApiRequest request) {
    log.info("Set phone for '{}' to '{}'", username, request);
  }

  public record ApiRequest(String phone) {
  }

}
