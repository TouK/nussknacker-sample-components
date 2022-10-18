package pl.touk.nussknacker.sample

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import pl.touk.nussknacker.engine.api.component.{ComponentDefinition, ComponentProvider, NussknackerVersion}
import pl.touk.nussknacker.engine.api.process.ProcessObjectDependencies
import pl.touk.nussknacker.sample.csv.{CallDetailRecordSourceFactory, CsvSinkFactory, GenericCsvSourceFactory}

//noinspection ScalaUnusedSymbol
class FlinkSampleComponentProvider extends ComponentProvider {
  override def providerName: String = "flinkSample"

  override def resolveConfigForExecution(config: Config): Config = config

  override def create(config: Config, dependencies: ProcessObjectDependencies): List[ComponentDefinition] = {
    val componentsConfig = config.as[FlinkSampleComponentsConfig]
    List(
      ComponentDefinition("cdr", CallDetailRecordSourceFactory.prepare(componentsConfig.csv.filesDir, componentsConfig.csv.separator.charAt(0))),
      ComponentDefinition("csvSource", new GenericCsvSourceFactory(componentsConfig.csv.filesDir, componentsConfig.csv.separator.charAt(0))),
      ComponentDefinition("csvSink", new CsvSinkFactory(componentsConfig.csv.filesDir, componentsConfig.csv.separator.charAt(0))),
    )
  }

  override def isCompatible(version: NussknackerVersion): Boolean = true

  override def isAutoLoaded: Boolean = true
}

case class FlinkSampleComponentsConfig(csv: CsvConfig)

case class CsvConfig(filesDir: String, separator: String = ";")
