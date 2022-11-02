package service.external

import service.entity.RecordApiEntity
import zio.{ZIO, ZLayer}
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}


import scala.xml.{Node, NodeSeq, XML}

trait SourceB {

   def fetchSourceBRecord(): ZIO[Any, Throwable,Option[RecordApiEntity]]

}

case class SourceBImpl(url: String) extends SourceB {

   override def fetchSourceBRecord(): ZIO[Any, Throwable,Option[RecordApiEntity]] = (for {
      _  <- ZIO.logInfo(s"Placing a request to $url")
      response  <- Client.request(url)
      maybeRecord <- processResponse(response)
   } yield maybeRecord).provide(
      EventLoopGroup.auto(),
      ChannelFactory.auto)


   private def processResponse(response: Response) = {
      for {
         stringRecord <-  response.body.asString
         maybeXmlMessage  =  extractXmlMessage(stringRecord)
         recordApiEntity  =  if (maybeXmlMessage.isEmpty){
                               val empty : Option[RecordApiEntity] = None
                               empty
                            } else {
           Some(createRecordEntity(maybeXmlMessage.get))
      }
      } yield (recordApiEntity)
   }

   private def extractXmlMessage(response: String): Option[Node] = {
      val xmlDoc = XML.loadString(response)
      val xmlMsg = xmlDoc \\ "msg"
      xmlMsg.lastOption
   }

   private def createRecordEntity(msgXml: Node) = {
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
