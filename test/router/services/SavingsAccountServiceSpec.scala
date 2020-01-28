/*
 * Copyright 2020 HM Revenue & Customs
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

package router.services

import mocks.config.MockAppConfig
import mocks.connectors.{MockSavingsAccountConnector, MockSelfAssessmentConnector}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import router.constants.Versions.VERSION_2
import router.errors.{IncorrectAPIVersion, UnsupportedAPIVersion}
import support.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class SavingsAccountServiceSpec extends UnitSpec
  with MockSavingsAccountConnector with MockAppConfig with MockSelfAssessmentConnector {

  class Setup {

    object service extends SavingsAccountService(
      mockAppConfig,
      mockSavingsAccountConnector
    )

  }

  implicit val request = FakeRequest()

  "post" should {
    val requestBody: JsObject = Json.obj("accountName" -> "Main account name")
    val responseBody: JsObject = Json.obj("id" -> "SAVKB2UVwUTBQGJ")

    val httpResponse = HttpResponse(CREATED, Some(responseBody))

    "return a HttpResponse" when {
      "the request contains a version 2.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))

        MockSavingsAccountConnector.post(s"/$VERSION_2${request.uri}", requestBody)
          .returns(Future.successful(Right(httpResponse)))

        val result = await(service.post(requestBody))
        result shouldBe Right(httpResponse)
      }
    }

    "return an UnsupportedAPIVersion error" when {
      "the Accept header contains an unsupported API version" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.5.0+json"))

        val result = await(service.post(requestBody))
        result shouldBe Left(UnsupportedAPIVersion)
      }

      "the request contains a version 1.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))

        val result = await(service.post(requestBody))
        result shouldBe Left(UnsupportedAPIVersion)
      }
    }

    "return an IncorrectAPIVersion" when {
      "the Accept header contains an incorrect value" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "incorrect value"))

        val result = await(service.post(requestBody))
        result shouldBe Left(IncorrectAPIVersion)
      }
    }
  }

  "retrieve" should {
    "return a HttpResponse" when {
      "the request contains a version 2.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))
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

        val correlationId = "X-123"
        val httpResponse = HttpResponse(OK, Some(body), Map("CorrelationId" -> Seq(correlationId)))

        MockSavingsAccountConnector.get(s"/$VERSION_2${request.uri}")
          .returns(Future.successful(Right(httpResponse)))

        val result = await(service.get())
        result shouldBe Right(httpResponse)
      }

      "return an UnsupportedAPIVersion error" when {
        "the Accept header contains an unsupported API version" in new Setup {
          implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.5.0+json"))

          val result = await(service.get())
          result shouldBe Left(UnsupportedAPIVersion)
        }

        "the request contains a version 1.0 header" in new Setup {
          implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))

          val result = await(service.get())
          result shouldBe Left(UnsupportedAPIVersion)
        }
      }

      "return an IncorrectAPIVersion" when {
        "the Accept header contains an incorrect value" in new Setup {
          implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "incorrect value"))

          val result = await(service.get())
          result shouldBe Left(IncorrectAPIVersion)
        }
      }
    }
  }

  "amend" should {
    val requestBody = Json.obj("test" -> "body")

    "return a HttpResponse" when {
      "the request contains a version 2.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))
        val response = HttpResponse(204)

        MockSavingsAccountConnector.put(s"/$VERSION_2${request.uri}", requestBody).returns(Future.successful(Right(response)))

        val result = await(service.put(requestBody))
        result shouldBe Right(response)
      }

    }

    "return an UnsupportedAPIVersion error" when {
      "the Accept header contains an unsupported API version" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.5.0+json"))

        val result = await(service.put(requestBody))
        result shouldBe Left(UnsupportedAPIVersion)
      }

      "the request contains a version 1.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))

        val result = await(service.put(requestBody))
        result shouldBe Left(UnsupportedAPIVersion)
      }
    }

    "return an IncorrectAPIVersion" when {
      "the Accept header contains an incorrect value" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "incorrect value"))

        val result = await(service.put(requestBody))
        result shouldBe Left(IncorrectAPIVersion)
      }
    }


  }

}
