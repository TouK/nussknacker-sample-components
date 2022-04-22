import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
    base
    `java-library`
    scala
    idea
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.bmuschko.docker-remote-api") version "7.0.0"
}

val scalaVersion = "2.12.10"

group = "pl.touk.nussknacker"

var nussknackerVersion: String by extra
ext {
    nussknackerVersion = File("./nussknacker.version").readText(Charsets.UTF_8)
}

repositories {
    maven("https://oss.sonatype.org/content/groups/public/")
    mavenCentral()
}

fun commonDockerAction(from: String, target: String, task: DockerBuildImage) {
    with(task) {
        dependsOn(tasks.shadowJar)
        inputDir.set(file("docker"))
        images.add("$target:latest")
        buildArgs.putAll(mapOf(
            "JAR_PATH" to (tasks.shadowJar.flatMap { it.archiveFile }.get().asFile.absolutePath),
            "NU_IMAGE" to "$from:${nussknackerVersion}"
        ))
    }
}

tasks.create("buildDesignerImage", DockerBuildImage::class) {
    commonDockerAction("touk/nussknacker", rootProject.name, this)
}
tasks.create("buildLiteKafkaRuntimeImage", DockerBuildImage::class) {
    commonDockerAction("touk/nussknacker-lite-kafka-runtime", "${rootProject.name}-lite-kafka-runtime", this)
}

tasks.create("buildImages", DefaultTask::class) {
    dependsOn("buildDesignerImage", "buildLiteKafkaRuntimeImage")
}

tasks.test {
    useJUnitPlatform()
}