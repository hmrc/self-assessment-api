package config

import mocks.services.MockSavingsAccountService
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json, OFormat}
import router.errors.ErrorCode
import router.services.SavingsAccountService
import support.IntegrationSpec
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import scala.util.control.NoStackTrace

/**
  * Integration tests for [[ErrorHandler]].
  */
class ErrorHandlerISpec extends IntegrationSpec with MockSavingsAccountService {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .bindings(bind[SavingsAccountService].toInstance(mockSavingsAccountService))
    .configure(fakeApplicationConfig)
    .build()

  implicit val erFormats: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  "bad json from client" should {
    // WLOG use any microservice with a routes entry
    val uri = "/ni/AA111111A/savings-accounts"

    val badJson = "{invalidJson}"

    "result in a standard V2 formatted response" when {
      "a version 2.0 header received" in {
        Given()
          .theClientIsAuthorised
          .When()
          .post(uri)
          .withBody(badJson)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(BAD_REQUEST)
          .bodyIs(Json.toJson(ErrorCode.invalidRequest))
      }
    }

    "result in a standard V1 formatted response" when {
      "a version 1.0 header received" in {
        Given()
          .theClientIsAuthorised
          .When()
          .post(uri)
          .withBody(badJson)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.1.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(BAD_REQUEST)
          .bodyIs(Json.toJson(ErrorResponse(BAD_REQUEST, "bad request")))
      }
    }
  }

  "bad media type from client" should {
    // WLOG use any microservice with a routes entry
    val uri = "/ni/AA111111A/savings-accounts"

    val badJson = "{invalidJson}"

    "result in a standard V2 formatted response" when {
      "a version 2.0 header received" in {
        Given()
          .theClientIsAuthorised
          .When()
          .post(uri)
          .withBody(badJson)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json"
          )
          .Then()
          .statusIs(UNSUPPORTED_MEDIA_TYPE)
          .bodyIs(Json.toJson(ErrorCode.invalidBodyType))
      }
    }

    "result in a standard V1 formatted response" when {
      "a version 1.0 header received" in {
        Given()
          .theClientIsAuthorised
          .When()
          .post(uri)
          .withBody(badJson)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.1.0+json"
          )
          .Then()
          .statusIs(UNSUPPORTED_MEDIA_TYPE)
          .bodyIs(Json.toJson(ErrorResponse(UNSUPPORTED_MEDIA_TYPE, "Expecting text/json or application/json body")))
      }
    }
  }

  "a server error" should {
    // WLOG use any microservice with a routes entry
    val uri = "/ni/AA111111A/savings-accounts"
    val body: JsValue = Json.parse(
      s"""{
         |    "savingsAccounts": [
         |        {
         |            "id": "SAVKB2UVwUTBQGJ",
         |            "accountName": "Main account name"
         |        },
         |        {
         |            "id": "SAVKB2UVwUTBQGK",
         |            "accountName": "Shares savings account"
         |        }
         |    ]
         |}""".stripMargin
    )

    "result in a standard V2 formatted response" when {
      "a version 2.0 header received" in {
        MockSavingsAccountService.post(body).thenThrow(new RuntimeException("Some error") with NoStackTrace)

        Given()
          .theClientIsAuthorised
          .When()
          .post(uri)
          .withBody(body)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(INTERNAL_SERVER_ERROR)
          .bodyIs(Json.toJson(ErrorCode.internalServerError))
      }
    }


    "result in a standard V1 formatted response" when {
      "a version 1.0 header received" in {
        MockSavingsAccountService.post(body).thenThrow(new RuntimeException("Some error") with NoStackTrace)

        Given()
          .theClientIsAuthorised
          .When()
          .post(uri)
          .withBody(body)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.1.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(INTERNAL_SERVER_ERROR)
          .bodyIs(Json.toJson(ErrorResponse(INTERNAL_SERVER_ERROR, "Some error")))
      }
    }
  }
}
