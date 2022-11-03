package service.external

import service.entity.RecordApiEntity
import service.internal.SourceResponseParser
import zio.{Scope}
import zio.test._

object SourceBSpec extends ZIOSpecDefault {

  val sampleXmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<msg>\n    <id value=\"0ad5aecf126c2ea417d65c6fbfbb7055\"/>\n</msg>"

  val malformedXmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<msg>\n    <49JP65PY0SHAX81YI3VE7QX06761KQ9AYDEGPME13ZUDRNHRTWHV2RD\n    </foo>\n</msg>"

  val sampleJsonResponse = "{\n    \"status\": \"ok\",\n    \"id\": \"f57737e502901644d20f39ecaeb95fe3\"\n}"

  val malformedJsonResponse = "{\n    \"status\": \"ok\",\n    \"weird\": \"f57737e502901644d20f39ecaeb95fe3\"\n}"

  val parseXmlResponseSpec = {
    suite ("parseXmlResponse Spec")(
      test("testing with a properly formed xml response") {
      val expectedResponse = Some(RecordApiEntity("ok",Some("0ad5aecf126c2ea417d65c6fbfbb7055")))
      val response = for {
        record  <- SourceResponseParser.parseXmlResponse(sampleXmlResponse)
      } yield record
      assertZIO(response)(Assertion.equalTo(expectedResponse))
    },
      test("testing with a malformed xml response") {
        val response = for {
          record  <- SourceResponseParser.parseXmlResponse(malformedXmlResponse)
        } yield record
        assertZIO(response)(Assertion.equalTo(None))
      }
    )
  }


  val parseJsonResponseSpec = {
    suite ("parseJsonResponse Spec")(
      test("testing with a properly formed json response") {
        val expectedResponse = Some(RecordApiEntity("ok",Some("f57737e502901644d20f39ecaeb95fe3")))
        val response = for {
          record  <- SourceResponseParser.parseJsonResponse(sampleJsonResponse)
        } yield record
        assertZIO(response)(Assertion.equalTo(expectedResponse))
      },
      test("testing with a malformed response") {
        val response = for {
          record  <- SourceResponseParser.parseXmlResponse(malformedJsonResponse)
        } yield record
        assertZIO(response)(Assertion.equalTo(None))
      }
    )
  }
  //.provide(ZLayer(mockDatabase))

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("SourceResponseParser Spec")(parseXmlResponseSpec,parseJsonResponseSpec)
}
