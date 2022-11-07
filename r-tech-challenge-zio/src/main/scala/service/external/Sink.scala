package service.external

import service.entity.{RecordSubmissionApiEntity, RecordSubmissionResponseApiEntity}
import zio.ZIO
import zio.json._
import service.entity.RecordSubmissionApiEntity.toJsonEncoderRecordSubmissionApiEntity
import service.internal.SinkResponseParser
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}

trait Sink {

  def submitRecord(
    url: String,
    record: RecordSubmissionApiEntity
  ): ZIO[Any, Throwable, Option[RecordSubmissionResponseApiEntity]]

}

case class SinkImpl() extends Sink {

  override def submitRecord(
    url: String,
    record: RecordSubmissionApiEntity
  ): ZIO[Any, Throwable, Option[RecordSubmissionResponseApiEntity]] = (for {
    recordSubmissionJson <- ZIO.from(record.toJson)
    body                 <- ZIO.from(Body.fromString(recordSubmissionJson))
    response             <- Client.request(url, Method.POST, content = body)
    responseString       <- response.body.asString
    maybeResponse        <- SinkResponseParser.jsonRecordSubmissionResponse(responseString)
  } yield maybeResponse).provide(
    EventLoopGroup.auto(),
    ChannelFactory.auto
  )

}
