package service.internal

import service.entity.{RecordApiEntity, RecordSubmissionResponseApiEntity}
import zio.ZIO
import zio.json.DecoderOps

object SinkResponseParser {

  def jsonRecordSubmissionResponse(response: String): ZIO[Any, Throwable, Option[RecordSubmissionResponseApiEntity]] =
    parseJsonResponseHelper(response)

  private def parseJsonResponseHelper(
    response: String
  ): ZIO[Any, Throwable, Option[RecordSubmissionResponseApiEntity]] =
    for {
      eitherErrorResponse <- ZIO.attempt(response).map(_.fromJson[RecordSubmissionResponseApiEntity])
      zioResponse = eitherErrorResponse match {
                      case Left(e) =>
                        ZIO.logWarning(s"Received a malformed response $e") zipRight
                          ZIO.succeed(None)
                      case Right(response) =>
                        if (response.status.equals("ok")) {
                          ZIO.logInfo(s"Received an ok submission ") zipRight ZIO.succeed(Some(response))
                        } else {
                          ZIO.logInfo(s"Received a bad submission ") zipRight ZIO.succeed(Some(response))
                        }
                    }
      maybeResponse <- zioResponse
    } yield maybeResponse

}
