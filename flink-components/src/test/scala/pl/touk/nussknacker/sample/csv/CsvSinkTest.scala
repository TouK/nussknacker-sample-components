package pl.touk.nussknacker.sample.csv

import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatestplus.junit.JUnitRunner
import pl.touk.nussknacker.engine.api.ProcessVersion
import pl.touk.nussknacker.engine.api.component.ComponentDefinition
import pl.touk.nussknacker.engine.api.process.{EmptyProcessConfigCreator, SourceFactory}
import pl.touk.nussknacker.engine.api.typed.typing.Typed
import pl.touk.nussknacker.engine.build.ScenarioBuilder
import pl.touk.nussknacker.engine.deployment.DeploymentData
import pl.touk.nussknacker.engine.flink.util.source.CollectionSource
import pl.touk.nussknacker.engine.process.ExecutionConfigPreparer
import pl.touk.nussknacker.engine.process.compiler.FlinkProcessCompilerWithTestComponents
import pl.touk.nussknacker.engine.process.registrar.FlinkProcessRegistrar
import pl.touk.nussknacker.engine.spel.Implicits.asSpelExpression
import pl.touk.nussknacker.engine.testing.LocalModelData
import pl.touk.nussknacker.engine.testmode.TestComponentsHolder
import pl.touk.nussknacker.sample.FlinkSampleComponentsBaseTest

import java.nio.file.Files
import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class CsvSinkTest extends FunSuite with FlinkSampleComponentsBaseTest with Matchers {

  test("should save rows to file") {
    val resultsFile = Files.createTempFile("test", ".csv")
    resultsFile.toFile.deleteOnExit()
    val modelData = LocalModelData(config, new EmptyProcessConfigCreator())
    val collectionSource = new CollectionSource[String](List("first", "second"), None, Typed[String])
    val testComponentHolder = TestComponentsHolder.registerTestComponents(List(ComponentDefinition("source", SourceFactory.noParam[String](collectionSource))))
    val registrar = FlinkProcessRegistrar(new FlinkProcessCompilerWithTestComponents(testComponentHolder, modelData), ExecutionConfigPreparer.unOptimizedChain(modelData))
    val env = flinkMiniCluster.createExecutionEnvironment()
    val scenario = ScenarioBuilder
      .streaming("test scenario")
      .source("source", "source")
      .emptySink("end", "csvSink", "fileName" -> s"'${resultsFile.getFileName.toFile}'",  "row" -> "{#input, 'const'}")

    registrar.register(new StreamExecutionEnvironment(env), scenario, ProcessVersion.empty, DeploymentData.empty)
    env.executeAndWaitForFinished(scenario.id)()

    Files.readAllLines(resultsFile).asScala.toList shouldBe List("first;const", "second;const")
  }
}
