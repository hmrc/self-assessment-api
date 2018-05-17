/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.services

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.fixtures.properties.PropertiesBISSFixture
import uk.gov.hmrc.selfassessmentapi.httpparsers.NoSubmissionDataExists
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockPropertiesBISSConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.{NinoInvalid, NinoNotFound, ServerError, ServiceUnavailable, TaxYearInvalid, TaxYearNotFound}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertiesBISSServiceSpec extends ServiceSpec with MockPropertiesBISSConnector {

  class SetUp {
    val service = new PropertiesBISSService {
      override val connector = mockPropertiesBISSConnector
    }
  }

  implicit val hc = HeaderCarrier()

  "PropertiesBISSService getSummary" should {
    "return PropertiesBISS model" when {
      "valid nino and taxyear is passed" in new SetUp {
        MockPropertiesBISSConnector.get(nino, taxYear).
          returns(Future.successful(Right(PropertiesBISSFixture.propertiesBISS())))

        val response = await(service.getSummary(nino, taxYear))
        response shouldBe Right(PropertiesBISSFixture.propertiesBISS())
      }
    }

    "return error" when {
      "invalid nino and taxyear is passed" in new SetUp {
        MockPropertiesBISSConnector.get(nino, taxYear).
          returns(Future.successful(Left(NinoInvalid)))

        val response = await(service.getSummary(nino, taxYear))
        response shouldBe Left(NinoInvalid)
      }
    }

    "return invalid tax year error response" when {
      "valid nino and invalid tax year is supplied" in new SetUp {
        MockPropertiesBISSConnector.get(nino, taxYear).
          returns(Future.successful(Left(TaxYearInvalid)))

        val response = await(service.getSummary(nino, taxYear))
        response shouldBe Left(TaxYearInvalid)
      }
    }

    "return nino not found error response" when {
      "nino supplied not found in the backend" in new SetUp {
        MockPropertiesBISSConnector.get(nino, taxYear).
          returns(Future.successful(Left(NinoNotFound)))

        val response = await(service.getSummary(nino, taxYear))
        response shouldBe Left(NinoNotFound)
      }
    }

    "return tax year not found error response" when {
      "tax year supplied not found in the backend" in new SetUp {
        MockPropertiesBISSConnector.get(nino, taxYear).
          returns(Future.successful(Left(TaxYearNotFound)))

        val response = await(service.getSummary(nino, taxYear))
        response shouldBe Left(TaxYearNotFound)
      }
    }

    "return data not found error response" when {
      "no data found with the supplied details in the backend" in new SetUp {
        MockPropertiesBISSConnector.get(nino, taxYear).
          returns(Future.successful(Left(TaxYearInvalid)))

        val response = await(service.getSummary(nino, taxYear))
        response shouldBe Left(TaxYearInvalid)
      }
    }

    "return server error response" when {
      "unknown error in the backend" in new SetUp {
        MockPropertiesBISSConnector.get(nino, taxYear).
          returns(Future.successful(Left(ServerError)))

        val response = await(service.getSummary(nino, taxYear))
        response shouldBe Left(ServerError)
      }
    }

    "return service unavailable error response" when {
      "backend is not available" in new SetUp {
        MockPropertiesBISSConnector.get(nino, taxYear).
          returns(Future.successful(Left(ServiceUnavailable)))

        val response = await(service.getSummary(nino, taxYear))
        response shouldBe Left(ServiceUnavailable)
      }
    }
  }
}
