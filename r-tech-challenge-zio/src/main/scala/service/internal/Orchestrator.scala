package service.internal

import service.external.{SourceA, SourceB}
import zio._

case class Orchestrator(sourceA: SourceA, sourceB: SourceB) {

  private def triggerA(): ZIO[Any, Throwable, Unit] =
    for {
      _      <- ZIO.logInfo("sale")
      record <- sourceA.fetchSourceARecord()
      // put record on collection
      done <- if (record.get.status.equals("Done")) {
                ZIO.logInfo(s"Received done record from source A") zipRight ZIO.unit
              } else {
                triggerA()
              }
    } yield done

}
