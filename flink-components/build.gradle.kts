plugins {
    base
    `java-library`
    scala
    idea
    id("com.github.johnrengelman.shadow")
    id("com.bmuschko.docker-remote-api")
}

group = "pl.touk.nussknacker"
val scalaVersion = "2.12.10"
val junitVersion = "5.9.0"

repositories {
    //this is where NU artifacts are stored
    maven("https://oss.sonatype.org/content/groups/public/")

    mavenCentral()
}

val nussknackerVersion: String by rootProject.extra

dependencies {
    implementation(project(":components"))

    compileOnly("org.scala-lang:scala-library:${scalaVersion}")
    compileOnly("pl.touk.nussknacker:nussknacker-components-api_2.12:${nussknackerVersion}")
    compileOnly("pl.touk.nussknacker:nussknacker-flink-components-api_2.12:${nussknackerVersion}")
    compileOnly("pl.touk.nussknacker:nussknacker-flink-components-utils_2.12:${nussknackerVersion}")
    compileOnly("org.apache.flink:flink-streaming-scala_2.12:1.15.2")
    testImplementation("org.scala-lang:scala-library:${scalaVersion}")
    testImplementation("pl.touk.nussknacker:nussknacker-flink-components-testkit_2.12:${nussknackerVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.test {
    useJUnitPlatform()
}
