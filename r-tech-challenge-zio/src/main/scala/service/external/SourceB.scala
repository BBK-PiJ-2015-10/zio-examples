package service.external

import service.entity.RecordApiEntity
import service.internal.SourceResponseParser
import zio.{ZIO, ZLayer}
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}

trait SourceB {

  def fetchSourceBRecord(): ZIO[Any, Throwable, Option[RecordApiEntity]]

}

case class SourceBImpl(url: String) extends SourceB {

  override def fetchSourceBRecord(): ZIO[Any, Throwable, Option[RecordApiEntity]] = (for {
    _              <- ZIO.logInfo(s"Placing a request to $url")
    response       <- Client.request(url)
    maybeRecordZio <- processResponse(response)
    maybeRecord    <- maybeRecordZio
  } yield maybeRecord).provide(EventLoopGroup.auto(), ChannelFactory.auto)

  private def processResponse(response: Response) =
    for {
      stringRecord <- response.body.asString
    } yield (SourceResponseParser.parseXmlResponse(stringRecord))

}

object SourceBImpl {
  def layer(url: String): ZLayer[Any, Throwable, SourceB] =
    ZLayer.fromZIO(ZIO.from(SourceBImpl(url)))
}
