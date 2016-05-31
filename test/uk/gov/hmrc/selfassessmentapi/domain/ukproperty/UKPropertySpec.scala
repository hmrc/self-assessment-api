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

package uk.gov.hmrc.selfassessmentapi.domain.ukproperty

import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec

class UKPropertySpec extends JsonSpec {

  "UKProperty" should {

    "make a valid json round trip" in {
      roundTripJson(UKProperty.example)
    }

    "reject name with more than 100 characters" in {
      assertValidationError[UKProperty](UKProperty(None, "Abcd" * 100, None, None, None),
        Map("/name" -> MAX_FIELD_LENGTH_EXCEEDED), "Expected invalid uk-property")
    }


    "reject annualInvestmentAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(allowances = Some(Allowances(annualInvestmentAllowance = Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map("/allowances/annualInvestmentAllowance" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk-property")
      }
    }

    "reject businessPremisesRenovationAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(allowances = Some(Allowances(businessPremisesRenovationAllowance = Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map("/allowances/businessPremisesRenovationAllowance" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk-property")
      }
    }

    "reject otherCapitalAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(allowances = Some(Allowances(otherCapitalAllowance = Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map("/allowances/otherCapitalAllowance" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk-property")
      }
    }


    "reject wearAndTearAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val value = UKProperty.example.copy(allowances = Some(Allowances(wearAndTearAllowance = Some(amount))))
        assertValidationError[UKProperty](
          value,
          Map("/allowances/wearAndTearAllowance" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk-property")
      }
    }

    "reject lossBroughtForward with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        assertValidationError[UKProperty](
          UKProperty.example.copy(adjustments = Some(Adjustments(Some(amount)))),
          Map("/adjustments/lossBroughtForward" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk-property")
      }
    }

    "reject rentARoomRelief with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        assertValidationError[UKProperty](
          UKProperty.example.copy(rentARoomRelief = Some(amount)),
          Map("/rentARoomRelief" -> INVALID_MONETARY_AMOUNT), "Expected invalid uk-property")
      }
    }

  }

}
