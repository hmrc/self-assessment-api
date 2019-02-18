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

package uk.gov.hmrc.selfassessmentapi.services

import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentBISSConnector
import uk.gov.hmrc.selfassessmentapi.fixtures.selfemployment.SelfEmploymentBISSFixture
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockSelfEmploymentBISSConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.ErrorWrapper
import uk.gov.hmrc.selfassessmentapi.models.{Errors, TaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SelfEmploymentBISSServiceSpec extends UnitSpec with MockitoSugar with MockSelfEmploymentBISSConnector {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val target = new SelfEmploymentBISSService (mockSelfEmploymentBISSConnector)

  "Calling .getSummary" when {

    "self employment ID is invalid" should {
      "return a SELF_EMPLOYMENT_ID_INVALID error" in {
        val expected = Left(ErrorWrapper(Errors.SelfEmploymentIDInvalid, None))

        val result = target.getSummary(Nino("AA123456A"), TaxYear("2017-18"), "invalidID")

        await(result) shouldBe expected
      }
    }

    "self employment ID is valid" should {
      "return a valid response" in {

        val expected = Right(SelfEmploymentBISSFixture.selfEmploymentBISS)

        MockSelfEmploymentBISSConnector.getSummary(Nino("AA123456A"), TaxYear("2017-18"), "XKIS00000000988")
          .returns(Future.successful(expected))

        val result = target.getSummary(Nino("AA123456A"), TaxYear("2017-18"), "XKIS00000000988")

        await(result) shouldBe expected
      }
    }

  }
}