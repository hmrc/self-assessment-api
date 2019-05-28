/*
 * Copyright 2019 HM Revenue & Customs
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

import mocks.connectors.MockPropertyConnector
import play.api.test.FakeRequest
import router.errors.{IncorrectAPIVersion, UnsupportedAPIVersion}
import support.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class PropertyEopsObligationsServiceSpec extends UnitSpec
  with MockPropertyConnector {

  class Setup {

    object service extends PropertyEopsObligationsService(
      mockPropertyConnector
    )
  }

  implicit val request = FakeRequest()

  "get" should {
    "return a HttpResponse" when {
      "the request contains a version 2.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.2.0+json"))
        val response = HttpResponse(200)

        MockPropertyConnector.get("/2.0"+request.uri)
          .returns(Future.successful(Right(response)))

        val result = await(service.get())
        result shouldBe Right(response)
      }
    }

    "return an UnsupportedAPIVersion error" when {

      "the request contains a version 1.0 header" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json"))

        val result = await(service.get())
        result shouldBe Left(UnsupportedAPIVersion)
      }

      "the Accept header contains an unsupported API version" in new Setup {
        implicit val hc = HeaderCarrier(extraHeaders = Seq(ACCEPT -> "application/vnd.hmrc.5.0+json"))

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
