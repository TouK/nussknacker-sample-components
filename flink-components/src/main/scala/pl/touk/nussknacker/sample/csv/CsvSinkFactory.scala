package pl.touk.nussknacker.sample.csv

import pl.touk.nussknacker.engine.api.{LazyParameter, MethodToInvoke, ParamName}
import pl.touk.nussknacker.engine.api.process.{Sink, SinkFactory}

import java.io.File

class CsvSinkFactory(filesDir: String, separator: Char) extends SinkFactory {

  @MethodToInvoke
  def create(@ParamName("fileName") fileName: String,
             @ParamName("row") row: LazyParameter[java.util.List[String]]): Sink = {
    val file = new File(filesDir, fileName)
    new CsvSink(file, separator, row)
  }
}
