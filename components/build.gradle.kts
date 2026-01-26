plugins {
    id("nussknacker-sample-components.scala-conventions")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(platform("pl.touk.nussknacker:nussknacker-bom_2.13:${versions.nussknacker}"))

    //nussknacker-api should not be included in fatjar, as it's provided by engine/designer
    compileOnly("pl.touk.nussknacker:nussknacker-components-api_2.13")
    implementation("org.apache.commons:commons-text:1.10.0") {
        exclude("org.apache.commons", "commons-lang3")
    }
    compileOnly("org.apache.commons:commons-lang3:${versions.commonsLang3Flink}")
}
