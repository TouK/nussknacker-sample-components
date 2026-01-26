This project contains sample implementation of custom components for [Nussknacker](https://nussknacker.io).

# Components

## Common

We provide a simple [enricher provider](components/src/main/scala/pl/touk/nussknacker/sample/SampleComponentProvider.scala)
which generates a random string, using the `commons-text` library, demonstrating how to add dependencies.

## Streaming-Flink only

[Flink provider](flink-components/src/main/scala/pl/touk/nussknacker/sample/FlinkSampleComponentProvider.scala) contains
sample Flink components to give an idea of how to implement Flink based sources, sinks and custom transformations.

- `cdr` (call detail record) - a source that emits specific case class
- `csvSource` - a source that emits a CSV record according to the row definition, shares the same Flink source function 
  as `cdr` source
- `csvSink` - a sink that writes a row represented as a list of strings to the given file

# Usage

To use a custom component, you have to put a fat JAR with the component 
on [model classpath](https://docs.nussknacker.io/documentation/docs/configuration/model/ModelConfiguration/#classpath-configuration).
If you use the default configuration, the easiest way is to put the JAR file in `components/common` directory.

If you are using a setup based on Docker Compose, you can mount JAR file as an additional volume, adding 
```yaml
- /(...)/sampleComponents.jar:/opt/nussknacker/components/common/sampleComponents.jar
``` 
in [docker-compose.yml](https://github.com/TouK/nussknacker-quickstart/blob/main/docker-compose.yml#L22)

For more production-ready setups (e.g. Kubernetes) it is better to create an image based on
the official Nussknacker image with appropriate JAR files added.

## Gradle

`./gradlew shadowJar` to build fat JARs
