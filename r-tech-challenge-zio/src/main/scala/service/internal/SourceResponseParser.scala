package service.internal

import service.entity.RecordApiEntity

import scala.xml.{Node, XML}

object SourceResponseParser {

  def parseXmlResponse(response: String)  : Option[RecordApiEntity]  = {
    val xmlMessage = extractXmlMessage(response)
    xmlMessage.map(createRecordEntity(_))
  }

  private def extractXmlMessage(response: String): Option[Node] = {
    val xmlDoc = XML.loadString(response)
    val xmlMsg = xmlDoc \\ "msg"
    xmlMsg.lastOption
  }

  private def createRecordEntity(msgXml: Node): RecordApiEntity = {
    println(s"Ale $msgXml")
    println("Mimado")

    val idXml = msgXml \ "id"
    println(s"Culon $idXml")
    if (!idXml.isEmpty) {
      val culon = idXml \\ "@value"
      val id  = culon.toList.head.text
      RecordApiEntity("ok",Some(id))
    } else {
      val status  =  msgXml.text
      RecordApiEntity(status,None)
    }
  }


}
