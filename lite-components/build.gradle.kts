plugins {
    id("nussknacker-sample-components.scala-conventions")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(platform("pl.touk.nussknacker:nussknacker-bom_2.13:${versions.nussknacker}"))
    implementation(project(":components"))

    testImplementation("pl.touk.nussknacker:nussknacker-lite-components-testkit_2.13")
}
