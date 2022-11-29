package pl.touk.nussknacker.sample.csv

import cats.data.{Validated, ValidatedNel}
import cats.syntax.apply._
import cats.syntax.traverse._
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner
import org.apache.flink.api.common.typeinfo.TypeInformation
import pl.touk.nussknacker.engine.api.NodeId
import pl.touk.nussknacker.engine.api.context.ProcessCompilationError.CustomNodeError
import pl.touk.nussknacker.engine.api.context.transformation.{DefinedEagerParameter, NodeDependencyValue, SingleInputGenericNodeTransformation}
import pl.touk.nussknacker.engine.api.context.{ProcessCompilationError, ValidationContext}
import pl.touk.nussknacker.engine.api.definition.{NodeDependency, ParameterWithExtractor}
import pl.touk.nussknacker.engine.api.process.{Source, SourceFactory}
import pl.touk.nussknacker.engine.api.typed.TypedMap
import pl.touk.nussknacker.engine.api.typed.typing.{Typed, TypedObjectTypingResult, TypingResult}
import pl.touk.nussknacker.engine.flink.api.timestampwatermark.StandardTimestampWatermarkHandler.toAssigner
import pl.touk.nussknacker.engine.util.typing.TypingUtils
import pl.touk.nussknacker.sample.csv.GenericCsvSourceFactory.{ColumnParsers, DefinitionParameter, FileNameParameter}

import java.io.File
import scala.collection.JavaConverters._

object GenericCsvSourceFactory {
  val FileNameParameter: ParameterWithExtractor[String] = ParameterWithExtractor.mandatory("fileName")
  val DefinitionParameter: ParameterWithExtractor[java.util.List[java.util.List[String]]] = ParameterWithExtractor.mandatory("definition")

  private val ColumnParsers: Map[TypingResult, String => Any] = Map(
    Typed[String] -> identity,
    Typed[java.lang.Long] -> ((s: String) => s.toLong),
  )
}

/**
 * A sample generic CSV source. It has two parameters - fileName and definition. Definition describe columns in the file - names and their types.
 */
class GenericCsvSourceFactory(filesDir: String, separator: Char) extends SourceFactory with SingleInputGenericNodeTransformation[Source] {

  override type State = Nothing

  override def contextTransformation(context: ValidationContext, dependencies: List[NodeDependencyValue])
                                    (implicit nodeId: NodeId): NodeTransformationDefinition = {
    case TransformationStep(Nil, _) =>
      NextParameters(FileNameParameter.parameter :: DefinitionParameter.parameter :: Nil)
    case TransformationStep(
    (FileNameParameter.parameter.name, DefinedEagerParameter(fileName: String, _)) ::
      (DefinitionParameter.parameter.name, DefinedEagerParameter(definition: java.util.List[java.util.List[String]], _)) ::
      Nil, _) =>
      (
        validateFileName(fileName),
        describeInput(definition).andThen(inputTypingResult => context.withVariable("input", inputTypingResult, paramName = None))
        )
        .mapN { case (_, finalContext) => finalContext }
        .fold(
          errors => FinalResults(context, errors.toList),
          finalContext => FinalResults(finalContext)
        )
  }

  override def implementation(params: Map[String, Any], dependencies: List[NodeDependencyValue], finalState: Option[State]): Source = {
    val fileName = FileNameParameter.extractValue(params)
    val file = new File(filesDir, fileName)
    val definition = DefinitionParameter.extractValue(params)
    // For each event, current time is assigned. We could also add a parameter with timestamp column name and assign timestamps
    // based on the given column value.
    val assignProcessingTime: SerializableTimestampAssigner[TypedMap] = toAssigner(_ => System.currentTimeMillis())
    //TODO: what should be here
    new CsvSource[TypedMap](file, separator, createRecordFunction(definition), assignProcessingTime) (TypeInformation.of(classOf[TypedMap]))
  }

  override def nodeDependencies: List[NodeDependency] = Nil

  private def validateFileName(fileName: String)(implicit nodeId: NodeId): ValidatedNel[ProcessCompilationError, Unit] = {
    val file = new File(filesDir, fileName)
    Validated.condNel(file.canRead, (), CustomNodeError(s"File: '$fileName' is not readable", paramName = Some(FileNameParameter.parameter.name)))
  }

  private def describeInput(definition: java.util.List[java.util.List[String]])
                           (implicit nodeId: NodeId): ValidatedNel[ProcessCompilationError, TypingResult] = {
    val validatedDefinitionFormat = definition.asScala.toList.zipWithIndex.map { case (column, idx) =>
      Validated.condNel(
        column.size() == 2,
        (column.get(0), column.get(1)),
        CustomNodeError(s"Column ${idx + 1} should have name and type", Some(DefinitionParameter.parameter.name))
      )
    }.sequence
    validatedDefinitionFormat.map(namesAndTypes => TypingUtils.typeMapDefinition(namesAndTypes.toMap))
      .andThen(ensureDefinitionHasOnlySupportedColumnTypes)
  }

  private def ensureDefinitionHasOnlySupportedColumnTypes(typingResult: TypingResult)
                                                         (implicit nodeId: NodeId): ValidatedNel[ProcessCompilationError, TypingResult] = {
    typingResult.asInstanceOf[TypedObjectTypingResult].fields.map { case (name, typingResult) =>
      Validated.condNel(
        ColumnParsers.contains(typingResult),
        (name, typingResult),
        CustomNodeError(s"Type for column '$name' is not supported", Some(DefinitionParameter.parameter.name))
      )
    }.toList.sequence.map(TypedObjectTypingResult(_))
  }

  private def createRecordFunction(definition: java.util.List[java.util.List[String]]): Array[String] => TypedMap = {
    val columnTransformations: List[(String, String => Any)] = definition.asScala.toList.map { nameAndType =>
      val name = nameAndType.get(0)
      val typ = nameAndType.get(1)
      val transformation: String => Any = typ match {
        case "String" => identity
        case "Long" => _.toLong // It can fail during runtime.
      }
      name -> transformation
    }
    (fields: Array[String]) => TypedMap(fields.zip(columnTransformations).map { case (value, (name, transformation)) =>
      name -> transformation(value)
    }.toMap)
  }
}
