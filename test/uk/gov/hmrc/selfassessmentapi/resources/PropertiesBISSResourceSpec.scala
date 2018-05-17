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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.fixtures.properties.PropertiesBISSFixture
import uk.gov.hmrc.selfassessmentapi.httpparsers.NoSubmissionDataExists
import uk.gov.hmrc.selfassessmentapi.mocks.services.MockPropertiesBISSService
import uk.gov.hmrc.selfassessmentapi.models.Errors.{InternalServerError, NinoInvalid, NinoNotFound, ServerError, ServiceUnavailable, TaxYearInvalid, TaxYearNotFound}
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.services.{AuthorisationService, PropertiesBISSService}

import scala.concurrent.Future

class PropertiesBISSResourceSpec extends BaseResourceSpec with MockPropertiesBISSService {

  val readJson = """{
                    |    "totalIncome": 100.00,
                    |    "totalExpenses": 50.00,
                    |    "totalAdditions": 5.00,
                    |    "totalDeductions": 60.00,
                    |    "netProfit": 50.00,
                    |    "netLoss": 0.00,
                    |    "taxableProfit": 0.00,
                    |    "taxableLoss": 5.00
                    |}"""

  class SetUp {
    val resource = new PropertiesBISSResource {
      override val authService: AuthorisationService = mockAuthorisationService
      override val propertiesBISSService: PropertiesBISSService = mockPropertiesBISSService
      override val appContext: AppContext = mockAppContext
    }
    mockAPIAction(SourceType.Properties)
  }

  implicit val hc = HeaderCarrier()

  "PropertiesBISSResource getSummary" should {
    "return valid response" when {
      "valid nino and tax year is supplied" in new SetUp {
        MockPropertiesBISSService.get(nino, taxYear).
          returns(Future.successful(Right(PropertiesBISSFixture.propertiesBISS())))

        showWithSessionAndAuth(resource.getSummary(nino, taxYear)){
          result => status(result) shouldBe OK
            contentAsJson(result) shouldBe toJson(PropertiesBISSFixture.propertiesBISS())
        }
      }
    }

    "return invalid nino error response" when {
      "invalid nino and valid tax year is supplied" in new SetUp {
        MockPropertiesBISSService.get(nino, taxYear).
          returns(Future.successful(Left(NinoInvalid)))

        showWithSessionAndAuth(resource.getSummary(nino, taxYear)){
          result => status(result) shouldBe BAD_REQUEST
            contentAsJson(result) shouldBe toJson(NinoInvalid)
        }
      }
    }

    "return invalid tax year error response" when {
      "valid nino and invalid tax year is supplied" in new SetUp {
        MockPropertiesBISSService.get(nino, taxYear).
          returns(Future.successful(Left(TaxYearInvalid)))

        showWithSessionAndAuth(resource.getSummary(nino, taxYear)){
          result => status(result) shouldBe BAD_REQUEST
            contentAsJson(result) shouldBe toJson(TaxYearInvalid)
        }
      }
    }

    "return nino not found error response" when {
      "nino supplied not found in the backend" in new SetUp {
        MockPropertiesBISSService.get(nino, taxYear).
          returns(Future.successful(Left(NinoNotFound)))

        showWithSessionAndAuth(resource.getSummary(nino, taxYear)){
          result => status(result) shouldBe NOT_FOUND
            contentAsJson(result) shouldBe toJson(NinoNotFound)
        }
      }
    }

    "return tax year not found error response" when {
      "tax year supplied not found in the backend" in new SetUp {
        MockPropertiesBISSService.get(nino, taxYear).
          returns(Future.successful(Left(TaxYearNotFound)))

        showWithSessionAndAuth(resource.getSummary(nino, taxYear)){
          result => status(result) shouldBe NOT_FOUND
            contentAsJson(result) shouldBe toJson(TaxYearNotFound)
        }
      }
    }

    "return data not found error response" when {
      "no data found with the supplied details in the backend" in new SetUp {
        MockPropertiesBISSService.get(nino, taxYear).
          returns(Future.successful(Left(NoSubmissionDataExists)))

        showWithSessionAndAuth(resource.getSummary(nino, taxYear)){
          result => status(result) shouldBe NOT_FOUND
            contentAsJson(result) shouldBe toJson(NoSubmissionDataExists)
        }
      }
    }

    "return server error response" when {
      "unknown error in the backend" in new SetUp {
        MockPropertiesBISSService.get(nino, taxYear).
          returns(Future.successful(Left(ServerError)))

        showWithSessionAndAuth(resource.getSummary(nino, taxYear)){
          result => status(result) shouldBe INTERNAL_SERVER_ERROR
            contentAsJson(result) shouldBe toJson(ServerError)
        }
      }
    }

    "return service unavailable error response" when {
      "backend is not available" in new SetUp {
        MockPropertiesBISSService.get(nino, taxYear).
          returns(Future.successful(Left(ServiceUnavailable)))

        showWithSessionAndAuth(resource.getSummary(nino, taxYear)){
          result => status(result) shouldBe SERVICE_UNAVAILABLE
            contentAsJson(result) shouldBe toJson(ServiceUnavailable)
        }
      }
    }
  }
}
