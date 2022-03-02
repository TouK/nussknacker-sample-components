package pl.touk.nussknacker.sample

import cats.data.Validated.Valid
import cats.data.ValidatedNel
import cats.{Id, Monad, catsInstancesForId}
import com.typesafe.config.ConfigFactory
import pl.touk.nussknacker.engine.Interpreter.InterpreterShape
import pl.touk.nussknacker.engine.Interpreter.InterpreterShape.transform
import pl.touk.nussknacker.engine.api._
import pl.touk.nussknacker.engine.api.process._
import pl.touk.nussknacker.engine.graph.EspProcess
import pl.touk.nussknacker.engine.lite.ScenarioInterpreterFactory
import pl.touk.nussknacker.engine.lite.api.commonTypes.{ErrorType, ResultType}
import pl.touk.nussknacker.engine.lite.api.customComponentTypes.{CapabilityTransformer, CustomComponentContext, LiteSource}
import pl.touk.nussknacker.engine.lite.api.interpreterTypes.{EndResult, ScenarioInputBatch}
import pl.touk.nussknacker.engine.lite.api.runtimecontext.LiteEngineRuntimeContextPreparer
import pl.touk.nussknacker.engine.lite.api.utils.sinks.LazyParamSink
import pl.touk.nussknacker.engine.lite.capabilities.FixedCapabilityTransformer
import pl.touk.nussknacker.engine.resultcollector.ProductionServiceInvocationCollector
import pl.touk.nussknacker.engine.testing.LocalModelData
import pl.touk.nussknacker.engine.util.SynchronousExecutionContext

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/*
  Id based engine
 */
object sample {

  case object SourceFailure extends Exception("Source failure")

  case class SampleInput(contextId: String, value: Int)

  type StringKeyMap[Value] = Map[String, Value]

  val modelData: LocalModelData = LocalModelData(ConfigFactory.empty(), MinimalConfigCreator)

  implicit val ec: ExecutionContext = SynchronousExecutionContext.ctx
  implicit val capabilityTransformer: CapabilityTransformer[Id] = new FixedCapabilityTransformer[Id]
  implicit val futureShape: InterpreterShape[Id] = new InterpreterShape[Id] {
    override def monad: Monad[Id] = Monad[Id]

    override def fromFuture[T](implicit ec: ExecutionContext): Future[T] => Id[Either[T, Throwable]] = Monad[Id].map(_)(transform(_).value.get.get)
  }

  def run(scenario: EspProcess, data: ScenarioInputBatch[SampleInput], runtimeContextPreparer: LiteEngineRuntimeContextPreparer = LiteEngineRuntimeContextPreparer.noOp): ResultType[EndResult[AnyRef]] = {

    val interpreter = ScenarioInterpreterFactory
      .createInterpreter[Id, SampleInput, AnyRef](scenario, modelData, Nil, ProductionServiceInvocationCollector, ComponentUseCase.EngineRuntime)
      .fold(errors => throw new IllegalArgumentException(errors.toString()), identity)
    interpreter.open(runtimeContextPreparer.prepare(JobData(scenario.metaData, ProcessVersion.empty)))
    val value: Id[ResultType[EndResult[AnyRef]]] = interpreter.invoke(data)
    value
  }

  object MinimalConfigCreator extends EmptyProcessConfigCreator {
    override def sourceFactories(processObjectDependencies: ProcessObjectDependencies): Map[String, WithCategories[SourceFactory]] =
      Map(
        "startSource" -> WithCategories(SimpleSourceFactory)
      )

    override def sinkFactories(processObjectDependencies: ProcessObjectDependencies): Map[String, WithCategories[SinkFactory]] =
      Map("end" -> WithCategories(SimpleSinkFactory))

  }

  object SimpleSourceFactory extends SourceFactory {

    @MethodToInvoke
    def create(): Source = new LiteSource[SampleInput] {
      override def createTransformation[F[_] : Monad](evaluateLazyParameter: CustomComponentContext[F]): SampleInput => ValidatedNel[ErrorType, Context] =
        input => Valid(Context(input.contextId, Map("input" -> input.value), None))
    }
  }

  object SimpleSinkFactory extends SinkFactory {
    @MethodToInvoke
    def create(@ParamName("value") value: LazyParameter[AnyRef]): LazyParamSink[AnyRef] = (_: LazyParameterInterpreter) => value
  }
}
