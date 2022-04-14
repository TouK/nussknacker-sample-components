package pl.touk.nussknacker.sample

import com.typesafe.config.{Config, ConfigFactory}
import org.junit.runner.RunWith
import org.scalatest.Inside.inside
import org.scalatest.{FunSuite, Matchers}
import org.scalatestplus.junit.JUnitRunner
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.flink.test.FlinkSpec
import pl.touk.nussknacker.engine.flink.util.test.NuTestScenarioRunner
import pl.touk.nussknacker.engine.spel.Implicits._

//to run scalatest with gradle use JUnitRunner
@RunWith(classOf[JUnitRunner])
class SampleComponentProviderTest extends FunSuite with FlinkSpec with Matchers {

  override protected lazy val config: Config = ConfigFactory.empty()

  test("should test sample component on flink runtime") {
    val scenario = ScenarioBuilder
        .streaming("test scenario")
        .source("custom-source-node-name", "source")
        .enricher("component-provider-service-node-name", "output", "randomString", "length" -> "#input")
        .processorEnd("end", "invocationCollector", "value" -> "#output")
      
    val runner = NuTestScenarioRunner
      .flinkBased(config, flinkMiniCluster)
      .build()

    val length = 5
    val results = runner.runWithData[Int, String](scenario, List(length))

    inside(results) { case data :: Nil =>
      data should have length length
    }

  }

}

