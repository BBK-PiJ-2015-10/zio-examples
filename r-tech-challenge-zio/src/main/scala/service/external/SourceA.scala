package service.external

import service.entity.{RecordAApiEntity, SourceAApiEntity}
import zio._
import zio._
import zio.json._
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import service.entity.SourceAApiEntity.fromJsonDecoderSourceAApiEntity

trait SourceA {

   def fetchSourceARecord(): ZIO[Any, Throwable,Option[RecordAApiEntity]]

}

case class SourceAImpl(url: String, eventLoopGroup: EventLoopGroup, channelFactory: ChannelFactory) extends SourceA {


   override def fetchSourceARecord(): ZIO[Any, Throwable,Option[RecordAApiEntity]] = (for {
      _  <- ZIO.logInfo(s"Placing a request to $url")
      response  <- Client.request(url)
      maybeRecord <- processResponse(response)
   } yield maybeRecord).provide(
      ZLayer.fromZIO(ZIO.from(eventLoopGroup)),
      ZLayer.fromZIO(ZIO.from(channelFactory)))


   private def processResponse(response: Response): ZIO[Any, Throwable, Option[RecordAApiEntity]] = {
      for {
         eitherErrorRecord <-  response.body.asString.map(_.fromJson[RecordAApiEntity])
         zioRecord  = eitherErrorRecord match {
            case Left(e) =>
               ZIO.logWarning(s"Received a malformed record $e") zipRight
               ZIO.succeed(None)
            case Right(record) =>
               ZIO.succeed(Some(record))
         }
         maybeRecord  <- zioRecord
      } yield maybeRecord
   }


}
