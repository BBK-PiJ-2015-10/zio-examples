package service.entity

import zio.json._

case class RecordAApiEntity(status: String, id: Option[String])


object SourceAApiEntity {

  implicit val toJsonEncoderSourceAApiEntity = DeriveJsonEncoder.gen[RecordAApiEntity]

  implicit val fromJsonDecoderSourceAApiEntity = DeriveJsonDecoder.gen[RecordAApiEntity]

}

