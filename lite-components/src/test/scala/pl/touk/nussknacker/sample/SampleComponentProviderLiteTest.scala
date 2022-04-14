package pl.touk.nussknacker.sample

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.Inside.inside
import org.scalatest.{FunSuite, Matchers}
import org.scalatestplus.junit.JUnitRunner
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.lite.util.test.LiteTestScenarioRunner
import pl.touk.nussknacker.engine.spel.Implicits._

//to run scalatest with gradle use JUnitRunner
@RunWith(classOf[JUnitRunner])
class SampleComponentProviderLiteTest extends FunSuite with Matchers {

  private case class SimpleInput(length: Int)

  test("should test sample component provider on lite interpreter") {

    val totalLength = 5
    val inputData = (0 until totalLength).map(SimpleInput(_: Int)).toList

    val scenario =
      ScenarioBuilder
        .streamingLite("sample_notification")
        .source("custom-source-node-name", LiteTestScenarioRunner.sourceName)
        .enricher("component-provider-service-node-name", "out1", "randomString", "length" -> "#input.length")
        .emptySink("end", LiteTestScenarioRunner.sinkName, "value" -> "#out1")


    val runner = LiteTestScenarioRunner(Nil, ConfigFactory.empty())

    val results = runner.runWithData[SimpleInput, String](scenario, inputData)

    inside(results) { case list =>
      list should have length totalLength
      list.zipWithIndex.foreach {
        case (generated, expectedLength) => generated should have length expectedLength
      }
    }

  }
}
