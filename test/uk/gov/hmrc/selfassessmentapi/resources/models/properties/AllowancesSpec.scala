/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode

class AllowancesSpec extends JsonSpec {
  "json" should {
    "round trip" in {
      roundTripJson(Allowances(Some(50), Some(12.23), Some(123.45), Some(20)))
    }

    "round trip with empty object" in {
      roundTripJson(Allowances())
    }
  }

  "allowances" should {
    "reject annualInvestmentAllowance with a negative value" in {
      assertValidationErrorWithCode(Allowances(annualInvestmentAllowance = Some(-50)),
        "/annualInvestmentAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject annualInvestmentAllowance with more than 2 decimal places" in {
      assertValidationErrorWithCode(Allowances(annualInvestmentAllowance = Some(50.123)),
        "/annualInvestmentAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject businessPremisesRenovationAllowance with a negative value" in {
      assertValidationErrorWithCode(Allowances(businessPremisesRenovationAllowance = Some(-50)),
        "/businessPremisesRenovationAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject businessPremisesRenovationAllowance with more than two decimal places" in {
      assertValidationErrorWithCode(Allowances(businessPremisesRenovationAllowance = Some(-50)),
        "/businessPremisesRenovationAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject otherCapitalAllowance with a negative value" in {
      assertValidationErrorWithCode(Allowances(otherCapitalAllowance = Some(-50)),
        "/otherCapitalAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject otherCapitalAllowance with more than two decimal places" in {
      assertValidationErrorWithCode(Allowances(otherCapitalAllowance = Some(-50)),
        "/otherCapitalAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject wearAndTearAllowance with a negative value" in {
      assertValidationErrorWithCode(Allowances(wearAndTearAllowance = Some(-50)),
        "/wearAndTearAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject wearAndTearAllowance with more than two decimal places" in {
      assertValidationErrorWithCode(Allowances(wearAndTearAllowance = Some(-50)),
        "/wearAndTearAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
