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

package uk.gov.hmrc.r2.selfassessmentapi.models.des

import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class PropertiesAnnualSummarySpec extends JsonSpec {
  "OtherPropertiesAnnualSummary" should {
    "round trip" in {
      roundTripJson(
        OtherPropertiesAnnualSummary(
          annualAllowances = Some(OtherPropertiesAllowances(Some(50), Some(20.20), Some(15.15), Some(12.34))),
          annualAdjustments = Some(OtherPropertiesAdjustments(Some(20.23), Some(50.55), Some(12.34)))))
    }
  }

  "FHLPropertiesAnnualSummary" should {
    "round trip" in {
      roundTripJson(
        FHLPropertiesAnnualSummary(
          annualAllowances = Some(FHLPropertiesAllowances(
            annualInvestmentAllowance = Some(50), 
            otherCapitalAllowance = Some(20.20), 
            businessPremisesRenovationAllowance = Some(74.32), 
            propertyIncomeAllowance = Some(100.25))),
          annualAdjustments = Some(FHLPropertiesAdjustments(
            lossBroughtForward = Some(20.23),
            privateUseAdjustment = Some(50.55),
            balancingCharge = Some(12.34),
            periodOfGraceAdjustment = Some(false),
            businessPremisesRenovationAllowanceBalancingCharges = Some(200.00),
            nonResidentLandlord = Some(true),
            ukFhlRentARoom = Some(FHLPropertiesUkFhlRentARoom(jointlyLet = false)))
          )))
    }
  }
}
