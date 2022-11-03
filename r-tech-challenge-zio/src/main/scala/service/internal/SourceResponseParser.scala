package service.internal

import service.entity.RecordApiEntity
import zio.ZIO
import zio.json.DecoderOps
import service.entity.RecordApiEntity.fromJsonDecoderRecordApiEntity

import scala.xml.{Node, XML}

object SourceResponseParser {

  def parseXmlResponse(response: String): ZIO[Any, Nothing, Option[RecordApiEntity]] =
    ZIO.attempt(extractXmlMessage(response).map(createRecordEntity(_))).catchAll { _ =>
      ZIO.logInfo(s"Received a malformed response $response") zipRight
        ZIO.succeed(None)
    }

  def parseJsonResponse(response: String): ZIO[Any, Throwable, Option[RecordApiEntity]] =
    parseJsonResponseHelper(response).catchAll{ _ =>
      ZIO.logInfo(s"Received a malformed response $response") zipRight
        ZIO.succeed(None)
    }
  
  private def parseJsonResponseHelper(response: String): ZIO[Any, Throwable, Option[RecordApiEntity]] =
    for {
      eitherErrorRecord <- ZIO.attempt(response).map(_.fromJson[RecordApiEntity])
      zioRecord = eitherErrorRecord match {
                    case Left(e) =>
                      ZIO.logWarning(s"Received a malformed record $e") zipRight
                        ZIO.succeed(None)
                    case Right(record) =>
                      ZIO.succeed(Some(record))
                  }
      maybeRecord <- zioRecord
    } yield maybeRecord

  private def extractXmlMessage(response: String): Option[Node] = {
    val xmlDoc = XML.loadString(response)
    val xmlMsg = xmlDoc \\ "msg"
    xmlMsg.lastOption
  }

  private def createRecordEntity(msgXml: Node): RecordApiEntity = {
    val idXml = msgXml \ "id"
    if (!idXml.isEmpty) {
      val culon = idXml \\ "@value"
      val id    = culon.toList.head.text
      RecordApiEntity("ok", Some(id))
    } else {
      val status = msgXml.text
      RecordApiEntity(status, None)
    }
  }

}
