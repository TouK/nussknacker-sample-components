This project contains sample implementation of custom components for [Nussknacker](https://nussknacker.io).

# Components

## Common

We provide simple [enricher provider](components/src/main/scala/pl/touk/nussknacker/sample/SampleComponentProvider.scala)
which generates random string, using `commons-text` library to demonstrate how to add additional dependencies.

## Streaming-Flink only

[Flink provider](flink-components/src/main/scala/pl/touk/nussknacker/sample/FlinkSampleComponentProvider.scala) contains
sample Flink components to give an idea of how to implement Flink based sources, sinks and custom transformations.

- `cdr` (call detail record) - a source that emits specific case class
- `csvSource` - a source that emits a CSV record according to the row definition, shares the same Flink source function 
  as `cdr` source
- `csvSink` - a sink that writes a row represented as a list of strings to the given file

# Usage

To use custom component, you have to put fat jar with the component 
on [model classpath](https://docs.nussknacker.io/documentation/docs/installation_configuration_guide/ModelConfiguration#classpath-configuration).
If you use default configuration, the easiest way is to put the jar in `components/common` directory, 
as this directory is added to classpath.

If you are using setup based on docker-compose, you can mount jar as additional volume, adding 
```
- /(...)/sampleComponents.jar:/opt/nussknacker/components/common/sampleComponents.jar
``` 
in [docker-compose](https://github.com/TouK/nussknacker-quickstart/blob/main/docker/docker-compose.yml#L25)

For more production-ready setups (e.g. Kubernetes) it is better to create image based on
official Nussknacker image with jar added. See gradle project and `docker/Dockerfile` for a sample.

`docker/runDocker.sh` provides minimal script allowing to run Designer with added library 
(it doesn't connect with Kafka, Flink etc., but can be used to check if component is configured properly)

## Gradle
    
`./gradlew shadowJar` to build fat jar
`./gradlew buildImages` to build Docker images of Designer and Lite runtime. 

## Sbt

`sbt assembly` to build fat jar. At the moment we don't provide scripts for image generation,
use `docker/Dockerfile` as a base. 
