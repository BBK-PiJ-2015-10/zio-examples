package service.internal
import service.entity.{RecordApiEntity, RecordSubmissionApiEntity}
import zio.{URLayer, ZIO, ZLayer}

case class BiProcessor() extends Processor {

  var recordIds: Set[String] = Set()

  override def process(record: RecordApiEntity): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] =
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
        ZIO.logInfo(s"Processor received a weird record ${record} with empty id") zipRight ZIO.succeed(List())
    } else {
      ZIO.logInfo(s"Processor received a weird record ${record.status} with status ${record.status}") zipRight ZIO
        .succeed(List())
    }

  override def processOrphans(): ZIO[Any, Throwable, List[RecordSubmissionApiEntity]] =
    ZIO.logInfo(s"Processor processing orphans") zipRight
      ZIO.attempt(recordIds.map(id => RecordSubmissionApiEntity("orphan", id)).toList)

}

object BiProcessor {

  def layer: URLayer[Any, Processor] =
    ZLayer.succeed(BiProcessor())

}
