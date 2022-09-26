package pl.touk.nussknacker.sample

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigFactory}
import org.junit.jupiter.api.{AfterAll, BeforeAll}
import org.scalatest.Suite
import pl.touk.nussknacker.engine.flink.test.FlinkSpec

class FlinkSampleComponentsBaseClassTest extends Suite with FlinkSpec {

  override lazy val config: Config = ConfigFactory.empty()
    .withValue("components.flinkSample.csv.filesDir", fromAnyRef(System.getProperty("java.io.tmpdir")))

  @BeforeAll
  def init(): Unit = {
    beforeAll()
  }

  @AfterAll
  def tearDown(): Unit = {
    afterAll()
  }

}


