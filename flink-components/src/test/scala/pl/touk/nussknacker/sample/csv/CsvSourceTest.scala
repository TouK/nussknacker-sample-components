package pl.touk.nussknacker.sample.csv

import org.junit.runner.RunWith
import org.scalatest.Inside.inside
import org.scalatest.Matchers
import org.scalatestplus.junit.JUnitRunner
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.graph.expression.Expression
import pl.touk.nussknacker.engine.spel.Implicits.asSpelExpression
import pl.touk.nussknacker.sample.BaseSourceTest

import java.nio.file.Files
import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class CsvSourceTest extends BaseSourceTest with Matchers {

  test("should read CSV according to definition") {
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
      .processorEnd("end", "invocationCollector", "value" -> "#input")
    registerScenario(scenario)

    env.executeAndWaitForFinished(scenario.id)()

    val results = invocationCollectorResults[java.util.Map[String, Any]]()
    inside(results) {
      case row1 :: row2 :: Nil =>
        row1.get("name") shouldBe a[String]
        row1.get("name") shouldBe "Alice"
        row1.get("phoneNumber") shouldBe a[java.lang.Long]
        row1.get("phoneNumber") shouldBe 48111111111L
        row2.get("name") shouldBe "Bob"
        row2.get("phoneNumber") shouldBe 48222222222L
    }
  }

  test("should throw on non readable file") {
    testCompilationErrors("fileName" -> "'unexisting.csv'", "definition" -> "{{'name', 'String'}, {'phoneNumber', 'Long'}}")
      .getMessage should (include("Compilation errors:") and include("unexisting.csv") and include("is not readable"))
  }

  test("should throw on malformed definition") {
    val emptyFile = Files.createTempFile("test", ".csv")
    emptyFile.toFile.deleteOnExit()
    testCompilationErrors("fileName" -> s"'${emptyFile.getFileName.toString}'", "definition" -> "{{'name', 'String'}, {'phoneNumber'}}")
      .getMessage should include ("Column 2 should have name and type")
  }

  test("should throw on unknown type") {
    val emptyFile = Files.createTempFile("test", ".csv")
    emptyFile.toFile.deleteOnExit()
    testCompilationErrors("fileName" -> s"'${emptyFile.getFileName.toString}'", "definition" -> "{{'name', 'String'}, {'phoneNumber', 'Integer'}, {'callDuration', 'java.time.Duration'}}")
      .getMessage should (include("Type for column 'phoneNumber' is not supported") and include("Type for column 'callDuration' is not supported"))
  }

  private def testCompilationErrors(params: (String, Expression)*): Exception = {
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("source", "csvSource", params: _*)
      .processorEnd("end", "invocationCollector", "value" -> "#input")

    intercept[IllegalArgumentException] {
      registerScenario(scenario)
    }
  }
}
