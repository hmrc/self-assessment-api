/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import mocks.services.MockSavingsAccountService
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.libs.ws.{WSRequest, WSResponse}
import router.errors.ErrorCode
import router.services.SavingsAccountService
import support.IntegrationSpec
import support.stubs.AuthStub
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import scala.util.control.NoStackTrace

/**
  * Integration tests for [[ErrorHandler]].
  */
class ErrorHandlerISpec extends IntegrationSpec with MockSavingsAccountService {

  override lazy val binding: Option[GuiceableModule] = Some(bind[SavingsAccountService].toInstance(mockSavingsAccountService))

  trait Test {
    implicit val erFormats: OFormat[ErrorResponse] = Json.format[ErrorResponse]

    val nino = "AA123456B"

    val acceptHeader: String

    def uri: String = s"/ni/$nino/savings-accounts"

    def setupStubs(): StubMapping

    def headers: Seq[(String, String)] = Seq((ACCEPT, acceptHeader))

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(headers: _*)
    }
  }

  "bad json from client" should {
    val badJson = "{invalidJson}"

    "result in a standard V2 formatted response" when {
      "a version 2.0 header received" in new Test {
        override def headers: Seq[(String, String)] = super.headers ++ Seq((CONTENT_TYPE, JSON))

        override val acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request.post(badJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe ErrorCode.invalidRequest.asJson
      }
    }

    "result in a standard V1 formatted response" when {
      "a version 1.0 header received" in new Test {
        override def headers: Seq[(String, String)] = super.headers ++ Seq((CONTENT_TYPE, JSON))

        override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request.post(badJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(ErrorResponse(BAD_REQUEST, "bad request"))
      }
    }
  }

  "bad media type from client" should {
    val badJson = "{invalidJson}"

    "result in a standard V2 formatted response" when {
      "a version 2.0 header received" in new Test {
        override val acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request.post(badJson))
        response.status shouldBe UNSUPPORTED_MEDIA_TYPE
        response.json shouldBe ErrorCode.invalidBodyType.asJson
      }
    }

    "result in a standard V1 formatted response" when {
      "a version 1.0 header received" in new Test {
        override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request.post(badJson))
        response.status shouldBe UNSUPPORTED_MEDIA_TYPE
        response.json shouldBe Json.toJson(ErrorResponse(UNSUPPORTED_MEDIA_TYPE, "Expecting text/json or application/json body"))
      }
    }
  }

  "a server error" should {
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
      "a version 2.0 header received" in new Test {
        MockSavingsAccountService.post(body).thenThrow(new RuntimeException("Some error") with NoStackTrace)

        override val acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request.post(body))
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe ErrorCode.internalServerError.asJson
      }
    }

    "result in a standard V1 formatted response" when {
      "a version 1.0 header received" in new Test {
        MockSavingsAccountService.post(body).thenThrow(new RuntimeException("Some error") with NoStackTrace)

        override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request.post(body))
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe Json.toJson(ErrorResponse(INTERNAL_SERVER_ERROR, "Some error"))
      }
    }
  }
}
