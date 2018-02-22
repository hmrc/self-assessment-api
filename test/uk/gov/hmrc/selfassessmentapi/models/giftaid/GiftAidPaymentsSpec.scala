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

package uk.gov.hmrc.selfassessmentapi.models.giftaid

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class GiftAidPaymentsSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  "validate" should {
    "reject GiftAidPayments with a empty value" in {
      assertValidationErrorWithCode[GiftAidPayments](GiftAidPayments(totalPayments = Some(-1)),
        "/totalPayments", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject GiftAidPayments more than 99999999999999.98" in {
      assertValidationErrorWithCode(GiftAidPayments(totalPayments = Some(BigDecimal("999999999999999999.99"))),
        "/totalPayments", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject GiftAidPayments with more than 2 decimal places" in {
      assertValidationErrorWithCode(GiftAidPayments(totalPayments = Some(50.123)),
        "/totalPayments", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject GiftAidPayments with empty non UK charities " in {
      assertValidationErrorWithCode(GiftAidPayments(Some(100.20), Some(80.20),
        Some(100.20), Some(100.20), Some(100.20), Some(GiftAidUKCharityPayments(100.20)), Some(GiftAidNonUKCharityPayments(None, None))),
      "/nonUKCharityGift", ErrorCode.INVALID_GIFT_AID_PAYMENTS)
    }

    "reject GiftAidPayments with invalid gift aid total payments " in {
      assertValidationErrorWithCode(GiftAidPayments(totalPayments = Some(0) ,Some(80.20),
        Some(100.20), Some(100.20), Some(100.20), Some(GiftAidUKCharityPayments(100.20)),
        Some(GiftAidNonUKCharityPayments(Some(10.00), Some(20.00)))),
        "", ErrorCode.TOTAL_PAYMENTS_LESS)
    }

    "reject GiftAidPayments with empty payments " in {
      assertValidationErrorWithCode(GiftAidPayments(),
        "", ErrorCode.INVALID_REQUEST)
    }

    "reject GiftAidPayments with no gift aid total payments " in {
      assertValidationErrorWithCode(GiftAidPayments(totalOneOffPayments = Some(80.20),
        totalPaymentsBeforeTaxYearStart = Some(100.20), totalPaymentsAfterTaxYearEnd = Some(100.20),
        sharesOrSecurities = Some(100.20),ukCharityGift = Some(GiftAidUKCharityPayments(100.20)),
        nonUKCharityGift = Some(GiftAidNonUKCharityPayments(Some(10.00), Some(20.00)))),
        "", ErrorCode.TOTAL_PAYMENTS_LESS)
    }
  }
}
