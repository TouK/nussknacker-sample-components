package pl.touk.nussknacker.sample.csv

import org.junit.runner.RunWith
import org.scalatest.Inside.inside
import org.scalatest.Matchers
import org.scalatestplus.junit.JUnitRunner
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.spel.Implicits.asSpelExpression
import pl.touk.nussknacker.sample.BaseSourceTest

import java.nio.file.Files
import java.time.Duration
import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class CallDetailRecordSourceTest extends BaseSourceTest with Matchers  {

  test("should read CDR records") {
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
      .processorEnd("end", "invocationCollector", "value" -> "#input")
    registerScenario(scenario)

    env.executeAndWaitForFinished(scenario.id)()

    val results = invocationCollectorResults[CallDetailRecord]()
    inside(results) {
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

  test("should throw on non readable file") {
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("cdr source", "cdr", "fileName" -> s"'unexisting.csv'")
      .processorEnd("end", "invocationCollector", "value" -> "#input")

    intercept[IllegalArgumentException] {
      registerScenario(scenario)
    }.getMessage should (include ("Compilation errors:") and include ("unexisting.csv") and include ("is not readable"))
  }
}
