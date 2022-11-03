package service.external

import service.entity.RecordApiEntity
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.{ZIO, ZLayer}

trait Source {

  def fetchSourceRecord(
    url: String,
    responseParser: String => ZIO[Any, Nothing, Option[RecordApiEntity]]
  ): ZIO[Any, Throwable, Option[RecordApiEntity]]

}

case class SourceImpl() extends Source {

  def fetchSourceRecord(
    url: String,
    responseParser: String => ZIO[Any, Nothing, Option[RecordApiEntity]]
  ): ZIO[Any, Throwable, Option[RecordApiEntity]] = (for {
    _              <- ZIO.logInfo(s"Placing a request to $url")
    response       <- Client.request(url)
    responseString <- response.body.asString
    maybeRecord    <- responseParser(responseString)
  } yield maybeRecord).provide(EventLoopGroup.auto(), ChannelFactory.auto)

}

object SourceImpl {

  def layer(): ZLayer[Any, Nothing, Source] = ZLayer.succeed(SourceImpl())

}
