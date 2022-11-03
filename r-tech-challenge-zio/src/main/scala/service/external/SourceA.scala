package service.external

import service.entity.RecordApiEntity
import service.internal.SourceResponseParser
import zio.{ZIO, ZLayer}

trait SourceA {

  def fetchSourceARecord(): ZIO[Any, Throwable, Option[RecordApiEntity]]

}

case class SourceAImpl(url: String, source: Source) extends SourceA {

  override def fetchSourceARecord(): ZIO[Any, Throwable, Option[RecordApiEntity]] =
    source.fetchSourceRecord(url, stringResponse => SourceResponseParser.parseJsonResponse(stringResponse))

}

object SourceAImpl {
  def layer(url: String, source: Source): ZLayer[Any, Throwable, SourceA] =
    ZLayer.fromZIO(ZIO.from(SourceAImpl(url, source)))
}
