
name := "nussknacker-sample-components"
organization := "pl.touk.nussknacker"
scalaVersion := "2.12.10"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val nussknackerVersion = IO.read(new File("nussknacker.version"))
libraryDependencies ++= Seq(
  "pl.touk.nussknacker" %% "nussknacker-api" % nussknackerVersion % "provided",
  "org.apache.commons" % "commons-text" % "1.8" exclude("org.apache.commons", "commons-lang3"),
  "org.apache.commons" % "commons-lang3" % "3.9" % "provided"
)

assembly / assemblyJarName := "sampleComponents.jar"
assembly / assemblyOption := (assembly / assemblyOption).value.withIncludeScala(false)
