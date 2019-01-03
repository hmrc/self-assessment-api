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

import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockPropertiesAnnualSummaryConnector
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.models.properties._

import scala.concurrent.Future


class PropertiesAnnualSummaryResourceSpec extends ResourceSpec
  with MockPropertiesAnnualSummaryConnector {

  class Setup {
    val resource = new PropertiesAnnualSummaryResource {
      override val appContext = mockAppContext
      override val authService = mockAuthorisationService
      override val connector = mockPropertiesAnnualSummaryConnector
    }
    mockAPIAction(SourceType.Properties)
  }

  val otherPropertiesAllowances = OtherPropertiesAllowances(
    annualInvestmentAllowance = Some(0.0),
    otherCapitalAllowance = Some(0.0),
    costOfReplacingDomesticItems = Some(0.0),
    zeroEmissionsGoodsVehicleAllowance = Some(0.0)
  )
  val otherPropertiesAdjustments = OtherPropertiesAdjustments(
    lossBroughtForward = Some(0.0),
    privateUseAdjustment = Some(0.0),
    balancingCharge = Some(0.0)
  )
  val otherPropertiesAnnualSummary: PropertiesAnnualSummary = OtherPropertiesAnnualSummary(
    Some(otherPropertiesAllowances),
    Some(otherPropertiesAdjustments)
  )
  val otherPropertiesAnnualSummaryJson = Jsons.Properties.otherAnnualSummary()

  "updateAnnualSummary" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request = FakeRequest().withBody[JsValue](otherPropertiesAnnualSummaryJson)

        MockPropertiesAnnualSummaryConnector.update(nino, PropertyType.OTHER, taxYear, otherPropertiesAnnualSummary)
            .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.updateAnnualSummary(nino, PropertyType.OTHER, taxYear)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrieveAnnualSummary" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        MockPropertiesAnnualSummaryConnector.get(nino, PropertyType.OTHER, taxYear)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieveAnnualSummary(nino, PropertyType.OTHER, taxYear)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
