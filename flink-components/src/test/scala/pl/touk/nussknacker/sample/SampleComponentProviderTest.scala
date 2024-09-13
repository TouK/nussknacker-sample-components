package pl.touk.nussknacker.sample

import org.junit.jupiter.api.Test
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers
import pl.touk.nussknacker.engine.api.component.ComponentDefinition
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.flink.util.test.FlinkTestScenarioRunner._
import pl.touk.nussknacker.engine.spel.SpelExtension._
import pl.touk.nussknacker.engine.util.test.TestScenarioRunner
import pl.touk.nussknacker.test.ValidatedValuesDetailedMessage

class SampleComponentProviderTest extends Matchers with ValidatedValuesDetailedMessage {

  import pl.touk.nussknacker.sample.SampleComponentProviderTest._

  @Test
  def testSampleComponentProviderWithLiteInterpreter(): Unit = {
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("custom-source-node-name", TestScenarioRunner.testDataSource)
      .enricher("component-provider-service-node-name", "output", "randomString", "length" -> "#input".spel)
      .emptySink("end", TestScenarioRunner.testResultSink, "value" -> "#output".spel)

    val runner = TestScenarioRunner
      .flinkBased(config, flinkMiniCluster)
      .withExtraComponents(ComponentDefinition("randomString", new RandomStringProvider) :: Nil)
      .build()

    val length = 5
    val results = runner.runWithData[Int, String](scenario, List(length)).validValue

    inside(results.successes) { case data :: Nil =>
      data should have length length
    }
  }

}

object SampleComponentProviderTest extends FlinkSampleComponentsBaseClassTest
