package service.external

import service.entity.{RecordSubmissionApiEntity, RecordSubmissionResponseApiEntity}
import zio.{Ref, ZIO, ZLayer}
import zio.json._
import service.entity.RecordSubmissionApiEntity.toJsonEncoderRecordSubmissionApiEntity
import service.internal.SinkResponseParser
import zhttp.http._
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}

trait Sink {

  def submitRecord(
    record: RecordSubmissionApiEntity
  ): ZIO[Any, Throwable, Option[RecordSubmissionResponseApiEntity]]

  def submitRecords(
    records: List[RecordSubmissionApiEntity]
  ): ZIO[Any, Throwable, List[Option[RecordSubmissionResponseApiEntity]]]

}

case class SinkImpl(url: String) extends Sink {

  override def submitRecord(
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

  override def submitRecords(
    records: List[RecordSubmissionApiEntity]
  ): ZIO[Any, Throwable, List[Option[RecordSubmissionResponseApiEntity]]] =
    for {
      _         <- ZIO.logInfo(s"Submitting $records records")
      responses <- ZIO.foreachPar(records)(submitRecord(_))
    } yield responses
}

object SinkImpl {
  def layer(url: String): ZLayer[Any, Throwable, Sink] =
    ZLayer.fromZIO(ZIO.from(SinkImpl(url)))
}
