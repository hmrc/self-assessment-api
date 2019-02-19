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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json.toJson
import play.api.test.FakeRequest
import uk.gov.hmrc.selfassessmentapi.fixtures.selfemployment.SelfEmploymentBISSFixture
import uk.gov.hmrc.selfassessmentapi.mocks.services.MockSelfEmploymentBISSService
import uk.gov.hmrc.selfassessmentapi.models.Errors._
import uk.gov.hmrc.selfassessmentapi.models.SourceType

import scala.concurrent.Future

class SelfEmploymentBISSResourceSpec extends ResourceSpec
  with MockSelfEmploymentBISSService {

  class Setup {
    val resource = new SelfEmploymentBISSResource(
      mockAppContext,
      mockAuthorisationService,
      mockSelfEmploymentBISSService
    )
    mockAPIAction(SourceType.SelfEmployments)
  }

  val selfEmploymentBISS = SelfEmploymentBISSFixture.selfEmploymentBISS
  val selfEmploymentBISSJson = SelfEmploymentBISSFixture.selfEmploymentBISSJson
  val selfEmploymentId = "test-source-id"

  "getSummary" should {
    "return a 200 with a SelfEmploymentBISS response" when {
      "a valid nino and tax year is supplied and DES return a SelfEmploymentBISS response" in new Setup {
        MockSelfEmploymentBISSService.getSummary(nino, taxYear, selfEmploymentId)
          .returns(Future.successful(Right(selfEmploymentBISS)))

        val result = resource.getSummary(nino, taxYear, selfEmploymentId)(FakeRequest())
        status(result) shouldBe OK
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe selfEmploymentBISSJson
      }
    }

    Seq(
      BAD_REQUEST -> NinoInvalid,
      BAD_REQUEST -> TaxYearInvalid,
      BAD_REQUEST -> SelfEmploymentIDInvalid,
      NOT_FOUND -> NoSubmissionDataExists,
      INTERNAL_SERVER_ERROR -> ServerError,
      INTERNAL_SERVER_ERROR -> ServiceUnavailable
    ).foreach { case (responseCode, errorCode) =>

      s"return a status ($responseCode)" when {
        s"a ${errorCode.code} error is returned from the connector" in new Setup {
          MockSelfEmploymentBISSService.getSummary(nino, taxYear, selfEmploymentId)
            .returns(Future.successful(Left(ErrorWrapper(errorCode, None))))

          val result = resource.getSummary(nino, taxYear, selfEmploymentId)(FakeRequest())
          status(result) shouldBe responseCode
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe toJson(errorCode)
        }
      }
    }
  }
}
