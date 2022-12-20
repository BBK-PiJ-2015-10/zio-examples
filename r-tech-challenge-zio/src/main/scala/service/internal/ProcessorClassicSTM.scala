package service.internal

import service.entity.{RecordApiEntity, RecordSubmissionApiEntity}
import zio.stm._
import zio._

case class ProcessorClassicSTM(var recordIds: Set[String], sources: Ref[Int]) extends Processor {

//  def process0(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] = {
//    val processZio: ZSTM[Any, Throwable, List[RecordSubmissionApiEntity]] = for {
//      recordsToSubmit <- processHelper(record)
//    } yield recordsToSubmit
//    processZio.commit
//  }

  override def processOrphans(): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] =
    ZIO.attempt(processOrphans2())

  def process(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] =
    processZIO(record)
  //processHelper(record).commit

  def getSources = sources

//  private def processHelper(record: RecordApiEntity): STM[Throwable, List[RecordSubmissionApiEntity]] =
//    if (record.status.equals("ok")) {
//      if (!record.id.isEmpty) {
//        val exist = recordIds.contains(record.id.get)
//        if (exist) {
//          recordIds = recordIds - record.id.get
//          println("FUCKER1")
//          STM.attempt(List(RecordSubmissionApiEntity("joined", record.id.get)))
//        } else {
//          recordIds = recordIds + record.id.get
//          println("FUCKER2")
//          STM.attempt(List())
//        }
//      } else STM.attempt(List())
//    } else {
//      val count = sources.updateAndGet(_ + 1)
//      if (count == 2) {
//        println("FUCKER3")
//        STM.attempt(processOrphans2())
//      } else {
//        println("FUCKER4")
//        STM.attempt(List())
//      }
//    }

  def processZIO(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] =
    if (record.status.equals("ok")) {
      if (!record.id.isEmpty) {
        val exist = recordIds.contains(record.id.get)
        if (exist) {
          recordIds = recordIds - record.id.get
          for {
            _      <- ZIO.logInfo(s"Processor found a pair for id :${record.id}")
            output <- ZIO.attempt(List(RecordSubmissionApiEntity("joined", record.id.get)))
          } yield output
        } else {
          recordIds = recordIds + record.id.get
          for {
            _      <- ZIO.logInfo(s"Processor added id :${record.id}")
            output <- ZIO.attempt(List())
          } yield output
        }
      } else
        ZIO.logInfo(s"Processor received a weird record ${record.status} with empty id") zipRight ZIO.succeed(List())
    } else {
      val count = sources.updateAndGet(_ + 1)
      if (count == 2) {
        ZIO.logInfo(s"Processor Process done, sending orphans") zipRight ZIO.attempt(processOrphans2())
      } else {
        ZIO.logInfo(s"Processor Received first done. Sending empty") zipRight ZIO.attempt(List())
      }
    }

  def processOrphans2(): List[RecordSubmissionApiEntity] =
    recordIds.map(id => RecordSubmissionApiEntity("orphan", id)).toList

}

object ProcessorClassicSTM {
  def layer(recordIds: Set[String], sources: Ref[Int]): ZLayer[Any, Throwable, Processor] =
    ZLayer.fromZIO(ZIO.from(ProcessorClassicSTM(recordIds, sources)))

}
