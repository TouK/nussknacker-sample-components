import org.gradle.api.Project

/**
 * A custom "version catalog", because the official one still doesn't allow for version overrides,
 * and a workaround code would be required in settings.gradle.kts of each subproject:
 * https://github.com/gradle/gradle/issues/20070
 */
abstract class Versions(private val project: Project) {
    val nussknacker: String = run {
        System.getenv()["NUSSKNACKER_VERSION"]?.ifBlank { null }
            ?: version("nussknackerVersion")
    }
    val scala: String = version("scalaVersion")

    val slf4jFlink: String = version("slf4JFlinkVersion")
    val commonsLang3Flink: String = version("commonsLang3FlinkVersion")

    val junit: String = version("junitVersion")

    private fun version(name: String): String =
        project.properties[name] as String?
            ?: throw RuntimeException("Version property `$name` is missing from gradle.properties")
}
