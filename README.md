This project contains sample implementation of custom components for [Nussknacker](https://nussknacker.io).

# Usage
       
At the moment we provide simple [enricher provider](src/main/scala/pl/touk/nussknacker/sample/SampleComponentProvider.scala), 
which generates random string, using `commons-text` library to demonstrate how to add additional dependencies.  

## Gradle
    
`./gradlew shadowJar` to build fat jar
`./gradlew buildImages` to build Docker images of Designer and Lite runtime. 

`docker/runDocker.sh` provides minimal script allowing to run Designer with added library 
(it doesn't connect with Kafka, Flink etc., but can be used to check if component is configured properly)

Use [Quickstart](https://docs.nussknacker.io/documentation/quickstart/docker/) to configure running example:
- replace [image](https://github.com/TouK/nussknacker-quickstart/blob/main/docker-compose.yml#L7)
- add `docker/components.conf` to [config files](https://github.com/TouK/nussknacker-quickstart/blob/main/docker-compose.yml#L13)

## Sbt

`sbt assembly` to build fat jar. At the moment we don't provide scripts for image generation,
use `docker/Dockerfile` as a base. 