import service.AllLayers._
import service.external.{SourceA, SourceB}
import service.internal.{Orchestrator, OrchestratorImpl}
import zio._

object Main extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = (for {
//    _        <- ZIO.logInfo("Starting program")
//    sourceA  <- ZIO.service[SourceA]
//    response <- sourceA.fetchSourceARecord()
//    _        <- ZIO.logInfo(s"Sucker got $response")
//
//    sourceB  <- ZIO.service[SourceB]
//    response <- sourceB.fetchSourceBRecord()
//    _        <- ZIO.logInfo(s"Fucker got $response")
//    _        <- ZIO.logInfo("Ending program")

    orchestrator <- ZIO.service[Orchestrator]
    _            <- orchestrator.execute()

  } yield ()).provide(
    sourceLayer,
    sourceALayer,
    sourceBLayer,
    orchestratorLayer,
    processorLayer,
    sinkLayer
  )

}
