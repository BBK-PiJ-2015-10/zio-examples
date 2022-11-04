package service

import service.external.{Source, SourceA, SourceAImpl, SourceB, SourceBImpl, SourceImpl}
import service.internal.{Orchestrator, OrchestratorImpl}
import zio._

object AllLayers {

  val urlA = "http://localhost:7299/source/a"
  val urlB = "http://localhost:7299/source/b"

  val sourceLayer: ZLayer[Any, Nothing, Source] = SourceImpl.layer()

  val sourceALayer: ZLayer[Any, Throwable, SourceA] = sourceLayer.flatMap { src =>
    SourceAImpl.layer(urlA, src.get)
  }

  val sourceBLayer: ZLayer[Any, Throwable, SourceB] = sourceLayer.flatMap { src =>
    SourceBImpl.layer(urlB, src.get)
  }

  val orchestratorLayer = OrchestratorImpl.layer()

}
