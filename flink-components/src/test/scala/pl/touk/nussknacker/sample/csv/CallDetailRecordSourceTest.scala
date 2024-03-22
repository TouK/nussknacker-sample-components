package pl.touk.nussknacker.sample.csv

import org.junit.jupiter.api.Test
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers
import pl.touk.nussknacker.engine.api.context.ProcessCompilationError.CustomNodeError
import pl.touk.nussknacker.engine.api.component.ComponentDefinition
import pl.touk.nussknacker.engine.api.parameter.ParameterName
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.flink.util.test.FlinkTestScenarioRunner._
import pl.touk.nussknacker.engine.spel.Implicits.asSpelExpression
import pl.touk.nussknacker.engine.util.test.TestScenarioRunner
import pl.touk.nussknacker.sample.FlinkSampleComponentsBaseClassTest
import pl.touk.nussknacker.test.ValidatedValuesDetailedMessage

import java.nio.file.Files
import java.time.Duration
import scala.collection.JavaConverters._

class CallDetailRecordSourceTest extends Matchers with ValidatedValuesDetailedMessage {

  import CallDetailRecordSourceTest._

  @Test
  def shouldReadCdrRecords(): Unit = {
    val cdrsFile = Files.createTempFile("cdr", ".csv")
    cdrsFile.toFile.deleteOnExit()
    val cdrs = List(
      "48111111111;48111111112;42;1",
      "48222222221;48222222222;64;2",
    )
    Files.write(cdrsFile, cdrs.asJava)
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("cdr source", "cdr", "fileName" -> s"'${cdrsFile.getFileName.toString}'")
      .processorEnd("end", TestScenarioRunner.testResultService, "value" -> "#input")
    val runner = TestScenarioRunner
      .flinkBased(config, flinkMiniCluster)
      .withExtraComponents(ComponentDefinition("cdr", CallDetailRecordSourceFactory.prepare(cdrsFile.getParent.toString, ';')) :: Nil)
      .build()

    val results = runner.runWithoutData[CallDetailRecord](scenario).validValue

    inside(results.successes) {
      case cdr1 :: cdr2 :: Nil =>
        cdr1.phoneNumberA shouldBe "48111111111"
        cdr1.phoneNumberB shouldBe "48111111112"
        cdr1.callDuration shouldBe Duration.ofSeconds(42)
        cdr1.callStartTime shouldBe 1L
        cdr2.phoneNumberA shouldBe "48222222221"
        cdr2.phoneNumberB shouldBe "48222222222"
        cdr2.callDuration shouldBe Duration.ofSeconds(64)
        cdr2.callStartTime shouldBe 2L
    }
  }

  @Test
  def shouldThrowOnNonReadableFile(): Unit = {
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("cdr source", "cdr", "fileName" -> s"'unexisting.csv'")
      .processorEnd("end", TestScenarioRunner.testResultService, "value" -> "#input")
    val runner = TestScenarioRunner
      .flinkBased(config, flinkMiniCluster)
      .withExtraComponents(ComponentDefinition("cdr", CallDetailRecordSourceFactory.prepare("/tmp", ';')) :: Nil)
      .build()

    val compilationErrors = runner.runWithoutData[CallDetailRecord](scenario).invalidValue.toList

    compilationErrors should contain only CustomNodeError("cdr source", "File: '/tmp/unexisting.csv' is not readable", Some(ParameterName("fileName")))
  }
}

object CallDetailRecordSourceTest extends FlinkSampleComponentsBaseClassTest
