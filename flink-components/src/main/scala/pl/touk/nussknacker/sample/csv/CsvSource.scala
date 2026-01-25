package pl.touk.nussknacker.sample.csv

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.connector.source.{Boundedness, ReaderOutput, SourceReader, SourceReaderContext}
import org.apache.flink.core.io.InputStatus
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import pl.touk.nussknacker.engine.api.Context
import pl.touk.nussknacker.engine.flink.api.process.{CustomizableContextInitializerSource, FlinkContextInitializingFunction, FlinkCustomNodeContext, FlinkSource}
import pl.touk.nussknacker.engine.flink.api.timestampwatermark.WatermarkStrategyUtils
import pl.touk.nussknacker.engine.flink.util.source.SingleSplitSource

import java.io.File
import java.time.Duration
import scala.io.Source

/**
 * Very simple CSV source that exits after reading all lines in the file.
 */
class CsvSource[T: TypeInformation](file: File,
                                    separator: Char,
                                    createRecord: Array[String] => T,
                                    timestampAssigner: SerializableTimestampAssigner[T])
  extends FlinkSource with CustomizableContextInitializerSource[T] {

  override def contextStream(env: StreamExecutionEnvironment, flinkNodeContext: FlinkCustomNodeContext): DataStream[Context] =
    env
      .fromSource(
        flinkSource(file, separator, createRecord),
        WatermarkStrategyUtils.boundedOutOfOrderness(timestampAssigner, maxOutOfOrderness = Duration.ofMinutes(10)),
        flinkNodeContext.nodeId.id,
        implicitly[TypeInformation[T]]
      )
      .uid(flinkNodeContext.nodeId.id)
      .map(
        new FlinkContextInitializingFunction(
          contextInitializer,
          flinkNodeContext.nodeId,
          flinkNodeContext.convertToEngineRuntimeContext
        ),
        flinkNodeContext.contextTypeInfo
      )

  private def flinkSource(file: File,
                          separator: Char,
                          createRecord: Array[String] => T): SingleSplitSource[T] = new SingleSplitSource[T] {
    @transient
    private var fileSource: Source = _
    @transient
    private var fileSourceIterator: Iterator[String] = _

    override def getBoundedness: Boundedness = Boundedness.CONTINUOUS_UNBOUNDED

    override def createReader(sourceReaderContext: SourceReaderContext): SourceReader[T, SingleSplitSource.SingleSplit] =
      new SingleSplitSource.Reader[T] {

        override def pollNext(readerOutput: ReaderOutput[T]): InputStatus = {
          if (fileSourceIterator.hasNext) {
            val line = fileSourceIterator.next()
            if (!line.isBlank) {
              val fields = line.split(separator)
              val record = createRecord(fields)
              readerOutput.collect(record)
            }
          }
          if (fileSourceIterator.hasNext) InputStatus.MORE_AVAILABLE
          else InputStatus.END_OF_INPUT
        }

        override def start(): Unit = {
          fileSource = Source.fromFile(file)
          fileSourceIterator = fileSource.getLines()
        }

        override def close(): Unit = {
          fileSourceIterator = null
          fileSource.close()
        }
      }
  }

}
