package pl.touk.nussknacker.sample.csv

import org.apache.flink.api.common.typeinfo.TypeInformation
import pl.touk.nussknacker.engine.api.process.SourceFactory
import pl.touk.nussknacker.engine.api.typed.CustomNodeValidationException
import pl.touk.nussknacker.engine.api.{MethodToInvoke, ParamName}
import pl.touk.nussknacker.engine.flink.api.process.FlinkSource
import pl.touk.nussknacker.engine.flink.api.timestampwatermark.StandardTimestampWatermarkHandler.toAssigner

import java.io.File

class SpecificRecordCsvSourceFactory[T: TypeInformation](filesDir: File,
                                                         separator: Char,
                                                         createRecord: Array[String] => T,
                                                         extractTimestamp: T => Long) extends SourceFactory {

  @MethodToInvoke
  def create(@ParamName("fileName") fileName: String): FlinkSource = {
    val file = new File(filesDir, fileName)
    if (!file.canRead) {
      throw CustomNodeValidationException(s"File: '$file' is not readable", paramName = Some("fileName"))
    }
    new CsvSource[T](file, separator, createRecord, toAssigner(extractTimestamp(_)))
  }

}
