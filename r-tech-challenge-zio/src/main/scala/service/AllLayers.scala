package service

import service.external.{Source, SourceAImpl, SourceBImpl, SourceImpl}
import zio._

object AllLayers {

  val urlA = "http://localhost:7299/source/a"
  val urlB = "http://localhost:7299/source/b"

  val sourceLayer: ZLayer[Any, Nothing, Source] = SourceImpl.layer()

  val sourceALayer = sourceLayer.flatMap { src =>
    SourceAImpl.layer(urlA, src.get)
  }

  val sourceBLayer = sourceLayer.flatMap { src =>
    SourceBImpl.layer(urlB, src.get)
  }

}
