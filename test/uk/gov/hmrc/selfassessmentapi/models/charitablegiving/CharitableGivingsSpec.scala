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

package uk.gov.hmrc.selfassessmentapi.models.charitablegiving

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class CharitableGivingsSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  "validate" should {
    "reject CharitableGivings with a negative amount" in {
      assertValidationErrorWithCode[CharitableGivings](CharitableGivings(
        Some(GiftAidPayments(currentYear = Some(10000.00),oneOffCurrentYear = Some(-1000.00), currentYearTreatedAsPreviousYear = Some(300.00),
          nextYearTreatedAsCurrentYear = Some(400.00), nonUKCharities = Some(2000.00))),
        Some(Gifts(landAndBuildings = Some(700.00), sharesOrSecurities = Some(600.00), investmentsNonUKCharities = Some(300.00)))),
        "/giftAidPayments/oneOffCurrentYear", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject CharitableGivings more than 10000000000.00" in {
      assertValidationErrorWithCode(CharitableGivings(
        Some(GiftAidPayments(currentYear = Some(BigDecimal("999999999999999999.99")))), None),
        "/giftAidPayments/currentYear", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject CharitableGivings with more than 2 decimal places" in {
      assertValidationErrorWithCode(CharitableGivings(
        Some(GiftAidPayments(currentYear = Some(50.123))), None),
        "/giftAidPayments/currentYear", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "pass CharitableGivings with a valid amounts" in {
      assertValidationPasses[CharitableGivings](CharitableGivings(
        Some(GiftAidPayments(currentYear = Some(10000.00),oneOffCurrentYear = Some(1000.00), currentYearTreatedAsPreviousYear = Some(300.00),
          nextYearTreatedAsCurrentYear = Some(400.00), nonUKCharities = Some(2000.00))),
        Some(Gifts(landAndBuildings = Some(700.00), sharesOrSecurities = Some(600.00), investmentsNonUKCharities = Some(300.00)))))
    }

    "pass CharitableGivings with out Gifts tax relief payments" in {
      assertValidationPasses[CharitableGivings](CharitableGivings(
        Some(GiftAidPayments(currentYear = Some(10000.00),oneOffCurrentYear = Some(1000.00), currentYearTreatedAsPreviousYear = Some(300.00),
          nextYearTreatedAsCurrentYear = Some(400.00), nonUKCharities = Some(2000.00))), None))
    }

    "pass CharitableGivings with out Gifts aid tax relief payments" in {
      assertValidationPasses[CharitableGivings](CharitableGivings(
        None,
        Some(Gifts(landAndBuildings = Some(700.00), sharesOrSecurities = Some(600.00), investmentsNonUKCharities = Some(300.00)))))
    }

    "pass CharitableGivings with out gift aid nonUKCharities" in {
      assertValidationPasses[CharitableGivings](CharitableGivings(
        Some(GiftAidPayments(currentYear = Some(10000.00),oneOffCurrentYear = Some(1000.00), currentYearTreatedAsPreviousYear = Some(300.00),
          nextYearTreatedAsCurrentYear = Some(400.00))),
        Some(Gifts(landAndBuildings = Some(700.00), sharesOrSecurities = Some(600.00), investmentsNonUKCharities = Some(300.00)))))
    }
  }
}
