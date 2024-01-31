package pl.touk.nussknacker.sample

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Test
import org.scalatest.matchers.should.Matchers
import pl.touk.nussknacker.engine.api.component.ComponentDefinition
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.lite.util.test.LiteTestScenarioRunner
import pl.touk.nussknacker.engine.spel.Implicits._
import pl.touk.nussknacker.engine.util.test.TestScenarioRunner
import pl.touk.nussknacker.test.ValidatedValuesDetailedMessage

class SampleComponentProviderLiteTest extends Matchers with ValidatedValuesDetailedMessage {

  import LiteTestScenarioRunner._

  private case class SimpleInput(length: Int)

  @Test
  def testSampleComponentProviderWithLiteInterpreter(): Unit = {

    val totalLength = 5
    val inputData = (0 until totalLength).map(SimpleInput(_: Int)).toList

    val scenario =
      ScenarioBuilder
        .streamingLite("sample_notification")
        .source("custom-source-node-name", TestScenarioRunner.testDataSource)
        .enricher("component-provider-service-node-name", "out1", "randomString", "length" -> "#input.length")
        .emptySink("end", TestScenarioRunner.testResultSink, "value" -> "#out1")


    val runner = TestScenarioRunner
      .liteBased()
      .withExtraComponents(ComponentDefinition("randomString", new RandomStringProvider) :: Nil)
      .build()

    val results = runner.runWithData[SimpleInput, String](scenario, inputData)

    val validResults = results.validValue
    validResults.successes should have length totalLength
    validResults.successes.zipWithIndex.foreach {
      case (generated, expectedLength) => generated should have length expectedLength
    }
  }

}
