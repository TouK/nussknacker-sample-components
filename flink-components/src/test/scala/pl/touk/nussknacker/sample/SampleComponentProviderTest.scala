package pl.touk.nussknacker.sample

import com.typesafe.config.ConfigFactory
import org.apache.flink.streaming.api.scala
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatestplus.junit.JUnitRunner
import pl.touk.nussknacker.engine.api.ProcessVersion
import pl.touk.nussknacker.engine.build.EspProcessBuilder
import pl.touk.nussknacker.engine.deployment.DeploymentData
import pl.touk.nussknacker.engine.flink.test.FlinkSpec
import pl.touk.nussknacker.engine.graph.EspProcess
import pl.touk.nussknacker.engine.modelconfig.DefaultModelConfigLoader
import pl.touk.nussknacker.engine.process.ExecutionConfigPreparer
import pl.touk.nussknacker.engine.process.compiler.FlinkProcessCompiler
import pl.touk.nussknacker.engine.process.helpers.BaseSampleConfigCreator
import pl.touk.nussknacker.engine.process.helpers.SampleNodes.MockService
import pl.touk.nussknacker.engine.process.registrar.FlinkProcessRegistrar
import pl.touk.nussknacker.engine.spel.Implicits._
import pl.touk.nussknacker.engine.testing.LocalModelData

//to run scalatest with gradle use JUnitRunner
@RunWith(classOf[JUnitRunner])
class SampleComponentProviderTest extends FunSuite with FlinkSpec with Matchers {

  override protected lazy val config = ConfigFactory.empty()

  test("should test sample component on flink runtime") {
    val process =
      EspProcessBuilder
        .id("test scenario")
        .source("custom-source-node-name", "source")
        .processor("component-provider-service-node-name", "randomString", "length" -> "12")
        .processorEnd("custom-sink-node-name", "mockService", "all" -> s"#input")

    run(process, List("mockValue"))
    MockService.data shouldBe (List("mockValue"))
  }

  private var registrar: FlinkProcessRegistrar = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  private def run(process: EspProcess, data: List[String]): Unit = {
    val loadedConfig = new DefaultModelConfigLoader().resolveInputConfigDuringExecution(config, getClass.getClassLoader)
    import org.apache.flink.streaming.api.scala._
    val modelData = LocalModelData(loadedConfig.config, new BaseSampleConfigCreator(data))

    registrar = FlinkProcessRegistrar(new FlinkProcessCompiler(modelData), ExecutionConfigPreparer.unOptimizedChain(modelData))

    val env = flinkMiniCluster.createExecutionEnvironment()
    registrar.register(new scala.StreamExecutionEnvironment(env), process, ProcessVersion.empty, DeploymentData.empty)
    env.withJobRunning(process.id) {}
  }

}

