package pl.touk.nussknacker.sample.csv

import org.junit.jupiter.api.Test
import org.scalatest.matchers.should.Matchers
import pl.touk.nussknacker.engine.api.component.ComponentDefinition
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.flink.util.test.FlinkTestScenarioRunner._
import pl.touk.nussknacker.engine.spel.SpelExtension._
import pl.touk.nussknacker.engine.util.test.TestScenarioRunner
import pl.touk.nussknacker.sample.FlinkSampleComponentsBaseClassTest

import java.nio.file.Files
import scala.collection.JavaConverters._

class CsvSinkTest extends Matchers {

  import CsvSinkTest._

  @Test
  def shouldSaveRowsToFile(): Unit = {
    val resultsFile = Files.createTempFile("test", ".csv")
    resultsFile.toFile.deleteOnExit()
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("source", TestScenarioRunner.testDataSource)
      .emptySink("end", "csvSink", "fileName" -> s"'${resultsFile.getFileName.toFile}'".spel,  "row" -> "{#input, 'const'}".spel)
    val runner = TestScenarioRunner
      .flinkBased(config, flinkMiniCluster)
      .withExtraComponents(ComponentDefinition("csvSink", new CsvSinkFactory(resultsFile.getParent.toString, ';')) :: Nil)
      .build()

    runner.runWithDataIgnoringResults(scenario, List("first", "second"))

    Files.readAllLines(resultsFile).asScala.toList shouldBe List("first;const", "second;const")
  }
}

object CsvSinkTest extends FlinkSampleComponentsBaseClassTest
