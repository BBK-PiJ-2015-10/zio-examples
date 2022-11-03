package service.entity

import zio.json._

case class RecordApiEntity(status: String, id: Option[String])

object RecordApiEntity {

  implicit val toJsonEncoderRecordApiEntity = DeriveJsonEncoder.gen[RecordApiEntity]

  implicit val fromJsonDecoderRecordApiEntity = DeriveJsonDecoder.gen[RecordApiEntity]

}
