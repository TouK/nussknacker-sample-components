package pl.touk.nussknacker.sample.csv

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.connector.sink2.{Sink, SinkWriter, WriterInitContext}
import pl.touk.nussknacker.engine.api.{Context, LazyParameter, ValueWithContext}
import pl.touk.nussknacker.engine.flink.api.process.{BasicFlinkSink, FlinkCustomNodeContext, FlinkLazyParameterFunctionHelper}

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, StandardOpenOption}
import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

class CsvSink(file: File, separator: Char, row: LazyParameter[java.util.List[String]]) extends BasicFlinkSink {

  override def valueFunction(helper: FlinkLazyParameterFunctionHelper): FlatMapFunction[Context, ValueWithContext[Value]] = {
    helper.lazyMapFunction(row)
  }

  override def toFlinkSink(flinkNodeContext: FlinkCustomNodeContext): Sink[Value] = {
    new Sink[Value] {
      // TODO: Remove after upgrade to Flink 2.x
      @nowarn("cat=deprecation")
      override def createWriter(context: Sink.InitContext): SinkWriter[Value] =
        throw new IllegalAccessException("This method shouldn't be invoked")

      override def createWriter(context: WriterInitContext): SinkWriter[Value] = new SinkWriter[Value] {
        override def write(element: Value, context: SinkWriter.Context): Unit = {
          val row = element.asScala.mkString(separator.toString)
          Files.write(file.toPath, (row + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND)
        }

        override def flush(endOfInput: Boolean): Unit = {}

        override def close(): Unit = {}
      }
    }
  }

  override type Value = java.util.List[String]

}
