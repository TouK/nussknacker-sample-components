package pl.touk.nussknacker.sample.csv

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import pl.touk.nussknacker.engine.api.{Context, LazyParameter, ValueWithContext}
import pl.touk.nussknacker.engine.flink.api.process.{BasicFlinkSink, FlinkLazyParameterFunctionHelper}

import java.io.File

class CsvSink(file: File, separator: Char, row: LazyParameter[java.util.List[String]]) extends BasicFlinkSink {

  override def valueFunction(helper: FlinkLazyParameterFunctionHelper): FlatMapFunction[Context, ValueWithContext[Value]] = {
    helper.lazyMapFunction(row)
  }

  override def toFlinkFunction: SinkFunction[Value] = {
    new CsvSinkFunction(file, separator)
  }

  override type Value = java.util.List[String]
}
