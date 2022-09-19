package pl.touk.nussknacker.sample

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.BeforeAndAfterAll
import pl.touk.nussknacker.engine.api.ProcessVersion
import pl.touk.nussknacker.engine.api.process.EmptyProcessConfigCreator
import pl.touk.nussknacker.engine.deployment.DeploymentData
import pl.touk.nussknacker.engine.flink.test.MiniClusterExecutionEnvironment
import pl.touk.nussknacker.engine.graph.EspProcess
import pl.touk.nussknacker.engine.process.ExecutionConfigPreparer
import pl.touk.nussknacker.engine.process.compiler.FlinkProcessCompilerWithTestComponents
import pl.touk.nussknacker.engine.process.registrar.FlinkProcessRegistrar
import pl.touk.nussknacker.engine.testing.LocalModelData
import pl.touk.nussknacker.engine.testmode.{TestComponentHolder, TestComponentsHolder}

abstract class BaseSourceTest extends FlinkSampleComponentsBaseTest with BeforeAndAfterAll {

  private var testComponentHolder: TestComponentHolder = _
  protected var registrar: FlinkProcessRegistrar = _
  protected var env: MiniClusterExecutionEnvironment = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    val modelData = LocalModelData(config, new EmptyProcessConfigCreator())
    testComponentHolder = TestComponentsHolder.registerTestComponents(Nil)
    registrar = FlinkProcessRegistrar(new FlinkProcessCompilerWithTestComponents(testComponentHolder, modelData), ExecutionConfigPreparer.unOptimizedChain(modelData))
    env = flinkMiniCluster.createExecutionEnvironment()
  }

  protected def registerScenario(scenario: EspProcess): Unit = {
    registrar.register(new StreamExecutionEnvironment(env), scenario, ProcessVersion.empty, DeploymentData.empty)
  }

  protected def invocationCollectorResults[T](): List[T] = {
    testComponentHolder.results(testComponentHolder.runId)
  }
}
