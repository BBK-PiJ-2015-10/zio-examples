package service.internal

import service.entity.RecordApiEntity
import service.external.{SourceA, SourceB}
import zio._

trait Orchestrator {

  def execute(): ZIO[Any, Throwable, (String, String)]

}

case class OrchestratorImpl(sourceA: SourceA, sourceB: SourceB) extends Orchestrator {

  def testing() = for {
    queue         <- Queue.unbounded[RecordApiEntity]
    sourceAResult <- triggerA(queue).fork
    sourceBResult <- triggerB(queue).fork
  } yield ()

  private def triggerProcessor(queue: Queue[RecordApiEntity]) =
    for {
      record <- queue.take
    } yield ()

  override def execute(): ZIO[Any, Throwable, (String, String)] = for {
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
                ZIO.logInfo(s"Received record from source A: $record") zipRight triggerA(queue)
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
                ZIO.logInfo(s"Received record from source B $record") zipRight triggerB(queue)
              }
    } yield done

}

object OrchestratorImpl {
  def layer(): ZLayer[SourceA with SourceB, Throwable, Orchestrator] =
    ZLayer.fromFunction(OrchestratorImpl(_, _))
}
