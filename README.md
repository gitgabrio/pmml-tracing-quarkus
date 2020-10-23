# PMML Tracing Quarkus example

## Description

A simple PMML service to evaluate some models that might be consumed by the Trusty service.

## Installing and Running

### Prerequisites

You will need:
  - Java 11+ installed
  - Environment variable JAVA_HOME set accordingly
  - Maven 3.6.2+ installed

When using native image compilation, you will also need:
  - [GraalVM 19.3.1](https://github.com/oracle/graal/releases/tag/vm-19.3.1) installed
  - Environment variable GRAALVM_HOME set accordingly
  - Note that GraalVM native image compilation typically requires other packages (glibc-devel, zlib-devel and gcc) to be installed too.  You also need 'native-image' installed in GraalVM (using 'gu install native-image'). Please refer to [GraalVM installation documentation](https://www.graalvm.org/docs/reference-manual/aot-compilation/#prerequisites) for more details.

### Compile and Run in Local Dev Mode

```
mvn clean compile quarkus:dev
```

### Package and Run in JVM mode

```
mvn clean package
java -jar target/pmml-tracing-quarkus-runner.jar
```

or on Windows

```
mvn clean package
java -jar target\pmml-tracing-quarkus-runner.jar
```

### Package and Run using Local Native Image
Note that this requires GRAALVM_HOME to point to a valid GraalVM installation

```
mvn clean package -Pnative
```

To run the generated native executable, generated in `target/`, execute

```
./target/pmml-tracing-quarkus-runner
```

Note: This does not yet work on Windows, GraalVM and Quarkus should be rolling out support for Windows soon.

## OpenAPI (Swagger) documentation
[Specification at swagger.io](https://swagger.io/docs/specification/about/)

You can take a look at the [OpenAPI definition](http://localhost:8080/openapi?format=json) - automatically generated and included in this service - to determine all available operations exposed by this service. For easy readability you can visualize the OpenAPI definition file using a UI tool like for example available [Swagger UI](https://editor.swagger.io).

In addition, various clients to interact with this service can be easily generated using this OpenAPI definition.

When running in either Quarkus Development or Native mode, we also leverage the [Quarkus OpenAPI extension](https://quarkus.io/guides/openapi-swaggerui#use-swagger-ui-for-development) that exposes [Swagger UI](http://localhost:8080/swagger-ui/) that you can use to look at available REST endpoints and send test requests.

## Example Usage

Start the Kafka container with `docker-compose up` command.

Once Kafka container is up and running, the pmml-tracing-quarkus can be started, and it will connect to the running Kafka, listening for incoming messages on the
`kogito-tracing-prediction-input` topic.
For each incoming message on the above topic, a PMML4Result will be published on the `kogito-tracing-prediction-output` one.

### PUBLISH 
Publishing
```json
{
   "inputData": {
    "fld1": 3,
    "fld2": 2,
    "fld3": "y"
    },
   "pmmlModel": "LinReg"
}
```
will return
```json
{
  "correlationId":null,
  "segmentationId":null,
  "segmentId":null,
  "segmentIndex":0,
  "resultCode":"OK",
  "resultObjectName":"fld4",
  "resultVariables":
  {
    "fld4":52.5
  }
}
```
Publishing
```json
{
  "inputData": {
    "temperature": 30.0,
    "humidity": 10.0
  },
  "pmmlModel": "SampleMine"
}
```
will return
```json
{ 
  "correlationId":null,
  "segmentationId":null,
  "segmentId":null,
  "segmentIndex":0, 
  "resultCode":"OK",
  "resultObjectName":"decision",
  "resultVariables": {
          "decision":"sunglasses",
          "weatherdecision":"sunglasses" 
                      }
}
```
Publishing
```json
{
  "inputData" : { 
    "input1": 5.0,
    "input2": -10.0
  },
   "pmmlModel": "SimpleScorecard"
}
```
will return
```json
{ 
  "correlationId":null,
  "segmentationId":null,
  "segmentId":null,
  "segmentIndex":0, 
  "resultCode":"OK",
  "resultObjectName":"score",
  "resultVariables": {
          "score":-15.0,
          "Score":-15.0,
          "Reason Code 1":"Input1ReasonCode",
          "Reason Code 2":"Input2ReasonCode"
          }
}
```
Publishing
```json
{
  "inputData": {
    "residenceState": "AP",
    "validLicense": true,
    "occupation": "ASTRONAUT",
    "categoricalY": "classA",
    "categoricalX": "red",
    "variable": 6.6,
    "age": 25.0
  }, 
  "pmmlModel": "PredicatesMining"
}
```
will return
```json
{
  "correlationId": null,
  "segmentationId": null,
  "segmentId": null,
  "segmentIndex": 0,
  "resultCode": "OK",
  "resultObjectName": "categoricalResult",
  "resultVariables": {
    "categoricalResult": 1.381666666666666
  }
}
```

## Integration example with Trusty Service

When the tracing addon is enabled, the tracing events are emitted and pushed to a Kafka broker. The [Trusty Service](https://github.com/kiegroup/kogito-apps/tree/master/trusty) can consume such events and store them on a storage. The Trusty Service exposes then some api to consume the information that has been collected. 
A `docker-compose` example is provided in the current folder. In particular, when `docker-compose up` is run, a Kafka broker is deployed. 
Once the services are up and running, after a decision has been evaluated, you can access the trusty service swagger at `localhost:8081/swagger-ui/` and try to query what are the evaluations of the last day at `localhost:8081/v1/executions` for example.
## Deploying with Kogito Operator

In the [`operator`](operator) directory you'll find the custom resources needed to deploy this example on OpenShift with the [Kogito Operator](https://docs.jboss.org/kogito/release/latest/html_single/#chap_kogito-deploying-on-openshift).
