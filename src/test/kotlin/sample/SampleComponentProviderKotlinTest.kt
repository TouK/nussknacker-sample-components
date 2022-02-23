package sample

import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.junit.jupiter.api.Test
import org.scalatest.concurrent.AbstractPatienceConfiguration
import org.scalatest.time.*
import pl.touk.nussknacker.engine.api.ProcessVersion
import pl.touk.nussknacker.engine.build.EspProcessBuilder
import pl.touk.nussknacker.engine.deployment.DeploymentData
import pl.touk.nussknacker.engine.flink.test.*
import pl.touk.nussknacker.engine.graph.EspProcess
import pl.touk.nussknacker.engine.graph.expression
import pl.touk.nussknacker.engine.migration.ProcessMigrations
import pl.touk.nussknacker.engine.modelconfig.DefaultModelConfigLoader
import pl.touk.nussknacker.engine.process.ExecutionConfigPreparer
import pl.touk.nussknacker.engine.process.compiler.FlinkProcessCompiler
import pl.touk.nussknacker.engine.process.helpers.BaseSampleConfigCreator
import pl.touk.nussknacker.engine.process.helpers.SampleNodes
import pl.touk.nussknacker.engine.process.registrar.FlinkProcessRegistrar
import pl.touk.nussknacker.engine.testing.LocalModelData
import pl.touk.nussknacker.engine.util.loader.ModelClassLoader
import pl.touk.nussknacker.engine.util.namespaces.DefaultNamespacedObjectNaming
import pl.touk.nussknacker.engine.util.namespaces.`DefaultNamespacedObjectNaming$`
import scala.None
import scala.Tuple2
import scala.`None$`
import scala.collection.Seq
import scala.collection.immutable.`List$`
import scala.reflect.`ClassTag$`
import scala.reflect.api.TypeTags
import kotlin.reflect.KClass

class SampleComponentProviderKotlinTest {

    @Test
    fun `should test sample component provider`() {
        val processorParams = `List$`.`MODULE$`.empty<Tuple2<String, expression.Expression>>()
        val processorEndParams = `List$`.`MODULE$`.empty<Tuple2<String, expression.Expression>>()
        val empty = `List$`.`MODULE$`.empty<Tuple2<String, expression.Expression>>()
        val process = EspProcessBuilder
            .id("sample_notification")
            .parallelism(1)
            .source("custom-source-node-name", "source", empty
            )
            .processor("component-provider-service-node-name", "randomString", processorParams)
        .processorEnd("custom-sink-node-name", "mockService", processorEndParams)

//        run(process, listOf("sdf"))
//        SampleNodes.MockService.data()
    }

//    val flinkMiniCluster = `FlinkMiniClusterHolder$`.`MODULE$`.apply( FlinkTestConfiguration.configuration(2, 8),
//        FlinkMiniClusterHolder.AdditionalEnvironmentConfig(true,
//            AbstractPatienceConfiguration.PatienceConfig.apply(Span.Max(), Span.Max())
//        )
//    )

//    private val registrar: FlinkProcessRegistrar
     val config = ConfigFactory.empty()

//    private fun run(process: EspProcess, data: List<String>) {
//        val loadedConfig = DefaultModelConfigLoader().resolveInputConfigDuringExecution(config, this.javaClass.classLoader)
//        val sourceList: scala.collection.immutable.List<String> = `List$`.`MODULE$`.newBuilder<String>().`$plus$eq`("").result()
//        val baseSampleConfigCreator =
//            BaseSampleConfigCreator(sourceList,`ClassTag$`.`MODULE$`.apply(String::class.java), TypeTags.`TypeTag$`.`MODULE$`.Unit(), TypeInformation.of(String::class.java))
//        val modelData = LocalModelData.apply(loadedConfig.config(), baseSampleConfigCreator, ProcessMigrations.empty(),DefaultModelConfigLoader(), ModelClassLoader.empty(), `DefaultNamespacedObjectNaming$`.`MODULE$`)
////        val registrar = FlinkProcessRegistrar( FlinkProcessCompiler(modelData), ExecutionConfigPreparer.unOptimizedChain(modelData))
//
//        val env = flinkMiniCluster.createExecutionEnvironment()!!
////        registrar.register(StreamExecutionEnvironment(env), process, ProcessVersion.empty(), DeploymentData.empty(), `None$`.empty())
//        env.withJobRunning(process.id()) {}
//    }

}