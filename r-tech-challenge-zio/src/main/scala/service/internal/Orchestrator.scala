package service.internal

import service.external.{SourceA, SourceB}
import zio._

case class Orchestrator(sourceA: SourceA, sourceB: SourceB) {

  private def triggerA(): ZIO[Any, Throwable, String] =
    for {
      _      <- ZIO.logInfo("Requesting record from sourceA")
      record <- sourceA.fetchSourceARecord()
      // put record on collection
      done <- if (!record.isEmpty && record.get.status.equals("done")) {
                ZIO.logInfo(s"Received done record from source A") zipRight ZIO.succeed("done")
              } else {
                ZIO.logInfo(s"Received record from source A") zipRight triggerA()
              }
    } yield done

  private def triggerB(): ZIO[Any, Throwable, String] =
    for {
      _      <- ZIO.logInfo("Requesting record from sourceB")
      record <- sourceA.fetchSourceARecord()
      // put record on collection
      done <- if (!record.isEmpty && record.get.status.equals("done")) {
                ZIO.logInfo(s"Received done record from source B") zipRight ZIO.succeed("done")
              } else {
                ZIO.logInfo(s"Received record from source B") zipRight triggerB()
              }
    } yield done

}
