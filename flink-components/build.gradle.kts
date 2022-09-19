plugins {
    base
    `java-library`
    scala
    idea
    id("com.github.johnrengelman.shadow")
    id("com.bmuschko.docker-remote-api")
}

group = "pl.touk.nussknacker"
val junitVersion = "5.9.0"

repositories {
    //this is where NU artifacts are stored
    maven("https://oss.sonatype.org/content/groups/public/")

    mavenCentral()
}

val nussknackerVersion: String by rootProject.extra

dependencies {
    implementation(platform("pl.touk.nussknacker:nussknacker-bom_2.12:${nussknackerVersion}"))
    implementation(project(":components"))

    compileOnly("org.scala-lang:scala-library")
    compileOnly("pl.touk.nussknacker:nussknacker-components-api_2.12")
    compileOnly("pl.touk.nussknacker:nussknacker-flink-components-api_2.12")
    compileOnly("pl.touk.nussknacker:nussknacker-flink-components-utils_2.12")
    compileOnly("org.apache.flink:flink-streaming-scala_2.12")
    testImplementation("org.scala-lang:scala-library")
    testImplementation("pl.touk.nussknacker:nussknacker-flink-components-testkit_2.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.test {
    useJUnitPlatform()
}
