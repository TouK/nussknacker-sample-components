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
    maven("https://packages.confluent.io/maven/")

    mavenCentral()
}

val nussknackerVersion: String by rootProject.extra

dependencies {
    implementation(platform("pl.touk.nussknacker:nussknacker-bom_2.12:${nussknackerVersion}"))
    implementation(project(":components"))

    compileOnly("org.scala-lang:scala-library")
    testImplementation("org.scala-lang:scala-library")
    testImplementation("pl.touk.nussknacker:nussknacker-lite-components-testkit_2.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.test {
    useJUnitPlatform()
}

//for gradle wrongly resolving scala 2.13
configurations.all {
    resolutionStrategy.force("org.scala-lang:scala-library:$scalaVersion")
}
