package service.entity

import zio.json._

case class RecordSubmissionResponseApiEntity(status: String)

object RecordSubmissionResponseApiEntity {

  implicit val fromJsonDecoderRecordSubmissionResponseApiEntity =
    DeriveJsonDecoder.gen[RecordSubmissionResponseApiEntity]

}
