import zio._


object Main extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Console.printLine("Starting server")

}
