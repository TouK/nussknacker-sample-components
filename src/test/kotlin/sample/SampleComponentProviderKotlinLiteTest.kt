//package sample
//
//import cats.data.NonEmptyList
//import cats.data.Validated
//import com.typesafe.config.Config
//import com.typesafe.config.ConfigFactory
//import com.typesafe.config.ConfigValueFactory.fromAnyRef
//import io.dropwizard.metrics5.MetricRegistry
//import org.apache.kafka.clients.consumer.ConsumerRecord
//import org.apache.kafka.clients.producer.ProducerRecord
//import org.junit.jupiter.api.Test
//import org.scalatest.time.*
//import pl.touk.nussknacker.engine.api.JobData
//import pl.touk.nussknacker.engine.api.ProcessVersion
//import pl.touk.nussknacker.engine.api.context.ProcessCompilationError
//import pl.touk.nussknacker.engine.api.process.ComponentUseCase
//import pl.touk.nussknacker.engine.api.process.`ComponentUseCase$`
//import pl.touk.nussknacker.engine.build.EspProcessBuilder
//import pl.touk.nussknacker.engine.flink.test.*
//import pl.touk.nussknacker.engine.graph.EspProcess
//import pl.touk.nussknacker.engine.graph.expression
//import pl.touk.nussknacker.engine.lite.ScenarioInterpreterFactory
//import pl.touk.nussknacker.engine.lite.api.interpreterTypes
//import pl.touk.nussknacker.engine.lite.api.runtimecontext.LiteEngineRuntimeContextPreparer
//import pl.touk.nussknacker.engine.lite.kafka.KafkaTransactionalScenarioInterpreter
//import pl.touk.nussknacker.engine.lite.kafka.LiteKafkaJobData
//import pl.touk.nussknacker.engine.lite.metrics.dropwizard.DropwizardMetricsProviderFactory
//import pl.touk.nussknacker.engine.migration.ProcessMigrations
//import pl.touk.nussknacker.engine.modelconfig.DefaultModelConfigLoader
//import pl.touk.nussknacker.engine.resultcollector.`ProductionServiceInvocationCollector$`
//import pl.touk.nussknacker.engine.testing.LocalModelData
//import pl.touk.nussknacker.engine.util.loader.ModelClassLoader
//import pl.touk.nussknacker.engine.util.namespaces.`DefaultNamespacedObjectNaming$`
//import pl.touk.nussknacker.engine.util.process.EmptyProcessConfigCreator
//import scala.Tuple2
//import scala.collection.immutable.`List$`
//import scala.collection.immutable.`Nil$`
//import java.util.concurrent.Future
//
//typealias Input = ConsumerRecord<Array<Byte>, Array<Byte>>
//typealias Output = ProducerRecord<Array<Byte>, Array<Byte>>
//
//class SampleComponentProviderKotlinLiteTest {
//
//
//    @Test
//    fun `should test sample component provider`() {
//        val processorParams = `List$`.`MODULE$`.empty<Tuple2<String, expression.Expression>>()
//        val processorEndParams = `List$`.`MODULE$`.empty<Tuple2<String, expression.Expression>>()
//        val empty = `List$`.`MODULE$`.empty<Tuple2<String, expression.Expression>>()
//        val process = EspProcessBuilder
//            .id("sample_notification")
//            .parallelism(1)
//            .source(
//                "custom-source-node-name", "source", empty
//            )
//            .processor("component-provider-service-node-name", "randomString", processorParams)
//            .processorEnd("custom-sink-node-name", "mockService", processorEndParams)
//
//        runScenarioWithoutErrors(process)
//
//
//    }
//
//    private val metricRegistry = MetricRegistry()
//    private val preparer = LiteEngineRuntimeContextPreparer(DropwizardMetricsProviderFactory(metricRegistry))
//
//    val defaultConfig = ConfigFactory.empty()
//
//    fun runScenarioWithoutErrors(scenario: EspProcess, config: Config = defaultConfig) {
//        val jobData = JobData(scenario.metaData(), ProcessVersion.empty())
//        val liteKafkaJobData = LiteKafkaJobData(1)
//        val configToUse = adjustConfig("fixture.errorTopic", config)
//        val modelDataToUse = modelData(configToUse)
//        val interpreter =
//            ScenarioInterpreterFactory
//                .createInterpreter<scala.concurrent.Future<String>, ConsumerRecord<Array<Byte>, Array<Byte>>, ProducerRecord<Array<Byte>, Array<Byte>>>(
//                    scenario,
//                    modelDataToUse,
//                    `Nil$`.empty(),
//                    `ProductionServiceInvocationCollector$`.`MODULE$`,
//                    ComponentUseCase.`EngineRuntime$`.`MODULE$`,
//
//                )
//        KafkaTransactionalScenarioInterpreter(
//            interpreter.toOption().get(),
//            scenario,
//            jobData,
//            liteKafkaJobData,
//            modelDataToUse,
//            preparer
//        )
//    }
//
//    fun adjustConfig(errorTopic: String, config: Config) = config
//        .withValue("components.kafkaSources.enabled", fromAnyRef(true))
//        .withValue("kafka.\"auto.offset.reset\"", fromAnyRef("earliest"))
//        .withValue("exceptionHandlingConfig.topic", fromAnyRef(errorTopic))
//        .withValue("waitAfterFailureDelay", fromAnyRef("1 millis"))
//
//    fun modelData(config: Config): LocalModelData = LocalModelData.apply(
//        config,
//        EmptyProcessConfigCreator(),
//        ProcessMigrations.empty(),
//        DefaultModelConfigLoader(),
//        ModelClassLoader.empty(),
//        `DefaultNamespacedObjectNaming$`.`MODULE$`
//    )
//
//
//}
//
