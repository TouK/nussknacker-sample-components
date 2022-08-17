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

repositories {
    //this is where NU artifacts are stored
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://packages.confluent.io/maven/")

    mavenCentral()
}

val nussknackerVersion: String by rootProject.extra

dependencies {
    implementation(project(":components"))

    compileOnly("org.scala-lang:scala-library:${scalaVersion}")
    testImplementation("org.scala-lang:scala-library:${scalaVersion}")
    testImplementation("pl.touk.nussknacker:nussknacker-lite-components-testkit_2.12:${nussknackerVersion}")

    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

//for gradle wrongly resoling scala 2.13
configurations.all {
    resolutionStrategy.force("org.scala-lang:scala-library:$scalaVersion")
}