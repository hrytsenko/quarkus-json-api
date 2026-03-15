# Quarkus JSON API

Validate requests and responses using [JSON Schema](https://json-schema.org/) (YAML format) with Quarkus REST.

The example focuses on the following well-known vulnerabilities:

* [API3:2023 Broken Object Property Level Authorization](https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/)
* [CWE-20: Improper Input Validation](https://cwe.mitre.org/data/definitions/20.html)
* [CWE-200: Exposure of Sensitive Information to an Unauthorized Actor](https://cwe.mitre.org/data/definitions/200.html)

## Example

Use `@ValidateRequest` to enforce a request schema:

```java
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
public void updatePhone(@PathParam("username") String username, UpdatePhoneRequest request) {}
```

Use `@ValidateResponse` to enforce a response schema:

```java
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
public GetPhoneResponse getPhone(@PathParam("username") String username) {}
```

## Usage

Start the application:

```shell
quarkus dev
```

Execute API tests:

```shell
docker run --rm -t -v ${PWD}:/workdir jetbrains/intellij-http-client -D explore.rest
```
