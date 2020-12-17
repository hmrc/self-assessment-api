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
import mocks.connectors.{MockCrystallisationConnector, MockSelfAssessmentConnector}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import router.constants.Versions.VERSION_2
import router.errors.{IncorrectAPIVersion, UnsupportedAPIVersion}
import support.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class CrystallisationServiceSpec extends UnitSpec
  with MockCrystallisationConnector
  with MockAppConfig
  with MockSelfAssessmentConnector {

  class Setup {

    object service extends CrystallisationService (
      mockAppConfig,
      mockCrystallisationConnector,
      mockSelfAssessmentConnector
    )

  }

  implicit val request = FakeRequest()

  val version2ConfigEnabled =  Configuration("crystallisation-version-2.enabled" -> true)
  val version2ConfigDisabled = Configuration("crystallisation-version-2.enabled" -> false)


  "post" should {
    val requestBody = Json.obj("test" -> "body")

    "return a HttpResponse" when {
      "the request contains a version 2 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))
        val response = HttpResponse(204, None.orNull)

        MockCrystallisationConnector.post(s"/$VERSION_2${request.uri}", requestBody)
          .returns(Future.successful(Right(response)))

        val result = await(service.post(requestBody))
        result shouldBe Right(response)
      }
    }

    "return an UnsupportedAPIVersion error" when {
      "the accept header contains an unsupported version number" in new Setup {
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

  "post empty" should {

    "return a HttpResponse" when {
      "the request contains a version 2 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))
        val response = HttpResponse(204, None.orNull)

        MockCrystallisationConnector.postEmpty(s"/$VERSION_2${request.uri}")
          .returns(Future.successful(Right(response)))

        val result = await(service.postEmpty)
        result shouldBe Right(response)
      }
    }

    "return an UnsupportedAPIVersion error" when {
      "the accept header contains an unsupported version number" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.5.0+json"))

        val result = await(service.postEmpty)
        result shouldBe Left(UnsupportedAPIVersion)
      }

      "the request contains a version 1.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))

        val result = await(service.postEmpty)
        result shouldBe Left(UnsupportedAPIVersion)
      }
    }

    "return an IncorrectAPIVersion" when {
      "the Accept header contains an incorrect value" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "incorrect value"))

        val result = await(service.postEmpty)
        result shouldBe Left(IncorrectAPIVersion)
      }
    }
  }

  "retrieve" should {

    "return a HttpResponse" when {

      val body: JsValue = Json.parse(
        """
          |{
          |  "obligations": [
          |    {
          |    	"identification": {
          |				"incomeSourceType": "ITSA",
          |				"referenceNumber": "AB123456A",
          |				"referenceType": "NINO"
          |			},
          |    "obligationDetails": [
          |      {
          |        "status": "O",
          |        "inboundCorrespondenceFromDate": "2018-02-01",
          |        "inboundCorrespondenceToDate": "2018-02-28",
          |        "inboundCorrespondenceDateReceived": "2018-04-01",
          |        "inboundCorrespondenceDueDate": "2018-05-28"
          |      }
          |    ]
          |    }
          |  ]
          |}
        """.stripMargin)


      "the request contains a version 1.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))
        val response = HttpResponse(200, None.orNull)

        MockSelfAssessmentConnector.get(request.uri)
          .returns(Future.successful(Right(response)))

        val result = await(service.get())
        result shouldBe Right(response)
      }

      "the request contains a version 2.0 header" in new Setup {

        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))

        val httpResponse = HttpResponse(OK, body.toString())

        MockCrystallisationConnector.get(s"/$VERSION_2${request.uri}")
          .returns(Future.successful(Right(httpResponse)))

        val result = await(service.get())
        result shouldBe Right(httpResponse)
      }
    }
  }

}
