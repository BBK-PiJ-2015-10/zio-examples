package service.external

//import zio.ZIO
//
//import zhttp.http._
//import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
//
//trait HttpExecutorService {
//
//  def executeGet(url: String) : ZIO[Any,Throwable,String]
//
//}
//
//case class HttpExecutorServiceImpl() extends HttpExecutorService {
//
//  override def executeGet(url: String): ZIO[Any, Throwable, String] = {
//    for {
//      _ <- ZIO.logInfo("ale")
//      response <- Client.request(url)
//      //_ <- ZIO.fail(new Exception("ale"))
//    } yield response
//  }
//
//
//}
//
//
//
//
//object HttpExecutorService {
//
//}
