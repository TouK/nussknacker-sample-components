package pl.touk.nussknacker.sample.csv

import org.junit.jupiter.api.Test
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers
import pl.touk.nussknacker.engine.api.component.ComponentDefinition
import pl.touk.nussknacker.engine.api.context.ProcessCompilationError
import pl.touk.nussknacker.engine.api.context.ProcessCompilationError.CustomNodeError
import pl.touk.nussknacker.engine.api.parameter.ParameterName
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.flink.util.test.FlinkTestScenarioRunner._
import pl.touk.nussknacker.engine.graph.expression.Expression
import pl.touk.nussknacker.engine.spel.Implicits.asSpelExpression
import pl.touk.nussknacker.engine.util.test.TestScenarioRunner
import pl.touk.nussknacker.sample.FlinkSampleComponentsBaseClassTest
import pl.touk.nussknacker.test.ValidatedValuesDetailedMessage

import java.nio.file.Files
import scala.collection.JavaConverters._

class CsvSourceTest extends Matchers with ValidatedValuesDetailedMessage {

  import CsvSourceTest._

  @Test
  def shouldReadCSVAccordingToTheDefinition(): Unit = {
    val csvFile = Files.createTempFile("test", ".csv")
    csvFile.toFile.deleteOnExit()
    val rows = List(
      "Alice;48111111111",
      "Bob;48222222222",
    )
    Files.write(csvFile, rows.asJava)
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("source", "csvSource", "fileName" -> s"'${csvFile.getFileName.toString}'", "definition" -> "{{'name', 'String'}, {'phoneNumber', 'Long'}}")
      .processorEnd("end", TestScenarioRunner.testResultService, "value" -> "#input")
    val runner = TestScenarioRunner
      .flinkBased(config, flinkMiniCluster)
      .withExtraComponents(ComponentDefinition("csvSource", new GenericCsvSourceFactory(csvFile.getParent.toString, ';')) :: Nil)
      .build()

    val results = runner.runWithoutData[java.util.Map[String, Any]](scenario).validValue

    inside(results.successes) {
      case row1 :: row2 :: Nil =>
        row1.get("name") shouldBe a[String]
        row1.get("name") shouldBe "Alice"
        row1.get("phoneNumber") shouldBe a[java.lang.Long]
        row1.get("phoneNumber") shouldBe 48111111111L
        row2.get("name") shouldBe "Bob"
        row2.get("phoneNumber") shouldBe 48222222222L
    }
  }

  @Test
  def shouldThrowOnNonReadableFile(): Unit = {
    testCompilationErrors("fileName" -> "'unexisting.csv'", "definition" -> "{{'name', 'String'}, {'phoneNumber', 'Long'}}") should
      contain (CustomNodeError("source", "File: 'unexisting.csv' is not readable", Some(ParameterName("fileName"))))
  }

  @Test
  def shouldThrowOnMalformedDefinition(): Unit = {
    val emptyFile = Files.createTempFile("test", ".csv")
    emptyFile.toFile.deleteOnExit()
    testCompilationErrors("fileName" -> s"'${emptyFile.getFileName.toString}'", "definition" -> "{{'name', 'String'}, {'phoneNumber'}}") should
      contain (CustomNodeError("source", "Column 2 should have name and type", Some(ParameterName("definition"))))
  }

  @Test
  def shouldThrowOnUnknownType(): Unit = {
    val emptyFile = Files.createTempFile("test", ".csv")
    emptyFile.toFile.deleteOnExit()
    testCompilationErrors("fileName" -> s"'${emptyFile.getFileName.toString}'", "definition" -> "{{'name', 'String'}, {'phoneNumber', 'Integer'}, {'callDuration', 'java.time.Duration'}}") should
      contain allOf(
      CustomNodeError("source", "Type for column 'phoneNumber' is not supported", Some(ParameterName("definition"))),
      CustomNodeError("source", "Type for column 'callDuration' is not supported", Some(ParameterName("definition"))))
  }

  private def testCompilationErrors(params: (String, Expression)*): List[ProcessCompilationError] = {
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("source", "csvSource", params: _*)
      .processorEnd("end", TestScenarioRunner.testResultService, "value" -> "#input")
    val runner = TestScenarioRunner
      .flinkBased(config, flinkMiniCluster)
      .withExtraComponents(ComponentDefinition("csvSource", new GenericCsvSourceFactory("/tmp", ';')) :: Nil)
      .build()
    runner.runWithoutData[java.util.Map[String, Any]](scenario).invalidValue.toList
  }
}

object CsvSourceTest extends FlinkSampleComponentsBaseClassTest
