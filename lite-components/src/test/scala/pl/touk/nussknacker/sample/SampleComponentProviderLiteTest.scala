package pl.touk.nussknacker.sample

import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatestplus.junit.JUnitRunner
import pl.touk.nussknacker.engine.build.StreamingLiteScenarioBuilder
import pl.touk.nussknacker.engine.lite.api.interpreterTypes.{ScenarioInputBatch, SourceId}
import pl.touk.nussknacker.engine.spel.Implicits._
import pl.touk.nussknacker.sample.sample.SampleInput

//to run scalatest with gradle use JUnitRunner
@RunWith(classOf[JUnitRunner])
class SampleComponentProviderLiteTest extends FunSuite with Matchers {

  test("should test sample component provider on lite interpreter") {
    val process =
      StreamingLiteScenarioBuilder
        .id("sample_notification")
        .parallelism(1)
        .source("custom-source-node-name", "startSource")
        .processor("component-provider-service-node-name", "randomString", "length" -> "12")
        .emptySink("end", "end", "value" -> "#input")

    val results = sample.run(process,
      ScenarioInputBatch(List(0).zipWithIndex.map { case (value, idx) =>
        (SourceId("startSource"), SampleInput(idx.toString, value))
      })

    )
    val res = results.value.map(_.result)
    println(results.run._1)
  }
}
