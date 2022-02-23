package pl.touk.nussknacker.sample

import com.typesafe.config.Config
import org.apache.commons.text.RandomStringGenerator
import pl.touk.nussknacker.engine.api.component.{ComponentDefinition, ComponentProvider, NussknackerVersion}
import pl.touk.nussknacker.engine.api.process.ProcessObjectDependencies
import pl.touk.nussknacker.engine.api.{MethodToInvoke, ParamName, Service}

import scala.concurrent.Future

class SampleComponentProvider extends ComponentProvider {

  override def providerName: String = "sample"

  override def resolveConfigForExecution(config: Config): Config = config

  override def create(config: Config, dependencies: ProcessObjectDependencies): List[ComponentDefinition] = List(
    ComponentDefinition("randomString", new RandomStringProvider)
  )

  override def isCompatible(version: NussknackerVersion): Boolean = true

  override def isAutoLoaded: Boolean = true


}

class RandomStringProvider extends Service {

  private val builder = new RandomStringGenerator.Builder().build()

  @MethodToInvoke
  def invoke(@ParamName("length") argument: Int): Future[String] = Future.successful {
    builder.generate(argument)
  }

}
