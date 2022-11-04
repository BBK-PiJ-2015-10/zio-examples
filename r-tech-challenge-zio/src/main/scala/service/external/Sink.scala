package service.external

import service.entity.{RecordSubmissionApiEntity, RecordSubmissionResponseApiEntity}
import zio.ZIO

trait Sink {

  def submitRecord(record: RecordSubmissionApiEntity): ZIO[Any, Throwable, RecordSubmissionResponseApiEntity]

}

case class SinkImpl() extends Sink {
  override def submitRecord(record: RecordSubmissionApiEntity): ZIO[Any, Throwable, RecordSubmissionResponseApiEntity] =
    ???
}
