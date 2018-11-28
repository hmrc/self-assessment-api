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

package uk.gov.hmrc.r2.selfassessmentapi.models.properties

import uk.gov.hmrc.r2.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class OtherPropertiesAllowancesSpec extends JsonSpec {
  "json" should {
    "round trip" in {
      roundTripJson(OtherPropertiesAllowances(Some(50), Some(12.23), Some(123.45), Some(20)))
    }

    "round trip with empty object" in {
      roundTripJson(OtherPropertiesAllowances())
    }
  }

  "allowances" should {
    "reject annualInvestmentAllowance with a negative value" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(annualInvestmentAllowance = Some(-50)),
        "/annualInvestmentAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject annualInvestmentAllowance more than 99999999999999.98" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(annualInvestmentAllowance = Some(BigDecimal("99999999999999.99"))),
        "/annualInvestmentAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject annualInvestmentAllowance with more than 2 decimal places" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(annualInvestmentAllowance = Some(50.123)),
        "/annualInvestmentAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject otherCapitalAllowance with a negative value" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(otherCapitalAllowance = Some(-50)),
        "/otherCapitalAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject otherCapitalAllowance more than 99999999999999.98" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(otherCapitalAllowance = Some(BigDecimal("99999999999999.99"))),
        "/otherCapitalAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject otherCapitalAllowance with more than two decimal places" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(otherCapitalAllowance = Some(-50)),
        "/otherCapitalAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject costOfReplacingDomesticItems with a negative value" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(costOfReplacingDomesticItems = Some(-50)),
        "/costOfReplacingDomesticItems", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject costOfReplacingDomesticItems more than 99999999999999.98" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(costOfReplacingDomesticItems = Some(BigDecimal("99999999999999.99"))),
        "/costOfReplacingDomesticItems", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject costOfReplacingDomesticItems with more than two decimal places" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(costOfReplacingDomesticItems = Some(-50)),
        "/costOfReplacingDomesticItems", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject zeroEmissionsGoodsVehicleAllowance with a negative value" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(zeroEmissionsGoodsVehicleAllowance = Some(-50)),
        "/zeroEmissionsGoodsVehicleAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject zeroEmissionsGoodsVehicleAllowance more than 99999999999999.98" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(zeroEmissionsGoodsVehicleAllowance = Some(BigDecimal("99999999999999.99"))),
        "/zeroEmissionsGoodsVehicleAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject zeroEmissionsGoodsVehicleAllowance with more than two decimal places" in {
      assertValidationErrorWithCode(OtherPropertiesAllowances(zeroEmissionsGoodsVehicleAllowance = Some(-50)),
        "/zeroEmissionsGoodsVehicleAllowance", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
