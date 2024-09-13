package pl.touk.nussknacker.sample.csv

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.datastream.DataStreamSource
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import pl.touk.nussknacker.engine.flink.api.process.{FlinkCustomNodeContext, StandardFlinkSource, StandardFlinkSourceFunctionUtils}
import pl.touk.nussknacker.engine.flink.api.timestampwatermark.{StandardTimestampWatermarkHandler, TimestampWatermarkHandler}

import java.io.File
import java.time.Duration

class CsvSource[T: TypeInformation](file: File,
                                    separator: Char,
                                    createRecord: Array[String] => T,
                                    extractTimestamp: SerializableTimestampAssigner[T])
  extends StandardFlinkSource[T] {

  override def timestampAssigner: Option[TimestampWatermarkHandler[T]] = {
    Some(StandardTimestampWatermarkHandler.boundedOutOfOrderness(Some(extractTimestamp), maxOutOfOrderness = Duration.ofMinutes(10), None))
  }

  override protected def sourceStream(env: StreamExecutionEnvironment, flinkNodeContext: FlinkCustomNodeContext): DataStreamSource[T] =
    StandardFlinkSourceFunctionUtils.createSourceStream(env, new CsvSourceFunction[T](file, separator, createRecord), implicitly[TypeInformation[T]])
}
