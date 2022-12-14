package service.internal

import service.entity.{RecordApiEntity, RecordSubmissionApiEntity}
//import zio.stm._
import zio._

trait Processor {

  def process(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]]

  def processOrphans(): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]]

}

//case class ProcessorImpl(var recordIds: Set[String], sources: Ref[Int]) extends Processor {
//
//  def process0(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] = {
//    val processZio: ZSTM[Any, Throwable, List[RecordSubmissionApiEntity]] = for {
//      recordsToSubmit <- processHelper(record)
//    } yield recordsToSubmit
//    processZio.commit
//  }
//
//  def process(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] = {
//    processHelper(record).commit
//  }
//
//  def getSources = sources
//
//  private def processHelper(record: RecordApiEntity): STM[Throwable, List[RecordSubmissionApiEntity]] =
//    if (record.status.equals("ok")) {
//      if (!record.id.isEmpty) {
//        val exist = recordIds.contains(record.id.get)
//        if (exist) {
//          recordIds -= record.id.get
//          println("FUCKER1")
//          STM.attempt(List(RecordSubmissionApiEntity("joined", record.id.get)))
//        } else {
//          recordIds += record.id.get
//          println("FUCKER2")
//          STM.attempt(List())
//        }
//      } else STM.attempt(List())
//    } else {
//      val count = sources.updateAndGet(_ + 1)
//      if (count == 2) {
//        println("FUCKER3")
//        STM.attempt(processOrphans())
//      } else {
//        println("FUCKER4")
//        STM.attempt(List())
//      }
//    }
//
////  def processZIO(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] =
////    if (record.status.equals("ok")) {
////      if (!record.id.isEmpty) {
////        val exist = recordIds.contains(record.id.get)
////        if (exist)
////          ZIO.attempt(List(RecordSubmissionApiEntity("joined", record.id.get)))
////        else
////          recordIds += record.id.get
////        ZIO.attempt(List())
////      } else ZIO.logInfo(s"Weird received a ${record.status} with empty id") zipRight ZIO.succeed(List())
////    } else {
////      val count = sources.updateAndGet(_ + 1)
////      if (count == 2) {
////        ZIO.logInfo(s"Process done, sending orphans") zipRight ZIO.attempt(processOrphans())
////      } else {
////        ZIO.logInfo(s"Received first done. Sending empty") zipRight ZIO.attempt(List())
////      }
////    }
//
//  def processOrphans(): List[RecordSubmissionApiEntity] =
//    recordIds.map(id => RecordSubmissionApiEntity("orphan", id)).toList
//
//}
//
//object ProcessorImpl {
//  def layer(recordIds: Set[String], sources: Ref[Int]): ZLayer[Any, Throwable, Processor] =
//    ZLayer.fromZIO(ZIO.from(ProcessorImpl(recordIds, sources)))
//}
