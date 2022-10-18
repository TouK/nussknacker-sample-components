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
    val versionFromEnv = System.getenv()["NUSSKNACKER_VERSION"]
    if (versionFromEnv.isNullOrBlank()) {
        nussknackerVersion = File("./nussknacker.version").readText(Charsets.UTF_8).trim()
    } else {
        nussknackerVersion = versionFromEnv
    }
}
println("Nussknacker version: $nussknackerVersion")

repositories {
    maven("https://oss.sonatype.org/content/groups/public/")
    mavenCentral()
    maven("https://packages.confluent.io/maven")
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
tasks.create("buildLiteRuntimeAppImage", DockerBuildImage::class) {
    commonDockerAction("touk/nussknacker-lite-runtime-app", "${rootProject.name}-lite-runtime-app", this)
}

tasks.create("buildImages", DefaultTask::class) {
    dependsOn("buildDesignerImage", "buildLiteRuntimeAppImage")
}

tasks.test {
    useJUnitPlatform()
}
