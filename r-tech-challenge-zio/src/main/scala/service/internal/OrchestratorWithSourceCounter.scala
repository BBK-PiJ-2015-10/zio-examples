package service.internal
import service.entity.RecordApiEntity
import service.external.{Sink, SourceA, SourceB}
import zio.{Queue, Ref, URLayer, ZIO, ZLayer}

case class OrchestratorWithSourceCounter(sourceA: SourceA, sourceB: SourceB, processor: Processor, sink: Sink)
    extends Orchestrator {

  override def execute(): ZIO[Any, Throwable, (Boolean, Boolean, Boolean,Boolean)] = for {
    counter             <- Ref.make(0)
    queue               <- Queue.unbounded[RecordApiEntity]
    triggerAFib         <- triggerA(queue, counter).fork
    triggerBFib         <- triggerB(queue,counter).fork
    triggerProcessorFib <- triggerProcessor(queue,counter).fork
    _                   <- ZIO.logInfo("culon")
    doneA               <- triggerAFib.join
    _                   <- ZIO.logInfo(s"Done with source A $doneA")
    doneB               <- triggerBFib.join
    _                   <- ZIO.logInfo(s"Done with source B $doneB")
    doneMatch           <- triggerProcessorFib.join
    _                   <- ZIO.logInfo(s"Done with match processing $doneMatch")
    doneOrphans         <- triggerProcessorOrphanProcessing()
    _                   <- ZIO.logInfo(s"Done with orphan processing $doneOrphans")
    _                   <- ZIO.logInfo(s"Done with all of them $doneA, $doneB, $doneMatch, $doneOrphans")
  } yield (doneA, doneB, doneMatch,doneOrphans)

  private def triggerProcessor(queue: Queue[RecordApiEntity],counter: Ref[Int]): ZIO[Any, Throwable, Boolean] =
    for {
      recordReceived  <- queue.take
      recordsToSubmit <- processor.process(recordReceived)
      //_               <- ZIO.logInfo(s"Orchestrator will send to submit $recordsToSubmit")
      result <- if (!recordsToSubmit.isEmpty) {
                  ZIO.logInfo(s"Submitting $recordsToSubmit") zipRight sink.submitRecords(recordsToSubmit) *>
                    ZIO.logInfo("Evaluating if done path 1") zipRight evaluateDone(queue,counter)
                } else {
                  ZIO.logInfo("Evaluating if done path 2") zipRight evaluateDone(queue,counter)
                }
    } yield result

  private def triggerProcessorOrphanProcessing(): ZIO[Any, Throwable, Boolean] =
    for {
      recordsToSubmit <- processor.processOrphans()
      _               <- ZIO.logInfo(s"Orchestrator will send to submit $recordsToSubmit")
      result <- if (!recordsToSubmit.isEmpty) {
        ZIO.logInfo(s"Submitting orphans $recordsToSubmit") zipRight sink.submitRecords(recordsToSubmit) *>
          ZIO.succeed(true)
      } else {
        ZIO.logInfo(s"Submitting orphans $recordsToSubmit") *> ZIO.succeed(true)
      }
    } yield result



  def evaluateDone(queue: Queue[RecordApiEntity],counter: Ref[Int]): ZIO[Any, Throwable, Boolean] =
    for {
      doneCount <- counter.get
      result <- if (doneCount == 2) {
                  ZIO.logInfo(s"Done looking for queues, doneCount is $doneCount") *> ZIO.succeed(
                    true
                  )
                } else {
                  ZIO.logInfo(
                    s"Not done, , doneCount is $doneCount will look for more times on queue"
                  ) *> triggerProcessor(queue,counter)
                }
    } yield result

  private def triggerA(queue: Queue[RecordApiEntity], counter: Ref[Int]): ZIO[Any, Throwable, Boolean] =
    for {
      _      <- ZIO.logInfo("Requesting record from sourceA")
      record <- sourceA.fetchSourceARecord()
      _      <- ZIO.logInfo(s"Received record $record from sourceA")
      isEmpty = record.isEmpty
      done <- if (isEmpty) {
                ZIO.logInfo(s"Received empty record $record from sourceA") zipRight triggerA(queue, counter)
              } else {
                if (record.get.status.equals("done")) {
                  for {
                    _              <- ZIO.logInfo(s"Received done record $record from sourceA")
                    updatedCounter <- counter.updateAndGet(_ + 1)
                    _              <- ZIO.logInfo(s"Counter updated to $updatedCounter by sourceA")
                    result         <- ZIO.succeed(true)
                  } yield result
                } else {
                  for {
                    _      <- ZIO.logInfo(s"Received non empty record $record from sourceA")
                    _      <- queue.offer(record.get)
                    result <- triggerA(queue, counter)
                  } yield result
                }
              }
    } yield done

  private def triggerB(queue: Queue[RecordApiEntity], counter: Ref[Int]): ZIO[Any, Throwable, Boolean] =
    for {
      _      <- ZIO.logInfo("Requesting record from sourceB")
      record <- sourceB.fetchSourceBRecord()
      _      <- ZIO.logInfo(s"Received record $record from sourceB")
      isEmpty = record.isEmpty
      done <- if (isEmpty) {
                ZIO.logInfo(s"Received empty record $record from sourceB") zipRight triggerB(queue,counter)
              } else {
                if (record.get.status.equals("done")) {
                  for {
                    _              <- ZIO.logInfo(s"Received done record $record from sourceB")
                    updatedCounter <- counter.updateAndGet(_ + 1)
                    _              <- ZIO.logInfo(s"Counter updated to $updatedCounter by sourceB")
                    _              <- queue.offer(record.get)
                    result         <- ZIO.succeed(true)
                  } yield result
                } else {
                  for {
                    _      <- ZIO.logInfo(s"Received non empty record $record from sourceB")
                    _      <- queue.offer(record.get)
                    result <- triggerB(queue,counter)
                  } yield result
                }
              }
    } yield done
}

object OrchestratorWithSourceCounter {
  def layer(): URLayer[SourceA with SourceB with Processor with Sink,Orchestrator] =
    ZLayer.fromFunction(OrchestratorWithSourceCounter(_, _, _, _))
}
