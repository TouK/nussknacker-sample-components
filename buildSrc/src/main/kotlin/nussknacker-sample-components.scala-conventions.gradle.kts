plugins {
    id("nussknacker-sample-components.conventions")
    scala
}

repositories {
    // SNAPSHOT versions of Nussknacker
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    mavenCentral()
    maven("https://packages.confluent.io/maven")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}
tasks.withType<ScalaCompile>().configureEach {
    scalaCompileOptions.additionalParameters = listOf(
        "-unchecked",
        "-deprecation",
        "-release:11",
    )
}

val versions = object : Versions(rootProject){}

dependencies {
    // Scala dependencies need to be added manually - scala.scalaVersion adds scala-library as a runtime dependency,
    // causing it to be runtimeClasspath, which in turn makes it used by shadowJar
    compileOnly("org.scala-lang:scala-library:${versions.scala}")
    testImplementation("org.scala-lang:scala-library:${versions.scala}")

    testImplementation("org.junit.jupiter:junit-jupiter:${versions.junit}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.configureEach {
    resolutionStrategy {
        force("org.slf4j:slf4j-api:${versions.slf4jFlink}")
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens=java.base/java.time=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
    )
    testLogging {
        showStandardStreams = System.getenv()["CI"] == "true"
    }
}
