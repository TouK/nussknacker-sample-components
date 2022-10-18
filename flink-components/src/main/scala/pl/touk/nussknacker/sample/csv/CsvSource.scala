package pl.touk.nussknacker.sample.csv

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.source.SourceFunction
import pl.touk.nussknacker.engine.flink.api.process.BasicFlinkSource
import pl.touk.nussknacker.engine.flink.api.timestampwatermark.{StandardTimestampWatermarkHandler, TimestampWatermarkHandler}

import java.io.File
import java.time.Duration

class CsvSource[T: TypeInformation](file: File,
                                    separator: Char,
                                    createRecord: Array[String] => T,
                                    extractTimestamp: SerializableTimestampAssigner[T]) extends BasicFlinkSource[T] {

  override def flinkSourceFunction: SourceFunction[T] = {
    new CsvSourceFunction[T](file, separator, createRecord)
  }

  override def typeInformation: TypeInformation[T] = {
    implicitly[TypeInformation[T]]
  }

  override def timestampAssigner: Option[TimestampWatermarkHandler[T]] = {
    Some(StandardTimestampWatermarkHandler.boundedOutOfOrderness(extractTimestamp, maxOutOfOrderness = Duration.ofMinutes(10)))
  }
}
