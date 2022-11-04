package service.internal

import service.entity.RecordApiEntity
import service.external.{Source, SourceA, SourceB}
import zio._

case class OrchestratorImpl(sourceA: SourceA, sourceB: SourceB) {

  private def execute() = for {
    queue     <- Queue.unbounded[RecordApiEntity]
    doneTuple <- triggerA(queue) zipPar triggerB(queue)
  } yield doneTuple

  private def triggerA(queue: Queue[RecordApiEntity]): ZIO[Any, Throwable, String] =
    for {
      _      <- ZIO.logInfo("Requesting record from sourceA")
      record <- sourceA.fetchSourceARecord()
      _      <- queue.offer(record.get).when(!record.isEmpty && !record.get.status.equals("done"))
      done <- if (!record.isEmpty && record.get.status.equals("done")) {
                ZIO.logInfo(s"Received done record from source A") zipRight ZIO.succeed("done")
              } else {
                ZIO.logInfo(s"Received record from source A") zipRight triggerA(queue)
              }
    } yield done

  private def triggerB(queue: Queue[RecordApiEntity]): ZIO[Any, Throwable, String] =
    for {
      _      <- ZIO.logInfo("Requesting record from sourceB")
      record <- sourceA.fetchSourceARecord()
      _      <- queue.offer(record.get).when(!record.isEmpty && !record.get.status.equals("done"))
      done <- if (!record.isEmpty && record.get.status.equals("done")) {
                ZIO.logInfo(s"Received done record from source B") zipRight ZIO.succeed("done")
              } else {
                ZIO.logInfo(s"Received record from source B") zipRight triggerB(queue)
              }
    } yield done

}

object OrchestratorImpl {
  def layer(): ZLayer[SourceA with SourceB,Throwable,OrchestratorImpl] =
    ZLayer.fromFunction(OrchestratorImpl(_,_))
}



