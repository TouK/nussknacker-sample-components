plugins {
    base
    `java-library`
    scala
    idea
    id("com.github.johnrengelman.shadow")
    id("com.bmuschko.docker-remote-api")
}

group = "pl.touk.nussknacker"

repositories {
    //this is where NU artifacts are stored
    maven("https://oss.sonatype.org/content/groups/public/")

    mavenCentral()
}

val nussknackerVersion: String by rootProject.extra

dependencies {
    implementation(platform("pl.touk.nussknacker:nussknacker-bom_2.12:${nussknackerVersion}"))

    compileOnly("org.scala-lang:scala-library")
    testImplementation("org.scala-lang:scala-library")
    //nussknacker-api should not be included in fatjar, as it's provided by engine/designer
    compileOnly("pl.touk.nussknacker:nussknacker-components-api_2.12")
    implementation("org.apache.commons:commons-text:1.8") {
        exclude("org.apache.commons", "commons-lang3")
    }
    compileOnly("org.apache.commons:commons-lang3:3.9")
}

tasks.test {
    useJUnitPlatform()
}
