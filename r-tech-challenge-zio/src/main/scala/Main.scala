import service.external.{SourceA, SourceAImpl}
import zio._
import zio.Console.printLine
import zio._

object Main extends ZIOAppDefault {

  val url = "http://localhost:7299/source/a"

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = (for {
    _ <- ZIO.logInfo("Starting program")
    sourceA <- ZIO.service[SourceA]
    response <- sourceA.fetchSourceARecord()
    _        <- ZIO.logInfo(s"Sucker got $response")
    _ <- ZIO.logInfo("Ending program")
  } yield ()).provide(SourceAImpl.layer(url))

}