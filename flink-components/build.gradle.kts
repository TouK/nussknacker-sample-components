plugins {
    id("nussknacker-sample-components.scala-conventions")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(platform("pl.touk.nussknacker:nussknacker-bom_2.13:${versions.nussknacker}"))
    implementation(project(":components"))

    compileOnly("pl.touk.nussknacker:nussknacker-components-api_2.13")
    compileOnly("pl.touk.nussknacker:nussknacker-components-utils_2.13")
    compileOnly("pl.touk.nussknacker:nussknacker-flink-components-api_2.13")
    compileOnly("pl.touk.nussknacker:nussknacker-flink-components-utils_2.13")
    compileOnly("org.apache.flink:flink-streaming-java")
    testImplementation("pl.touk.nussknacker:nussknacker-flink-components-testkit_2.13")
    testImplementation("pl.touk.nussknacker:nussknacker-flink-test-utils_2.13")
}
