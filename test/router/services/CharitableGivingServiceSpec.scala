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
import mocks.connectors.{MockCharitableGivingConnector, MockSelfAssessmentConnector}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.FakeRequest
import router.constants.Versions.VERSION_2
import router.errors.{IncorrectAPIVersion, UnsupportedAPIVersion}
import support.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class CharitableGivingServiceSpec extends UnitSpec
  with MockCharitableGivingConnector with MockAppConfig with MockSelfAssessmentConnector {

  class Setup {

    object service extends CharitableGivingService(
      mockAppConfig,
      mockCharitableGivingConnector
    )
    
  }

  implicit val request = FakeRequest()

  "amend" should {
    val requestBody = Json.obj("test" -> "body")

    "return a HttpResponse" when {
      "the request contains a version 2.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))
        val response = HttpResponse(204)

        MockCharitableGivingConnector.put(s"/$VERSION_2${request.uri}", requestBody)
          .returns(Future.successful(Right(response)))

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

  "retrieve" should {
    "return a HttpResponse" when {
      "the request contains a version 2.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))
        val body = Json.parse(
          s"""{
             |  "giftAidPayments": {
             |    "specifiedYear": 10000.00,
             |    "oneOffSpecifiedYear": 1000.00,
             |    "specifiedYearTreatedAsPreviousYear": 300.00,
             |    "followingYearTreatedAsSpecifiedYear": 400.00,
             |    "nonUKCharities": 2000.00,
             |    "nonUKCharityNames": ["International Charity A","International Charity B"]
             |  },
             |  "gifts": {
             |    "landAndBuildings": 700.00,
             |    "sharesOrSecurities": 600.00,
             |    "investmentsNonUKCharities": 300.00,
             |    "investmentsNonUKCharityNames": ["International Charity C","International Charity D"]
             |  }
             |}""".stripMargin
        )

        val correlationId = "X-123"
        val httpResponse = HttpResponse(OK, Some(body), Map("CorrelationId" -> Seq(correlationId)))

        MockCharitableGivingConnector.get(s"/$VERSION_2${request.uri}")
          .returns(Future.successful(Right(httpResponse)))

        val result = await(service.get())
        result shouldBe Right(httpResponse)
      }
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
