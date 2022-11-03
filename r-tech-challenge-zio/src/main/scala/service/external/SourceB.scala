package service.external

import service.entity.RecordApiEntity
import service.internal.SourceResponseParser
import zio.{ZIO, ZLayer}

trait SourceB {

  def fetchSourceBRecord(): ZIO[Any, Throwable, Option[RecordApiEntity]]

}

case class SourceBImpl(url: String, source: Source) extends SourceB {

  def fetchSourceBRecord(): ZIO[Any, Throwable, Option[RecordApiEntity]] =
    source.fetchSourceRecord(url, stringResponse => SourceResponseParser.parseXmlResponse(stringResponse))

}

object SourceBImpl {

  def layer(url: String, source: Source): ZLayer[Any, Throwable, SourceB] =
    ZLayer.fromZIO(ZIO.from(SourceBImpl(url, source)))

}
