package service.entity

import zio.json._

case class RecordSubmissionApiEntity(kind: String, id: String)

object RecordSubmissionApiEntity {

  implicit val toJsonEncoderRecordSubmissionApiEntity = DeriveJsonEncoder.gen[RecordSubmissionApiEntity]

  implicit val fromJsonDecoderRecordSubmissionApiEntity = DeriveJsonDecoder.gen[RecordSubmissionApiEntity]

}
