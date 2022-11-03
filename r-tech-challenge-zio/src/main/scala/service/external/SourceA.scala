package service.external

import service.entity.RecordApiEntity
import zio.{ZIO, ZLayer}
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import service.internal.SourceResponseParser

trait SourceA {

  def fetchSourceARecord(): ZIO[Any, Throwable, Option[RecordApiEntity]]

}

case class SourceAImpl(url: String) extends SourceA {

  override def fetchSourceARecord(): ZIO[Any, Throwable, Option[RecordApiEntity]] = (for {
    _           <- ZIO.logInfo(s"Placing a request to $url")
    response    <- Client.request(url)
    maybeRecord <- processResponse(response)
  } yield maybeRecord).provide(EventLoopGroup.auto(), ChannelFactory.auto)

  private def processResponse(response: Response) =
    for {
      first          <- response.body.asString
      maybeApiEntity <- SourceResponseParser.parseJsonResponse(first)
    } yield maybeApiEntity

}

object SourceAImpl {
  def layer(url: String): ZLayer[Any, Throwable, SourceA] =
    ZLayer.fromZIO(ZIO.from(SourceAImpl(url)))
}
