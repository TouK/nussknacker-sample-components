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
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    mavenCentral()
}

val nussknackerVersion: String by rootProject.extra

dependencies {
    implementation(platform("pl.touk.nussknacker:nussknacker-bom_2.13:${nussknackerVersion}"))
    implementation(project(":components"))

    compileOnly("org.scala-lang:scala-library")
    compileOnly("pl.touk.nussknacker:nussknacker-components-api_2.13")
    compileOnly("pl.touk.nussknacker:nussknacker-components-utils_2.13")
    compileOnly("pl.touk.nussknacker:nussknacker-flink-components-api_2.13")
    compileOnly("pl.touk.nussknacker:nussknacker-flink-components-utils_2.13")
    compileOnly("org.apache.flink:flink-streaming-java")
    testImplementation("org.scala-lang:scala-library")
    testImplementation("pl.touk.nussknacker:nussknacker-flink-components-testkit_2.13")
    testImplementation("pl.touk.nussknacker:nussknacker-flink-test-utils_2.13")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
}
