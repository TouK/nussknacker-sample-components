package pl.touk.nussknacker.sample.csv

import org.apache.flink.streaming.api.functions.sink.SinkFunction

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, StandardOpenOption}
import java.util
import scala.collection.JavaConverters._

class CsvSinkFunction(file: File, separator: Char) extends SinkFunction[java.util.List[String]] {

  override def invoke(value: util.List[String], context: SinkFunction.Context): Unit = {
    val row = value.asScala.mkString(separator.toString)
    Files.write(file.toPath, (row + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND)
  }
}
