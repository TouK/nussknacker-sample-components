package pl.touk.nussknacker.sample.csv

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.source.SourceFunction
import pl.touk.nussknacker.engine.api.process.SourceFactory
import pl.touk.nussknacker.engine.api.typed.CustomNodeValidationException
import pl.touk.nussknacker.engine.api.{MethodToInvoke, ParamName}
import pl.touk.nussknacker.engine.flink.api.process.{BasicFlinkSource, FlinkSource}
import pl.touk.nussknacker.engine.flink.api.timestampwatermark.StandardTimestampWatermarkHandler.toAssigner
import pl.touk.nussknacker.engine.flink.api.timestampwatermark.{StandardTimestampWatermarkHandler, TimestampWatermarkHandler}

import java.io.File
import java.time.Duration
import scala.io.Source
import scala.util.Using

class SpecificRecordCsvFactory[T: TypeInformation](filesDir: File,
                                                   separator: Char,
                                                   createRecord: Array[String] => T,
                                                   extractTimestamp: T => Long) extends SourceFactory {

  @MethodToInvoke
  def create(@ParamName("fileName") fileName: String): FlinkSource = {
    val file = new File(filesDir, fileName)
    if (!file.canRead) {
      throw CustomNodeValidationException(s"File: '$file' is not readable", paramName = Some("fileName"))
    }
    new SpecificRecordFlinkCsvSource[T](file, separator, createRecord, toAssigner(extractTimestamp(_)))
  }

}

class SpecificRecordFlinkCsvSource[T: TypeInformation](file: File,
                                                       separator: Char,
                                                       createRecord: Array[String] => T,
                                                       extractTimestamp: SerializableTimestampAssigner[T]) extends BasicFlinkSource[T] {

  override def flinkSourceFunction: SourceFunction[T] = {
    new SpecificRecordCsvSourceFunction[T](file, separator, createRecord)
  }

  override def typeInformation: TypeInformation[T] = {
    implicitly[TypeInformation[T]]
  }

  override def timestampAssigner: Option[TimestampWatermarkHandler[T]] = {
    Some(StandardTimestampWatermarkHandler.boundedOutOfOrderness(extractTimestamp, maxOutOfOrderness = Duration.ofMinutes(10)))
  }
}

class SpecificRecordCsvSourceFunction[T](file: File,
                                         separator: Char,
                                         createRecord: Array[String] => T) extends SourceFunction[T] {

  @transient @volatile private var running: Boolean = _

  override def run(ctx: SourceFunction.SourceContext[T]): Unit = {
    running = true
    Using.resource(Source.fromFile(file)) { fileSource =>
      val linesIt = fileSource.getLines()
      while (running && linesIt.hasNext) {
        val line = linesIt.next()
        if (!line.isBlank) {
          val fields = line.split(separator)
          val record = createRecord(fields)
          ctx.collect(record)
        }
        // wait for checkpoint?
      }
    }
  }

  override def cancel(): Unit = {
    running = false
  }
}
