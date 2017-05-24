/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.properties

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode

class FHLPropertiesAllowancesSpec extends JsonSpec {
  "FHLPropertiesAllowancesSpec" should {
    "round trip" in {
      roundTripJson(FHLPropertiesAllowances(Some(50.50), Some(12.12)))
    }

    "round trip empty json" in {
      roundTripJson(FHLPropertiesAllowances())
    }
  }

  "validate" should {
    "reject annualInvestmentAllowance with a negative value" in {
      assertValidationErrorWithCode(FHLPropertiesAllowances(annualInvestmentAllowance = Some(-50)),
        "/annualInvestmentAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject annualInvestmentAllowance more than 99999999999999.98" in {
      assertValidationErrorWithCode(FHLPropertiesAllowances(annualInvestmentAllowance = Some(BigDecimal("99999999999999.99"))),
        "/annualInvestmentAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject annualInvestmentAllowance with more than 2 decimal places" in {
      assertValidationErrorWithCode(FHLPropertiesAllowances(annualInvestmentAllowance = Some(50.123)),
        "/annualInvestmentAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject otherCapitalAllowance with a negative value" in {
      assertValidationErrorWithCode(FHLPropertiesAllowances(otherCapitalAllowance = Some(-50)),
        "/otherCapitalAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject otherCapitalAllowance more than 99999999999999.98" in {
      assertValidationErrorWithCode(FHLPropertiesAllowances(otherCapitalAllowance = Some(BigDecimal("99999999999999.99"))),
        "/otherCapitalAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject otherCapitalAllowance with more than 2 decimal places" in {
      assertValidationErrorWithCode(FHLPropertiesAllowances(otherCapitalAllowance = Some(50.123)),
        "/otherCapitalAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
