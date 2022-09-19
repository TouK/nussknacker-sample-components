package pl.touk.nussknacker.sample

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FunSuite
import pl.touk.nussknacker.engine.flink.test.FlinkSpec

trait FlinkSampleComponentsBaseTest extends FunSuite with FlinkSpec {

  override protected lazy val config: Config = ConfigFactory.empty()
    .withValue("components.flinkSample.csv.filesDir", fromAnyRef(System.getProperty("java.io.tmpdir")))

}
