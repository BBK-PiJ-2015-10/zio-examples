package service.entity

import zio.json._
import java.util.UUID

case class SourceAApiEntity(status: String, id: UUID)


object SourceAApiEntity {

  implicit val toJsonEncoderSourceAApiEntity = DeriveJsonEncoder.gen[SourceAApiEntity]

  implicit val fromJsonDecoderSourceAApiEntity = DeriveJsonDecoder.gen[SourceAApiEntity]

}

