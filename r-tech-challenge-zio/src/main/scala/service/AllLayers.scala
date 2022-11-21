package service

import service.external.{SinkImpl, Source, SourceA, SourceAImpl, SourceB, SourceBImpl, SourceImpl}
import service.internal.{Orchestrator, OrchestratorImpl, Processor, ProcessorClassicSTM, ProcessorImpl}
import zio._
import zio.stm.{TRef, ZSTM}

object AllLayers {

  val urlA    = "http://localhost:7299/source/a"
  val urlB    = "http://localhost:7299/source/b"
  val urlSink = "http://localhost:7299/sink/a"

  val sourceLayer: ZLayer[Any, Nothing, Source] = SourceImpl.layer()

  val sourceALayer: ZLayer[Any, Throwable, SourceA] = sourceLayer.flatMap { src =>
    SourceAImpl.layer(urlA, src.get)
  }

  val sourceBLayer: ZLayer[Any, Throwable, SourceB] = sourceLayer.flatMap { src =>
    SourceBImpl.layer(urlB, src.get)
  }

  val sinkLayer = SinkImpl.layer(urlSink)

  val orchestratorLayer = OrchestratorImpl.layer()

  val totalSources: UIO[Ref[Int]] = Ref.make(2)

  val processorZio: ZIO[Any, Throwable, ProcessorClassicSTM] = for {
    totalConsumers <- Ref.make(0)
    ale            <- ZIO.from(ProcessorClassicSTM(Set(), totalConsumers))
  } yield ale

  val processorLayer: ZLayer[Any, Throwable, ProcessorClassicSTM] = {
    ZLayer.fromZIO(processorZio)
  }

}
