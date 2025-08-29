package hrytsenko;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
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
  public ClientResponse getPhone(@PathParam("username") String username) {
    log.info("Get phone for '{}'", username);
    return switch (username) {
      case "alice" -> new ClientResponse("380950000000");
      case "bob" -> new ClientResponse("+380950000000");
      default -> throw new BadRequestException("Unknown username");
    };
  }

  public record ClientResponse(String phone) {

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
  public void updatePhone(@PathParam("username") String username, ClientRequest request) {
    log.info("Set phone for '{}' to '{}'", username, request);
  }

  public record ClientRequest(String phone) {

  }

}
