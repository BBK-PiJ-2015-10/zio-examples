import service.AllLayers._
import service.internal.{Orchestrator}
import zio._

object Main extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = (for {
    orchestrator <- ZIO.service[Orchestrator]
    _            <- orchestrator.execute()

  } yield ()).provide(
    sourceLayer,
    sourceALayer,
    sourceBLayer,
    orchestratorLayer,
    biProcessor,
    sinkLayer
  )

}
