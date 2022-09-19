package pl.touk.nussknacker.sample.csv

import pl.touk.nussknacker.engine.api.process.SourceFactory

import java.io.File
import java.time.Duration

object CallDetailRecordSourceFactory {
  def prepare(filesDir: String, separator: Char): SourceFactory = {
    import org.apache.flink.api.scala.createTypeInformation

    new SpecificRecordCsvSourceFactory[CallDetailRecord](new File(filesDir), separator, CallDetailRecord.fromFields, _.callStartTime)
  }
}

case class CallDetailRecord(phoneNumberA: String, phoneNumberB: String, callDuration: Duration, callStartTime: Long)

object CallDetailRecord {
  def fromFields(fields: Array[String]): CallDetailRecord = {
    CallDetailRecord(
      phoneNumberA = fields(0),
      phoneNumberB = fields(1),
      callDuration = Duration.ofSeconds(fields(2).toLong),
      callStartTime = fields(3).toLong,
    )
  }
}
