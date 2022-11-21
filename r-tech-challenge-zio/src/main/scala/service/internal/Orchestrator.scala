package service.internal

import service.entity.RecordApiEntity
import service.external.{Sink, SourceA, SourceB}
import zio._

trait Orchestrator {

  def execute(): ZIO[Any, Throwable, (String, String, String)]

}

case class OrchestratorImpl(sourceA: SourceA, sourceB: SourceB, processor: ProcessorImpl, sink: Sink)
    extends Orchestrator {

  override def execute(): ZIO[Any, Throwable, (String, String, String)] = for {
    queue               <- Queue.unbounded[RecordApiEntity]
    triggerAFib         <- triggerA(queue).fork
    triggerBFib         <- triggerB(queue).fork
    triggerProcessorFib <- triggerProcessor(queue).fork
    _                   <- ZIO.logInfo("culon")
    doneA               <- triggerAFib.join
    _                   <- ZIO.logInfo(s"Done with $doneA")
    doneB               <- triggerBFib.join
    _                   <- ZIO.logInfo(s"Done with $doneB")
    doneP               <- triggerProcessorFib.join
    _                   <- ZIO.logInfo(s"Done with $doneP")
    _                   <- ZIO.logInfo(s"Done with all of them $doneA, $doneB, $doneP")
  } yield (doneA, doneB, doneP)

  private def triggerProcessor(queue: Queue[RecordApiEntity]): ZIO[Any, Throwable, String] =
    for {
      recordReceived  <- queue.take
      recordsToSubmit <- processor.process(recordReceived)
      _               <- ZIO.logInfo(s"Processing will send to submit $recordsToSubmit")
      result <- if (!recordsToSubmit.isEmpty) {
                  ZIO.logInfo(s"Submitting $recordsToSubmit") zipRight sink.submitRecords(recordsToSubmit) *>
                    ZIO.logInfo("Evaluating if done path 1") zipRight evaluateDone(queue)
                } else {
                  ZIO.logInfo("Evaluating if done path 2") zipRight evaluateDone(queue)
                }
    } yield result

  def evaluateDone(queue: Queue[RecordApiEntity]): ZIO[Any, Throwable, String] =
    for {
      doneCount <- processor.sources.get
      result <- if (doneCount == 2) {
                  ZIO.logInfo(s"Done looking for queues, doneCount is $doneCount") *> triggerProcessor(queue)
                } else {
                  ZIO.logInfo(s"Not done, , doneCount is $doneCount will look for more times on queue") *> ZIO.succeed(
                    "done-P"
                  )
                }
    } yield result

  private def triggerA(queue: Queue[RecordApiEntity]): ZIO[Any, Throwable, String] =
    for {
      _      <- ZIO.logInfo("Requesting record from sourceA")
      record <- sourceA.fetchSourceARecord()
      _      <- queue.offer(record.get).when(!record.isEmpty && !record.get.status.equals("done"))
      done <- if (!record.isEmpty && record.get.status.equals("done")) {
                ZIO.logInfo(s"Received done record from source A") zipRight ZIO.succeed("done-A")
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
                ZIO.logInfo(s"Received done record from source B") zipRight ZIO.succeed("done-B")
              } else {
                ZIO.logInfo(s"Received record from source B $record") zipRight triggerB(queue)
              }
    } yield done

}

object OrchestratorImpl {
  def layer(): ZLayer[SourceA with SourceB with ProcessorImpl with Sink, Throwable, Orchestrator] =
    ZLayer.fromFunction(OrchestratorImpl(_, _, _, _))
}
