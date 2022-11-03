package service.external

import service.entity.RecordApiEntity
import service.internal.SourceResponseParser
import zio.{Scope, ZIO, ZLayer}
import zio.test._

object SourceBSpec extends ZIOSpecDefault {

  val sampleResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<msg>\n    <id value=\"0ad5aecf126c2ea417d65c6fbfbb7055\"/>\n</msg>"

  val malformedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<msg>\n    <49JP65PY0SHAX81YI3VE7QX06761KQ9AYDEGPME13ZUDRNHRTWHV2RD\n    </foo>\n</msg>"

  val parseXmlResponseSpec = {
    suite ("parseXmlResponse Spec")(
      test("testing with a properly formed reponse") {
      val expectedResponse = Some(RecordApiEntity("ok",Some("0ad5aecf126c2ea417d65c6fbfbb7055")))
      val response = for {
        record  <- SourceResponseParser.parseXmlResponse(sampleResponse)
      } yield record
      assertZIO(response)(Assertion.equalTo(expectedResponse))
    },
      test("testing with a malformed response") {
        val response = for {
          record  <- SourceResponseParser.parseXmlResponse(malformedResponse)
        } yield record
        assertZIO(response)(Assertion.equalTo(None))
      }
    )
  }
  //.provide(ZLayer(mockDatabase))

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("sSourceReponseParser Spec")(parseXmlResponseSpec)
}
