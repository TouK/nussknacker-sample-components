plugins {
    base
    `java-library`
    scala
    idea
    id("com.github.johnrengelman.shadow")
    id("com.bmuschko.docker-remote-api")
}

group = "pl.touk.nussknacker"
val scalaVersion = "2.13.18"
val junitVersion = "5.9.0"

repositories {
    //this is where NU artifacts are stored
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}

val nussknackerVersion: String by rootProject.extra

dependencies {
    implementation(platform("pl.touk.nussknacker:nussknacker-bom_2.13:${nussknackerVersion}"))
    implementation(project(":components"))

    compileOnly("org.scala-lang:scala-library")
    testImplementation("org.scala-lang:scala-library")
    testImplementation("pl.touk.nussknacker:nussknacker-lite-components-testkit_2.13")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.test {
    useJUnitPlatform()
}
