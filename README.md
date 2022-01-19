This project contains sample implementation of custom components for [Nussknacker](https://nussknacker.io).

# Usage
       
At the moment we provide simple [enricher provider](src/main/scala/pl/touk/nussknacker/sample/SampleComponentProvider.scala), 
which generates random string, using `commons-text` library to demonstrate how to add additional dependencies.

To use custom component, you have to put fat jar with the component 
on [model classpath](https://docs.nussknacker.io/documentation/docs/installation_configuration_guide/ModelConfiguration#classpath-configuration).
If you use default configuration, the easiest way is to put the jar in `components/common` directory, 
as this directory is added to classpath.

If you are using setup based on docker-compose, you can mount jar as additional volume, adding 
```
- /(...)/sampleComponents.jar:/opt/nussknacker/components/common/sampleComponents.jar
``` 
in [docker-compose](https://github.com/TouK/nussknacker-quickstart/blob/main/docker-compose.yml#L25)

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