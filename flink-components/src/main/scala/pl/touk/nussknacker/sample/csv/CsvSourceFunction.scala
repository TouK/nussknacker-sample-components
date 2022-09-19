package pl.touk.nussknacker.sample.csv

import org.apache.flink.streaming.api.functions.source.SourceFunction

import java.io.File
import scala.io.Source
import scala.util.Using

/**
 * Very simple CSV source function that exits after reading all lines in the file.
 */
class CsvSourceFunction[T](file: File,
                           separator: Char,
                           createRecord: Array[String] => T) extends SourceFunction[T] {

  @transient
  @volatile private var running: Boolean = _

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
      }
    }
  }

  override def cancel(): Unit = {
    running = false
  }
}
