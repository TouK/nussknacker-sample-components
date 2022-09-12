package pl.touk.nussknacker.sample

import com.typesafe.config.{Config, ConfigFactory}
import org.junit.jupiter.api.{AfterAll, BeforeAll, Test}
import org.scalatest.Inside.inside
import org.scalatest.Suite
import org.scalatest.matchers.should.Matchers
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.flink.test.FlinkSpec
import pl.touk.nussknacker.engine.flink.util.test.NuTestScenarioRunner
import pl.touk.nussknacker.engine.spel.Implicits._
import pl.touk.nussknacker.test.ValidatedValuesDetailedMessage

class SampleComponentProviderTest extends Matchers with ValidatedValuesDetailedMessage {

  import pl.touk.nussknacker.sample.SampleComponentProviderTest._

  @Test
  def testSampleComponentProviderWithLiteInterpreter(): Unit = {
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("custom-source-node-name", "source")
      .enricher("component-provider-service-node-name", "output", "randomString", "length" -> "#input")
      .processorEnd("end", "invocationCollector", "value" -> "#output")

    val runner = NuTestScenarioRunner
      .flinkBased(config, flinkMiniCluster)
      .build()

    val length = 5
    val results = runner.runWithData[Int, String](scenario, List(length)).validValue

    inside(results.successes) { case data :: Nil =>
      data should have length length
    }
  }

}

object SampleComponentProviderTest extends Suite with FlinkSpec {

  override protected lazy val config: Config = ConfigFactory.empty()

  @BeforeAll
  def init(): Unit = {
    beforeAll()
  }

  @AfterAll
  def tearDown(): Unit = {
    afterAll()
  }

}