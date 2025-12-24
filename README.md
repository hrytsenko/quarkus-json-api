# Quarkus JSON API

Validate JSON requests and responses via [YAML](https://yaml.org/) representation of [JSON Schema](https://json-schema.org/)
for [Quarkus REST](https://quarkus.io/guides/rest).

## Risks

* [API3:2023 Broken Object Property Level Authorization](https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/)
* [CWE-20: Improper Input Validation](https://cwe.mitre.org/data/definitions/20.html)
* [CWE-200: Exposure of Sensitive Information to an Unauthorized Actor](https://cwe.mitre.org/data/definitions/200.html)

## Libraries

* [Jackson Dataformat YAML](https://github.com/FasterXML/jackson-dataformats-text) — parse YAML schemas.
* [JSON Schema Validator](https://github.com/everit-org/json-schema/) — validate JSON against schemas. 

## Commands

Run:

```shell
quarkus dev
```

Test:

```shell
docker run --rm -t -v ${PWD}:/workdir jetbrains/intellij-http-client -D explore.rest
```
