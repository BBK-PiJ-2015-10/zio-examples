import Main.urlB
import service.external.{SourceA, SourceAImpl, SourceB, SourceBImpl}
import zio._
import zio.Console.printLine
import zio._

object Main extends ZIOAppDefault {

  val urlA = "http://localhost:7299/source/a"
  val urlB = "http://localhost:7299/source/b"

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = (for {
    _ <- ZIO.logInfo("Starting program")
    sourceA <- ZIO.service[SourceA]
    response <- sourceA.fetchSourceARecord()
    _        <- ZIO.logInfo(s"Sucker got $response")
    //_ <- ZIO.logInfo("Ending program")

    //_ <- ZIO.logInfo("Starting program")
    sourceB <- ZIO.service[SourceB]
    response <- sourceB.fetchSourceBRecord()
    _        <- ZIO.logInfo(s"Fucker got $response")
    _ <- ZIO.logInfo("Ending program")

 // } yield ()).provide(SourceBImpl.layer(urlB))
  } yield ()).provide(SourceAImpl.layer(urlA),SourceBImpl.layer(urlB))

}