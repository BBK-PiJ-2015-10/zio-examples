package service.internal

import service.entity.{RecordApiEntity, RecordSubmissionApiEntity}

import zio.stm._
import zio._

trait Processor {}

case class ProcessorImpl(var recordIds: Set[String], ref: TRef[Int]) {

  def process(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] = {
    val processZio: ZSTM[Any, Throwable, List[RecordSubmissionApiEntity]] = for {
      recordsToSubmit <- processHelper(record)
    } yield recordsToSubmit
    processZio.commit
  }

  private def processHelper(record: RecordApiEntity): STM[Throwable, List[RecordSubmissionApiEntity]] =
    if (record.status.equals("ok")) {
      if (!record.id.isEmpty) {
        val exist = recordIds.contains(record.id.get)
        if (exist) {
          recordIds -= record.id.get
          STM.attempt(List(RecordSubmissionApiEntity("joined", record.id.get)))
        } else
          recordIds += record.id.get
        STM.attempt(List())
      } else STM.attempt(List())
    } else {
      val count = ref.updateAndGet(_ + 1)
      if (count == 2) {
        STM.attempt(processOrphans())
      } else {
        STM.attempt(List())
      }
    }

  def processZIO(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] =
    if (record.status.equals("ok")) {
      if (!record.id.isEmpty) {
        val exist = recordIds.contains(record.id.get)
        if (exist)
          ZIO.attempt(List(RecordSubmissionApiEntity("joined", record.id.get)))
        else
          recordIds += record.id.get
        ZIO.attempt(List())
      } else ZIO.logInfo(s"Weird received a ${record.status} with empty id") zipRight ZIO.succeed(List())
    } else {
      val count = ref.updateAndGet(_ + 1)
      if (count == 2) {
        ZIO.logInfo(s"Process done, sending orphans") zipRight ZIO.attempt(processOrphans())
      } else {
        ZIO.logInfo(s"Received first done. Sending empty") zipRight ZIO.attempt(List())
      }
    }

  def processOrphans(): List[RecordSubmissionApiEntity] =
    recordIds.map(id => RecordSubmissionApiEntity("orphan", id)).toList

}
