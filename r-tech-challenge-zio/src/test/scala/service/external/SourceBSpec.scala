package service.external

import service.entity.RecordApiEntity
import service.internal.SourceResponseParser
import zio.{Scope, ZIO, ZLayer}
import zio.test._

object SourceBSpec extends ZIOSpecDefault {

  val sampleResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<msg>\n    <id value=\"0ad5aecf126c2ea417d65c6fbfbb7055\"/>\n</msg>"

  val cat =
    test("normalized user names") {
      val expectedResponse = Some(RecordApiEntity("ok",Some("0ad5aecf126c2ea417d65c6fbfbb7055")))
      val response = for {
        record  <- SourceResponseParser.parseXmlResponse(sampleResponse)
      } yield record
      assertZIO(response)(Assertion.equalTo(expectedResponse))
    }
  //.provide(ZLayer(mockDatabase))

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("cat")(cat)
}
