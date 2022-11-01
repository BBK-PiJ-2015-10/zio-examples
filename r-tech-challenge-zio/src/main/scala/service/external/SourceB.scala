package service.external

import service.entity.RecordApiEntity
import zio.{URLayer, ZIO, ZLayer}
import zio.json._
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import service.entity.RecordApiEntity.fromJsonDecoderRecordApiEntity

import scala.xml.{Node, NodeSeq, XML}

trait SourceB {

   def fetchSourceBRecord(): ZIO[Any, Throwable,Option[RecordApiEntity]]

}

case class SourceBImpl(url: String) extends SourceB {

   override def fetchSourceBRecord(): ZIO[Any, Throwable,Option[RecordApiEntity]] = (for {
      _  <- ZIO.logInfo(s"Placing a request to $url")
      response  <- Client.request(url)
      maybeRecord <- fucker(response)
   } yield maybeRecord).provide(
      EventLoopGroup.auto(),
      ChannelFactory.auto)


   private def processResponse(response: Response): ZIO[Any, Throwable, Option[String]] = {
      for {
         stringRecord <-  response.body.asString
         id           <- ZIO.from(parseResponse(stringRecord))
         _            <- ZIO.logInfo(s"Fucker $id")

      } yield Some(stringRecord)
   }

   private def fucker(response: Response) = {
      for {
         stringRecord <-  response.body.asString
         ale          =   getMessage(stringRecord)
         xmlMsg            <- ZIO.fromOption(getMessage(stringRecord))
         //record            <- ZIO.attempt(createRecord(xmlMsg))
         //val ale : Option[RecordApiEntity]  = Some(record)
      } yield ()
   }

   private def parseResponse(response: String) = {
      val xmlDoc = XML.loadString(response)
      val xmlMsg =  xmlDoc \\ "msg"
      xmlMsg.foreach(ale => println(ale.text))
      xmlMsg.text
   }

   private def getMessage(response: String): Option[Node] = {
      val xmlDoc = XML.loadString(response)
      val xmlMsg = xmlDoc \\ "msg"
      xmlMsg.lastOption
   }

   private def createRecord(msgXml: Node) = {
      val idXml = msgXml \ "id"
      if (!idXml.isEmpty) {
         val id = idXml.text
         RecordApiEntity("ok",Some(id))
      } else {
         val status  =  msgXml.text
         RecordApiEntity(status,None)
      }
   }


}


object SourceBImpl {
   def layer(url: String): ZLayer[Any, Throwable, SourceB] =
      ZLayer.fromZIO(
         ZIO.from(SourceBImpl(url)))
}
