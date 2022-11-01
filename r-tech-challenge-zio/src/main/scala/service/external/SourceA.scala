package service.external

import service.entity.{RecordApiEntity}
import zio.{URLayer, ZIO, ZLayer}
import zio.json._
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import service.entity.RecordApiEntity.fromJsonDecoderRecordApiEntity

trait SourceA {

   def fetchSourceARecord(): ZIO[Any, Throwable,Option[RecordApiEntity]]

}

case class SourceAImpl(url: String) extends SourceA {

   override def fetchSourceARecord(): ZIO[Any, Throwable,Option[RecordApiEntity]] = (for {
      _  <- ZIO.logInfo(s"Placing a request to $url")
      response  <- Client.request(url)
      maybeRecord <- processResponse(response)
   } yield maybeRecord).provide(
      EventLoopGroup.auto(),
      ChannelFactory.auto)


   private def processResponse(response: Response): ZIO[Any, Throwable, Option[RecordApiEntity]] = {
      for {
         eitherErrorRecord <-  response.body.asString.map(_.fromJson[RecordApiEntity])
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


object SourceAImpl {
   def layer(url: String): ZLayer[Any, Throwable, SourceA] =
      ZLayer.fromZIO(
         ZIO.from(SourceAImpl(url)))
}
