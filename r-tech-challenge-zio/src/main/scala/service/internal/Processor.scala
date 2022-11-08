package service.internal

import service.entity.{RecordApiEntity, RecordSubmissionApiEntity}

import zio.stm._
import zio._

trait Processor {}

case class ProcessorImpl(recordIds: Set[String], ref: TRef[Int]) {

  ///val recordIds: Set[String]

  def process(record: RecordApiEntity) : ZIO[Any,Nothing,List[RecordSubmissionApiEntity]] =
    if (record.status.equals("ok")) {
      if (!record.id.isEmpty) {
        val exist = recordIds.contains(record.id.get)
        if (exist)
          ZIO.succeed(List(RecordSubmissionApiEntity("joined", record.id.get)))
        else
          recordIds += record.id.get
        ZIO.succeed(List())
      } else ZIO.logInfo(s"Weird received a ${record.status} with empty id") zipRight ZIO.succeed(List())
    } else {
      val count = ref.updateAndGet(_ + 1)
      if (count==2){
        ZIO.logInfo(s"Process done, sending orphans") zipRight ZIO.succeed(processOrphans())
      } else {
        ZIO.logInfo(s"Received first done. Sending empty") zipRight ZIO.succeed(List())
      }
    }

  def processOrphans(): List[RecordSubmissionApiEntity] =  {
    recordIds.map(id => RecordSubmissionApiEntity("orphan",id)).toList
  }



}
