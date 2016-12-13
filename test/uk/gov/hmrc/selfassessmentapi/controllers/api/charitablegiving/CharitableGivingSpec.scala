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

package uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class CharitableGivingSpec extends JsonSpec {

  "format" should {
    "round trip valid CharitableGiving json" in {
      roundTripJson(CharitableGiving.example())
    }
  }

  "validate" should {

    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = Some(testAmount)))),
          "/giftAidPayments/totalInTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(oneOff = Some(testAmount)))),
          "/giftAidPayments/oneOff", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(toNonUkCharities = Some(testAmount)))),
          "/giftAidPayments/toNonUkCharities", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(carriedBackToPreviousTaxYear = Some(testAmount)))),
          "/giftAidPayments/carriedBackToPreviousTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(carriedFromNextTaxYear = Some(testAmount)))),
          "/giftAidPayments/carriedFromNextTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(sharesSecurities = Some(SharesAndSecurities(totalInTaxYear = testAmount))),
          "/sharesSecurities/totalInTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(sharesSecurities = Some(SharesAndSecurities(totalInTaxYear = 100, toNonUkCharities = Some(testAmount)))),
          "/sharesSecurities/toNonUkCharities", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(landProperties = Some(LandAndProperties(totalInTaxYear = testAmount))),
          "/landProperties/totalInTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(landProperties = Some(LandAndProperties(totalInTaxYear = 100, toNonUkCharities = Some(testAmount)))),
          "/landProperties/toNonUkCharities", INVALID_MONETARY_AMOUNT
          )
      }
    }

    "reject negative amounts" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-1)).foreach { testAmount =>
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = Some(testAmount)))),
          "/giftAidPayments/totalInTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(oneOff = Some(testAmount)))),
          "/giftAidPayments/oneOff", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(toNonUkCharities = Some(testAmount)))),
          "/giftAidPayments/toNonUkCharities", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(carriedBackToPreviousTaxYear = Some(testAmount)))),
          "/giftAidPayments/carriedBackToPreviousTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(giftAidPayments = Some(GiftAidPayments(carriedFromNextTaxYear = Some(testAmount)))),
          "/giftAidPayments/carriedFromNextTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(sharesSecurities = Some(SharesAndSecurities(totalInTaxYear = testAmount))),
          "/sharesSecurities/totalInTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(sharesSecurities = Some(SharesAndSecurities(totalInTaxYear = 100, toNonUkCharities = Some(testAmount)))),
          "/sharesSecurities/toNonUkCharities", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(landProperties = Some(LandAndProperties(totalInTaxYear = testAmount))),
          "/landProperties/totalInTaxYear", INVALID_MONETARY_AMOUNT
        )
        assertValidationErrorWithCode(
          CharitableGiving(landProperties = Some(LandAndProperties(totalInTaxYear = 100, toNonUkCharities = Some(testAmount)))),
          "/landProperties/toNonUkCharities", INVALID_MONETARY_AMOUNT
        )
      }
    }

    "reject if oneOff is provided, but totalInTaxYear is not present" in {
      assertValidationErrorWithCode(
        CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = None, oneOff = Some(100)))),
        "/giftAidPayments", UNDEFINED_REQUIRED_ELEMENT
      )
    }

    "reject if toNonUkCharities is provided, but totalInTaxYear is not present" in {
      assertValidationErrorWithCode(
        CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = None, toNonUkCharities = Some(100)))),
        "/giftAidPayments", UNDEFINED_REQUIRED_ELEMENT
      )
    }

    "reject if carriedBackToPreviousTaxYear is provided, but totalInTaxYear is not present" in {
      assertValidationErrorWithCode(
        CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = None, carriedBackToPreviousTaxYear = Some(100)))),
        "/giftAidPayments", UNDEFINED_REQUIRED_ELEMENT
      )
    }

    "reject if oneOff is bigger than totalInTaxYear" in {
      assertValidationErrorWithCode(
        CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = Some(100), oneOff = Some(101)))),
        "/giftAidPayments", MAXIMUM_AMOUNT_EXCEEDED
      )
    }

    "reject if toNonUkCharities is bigger than totalInTaxYear" in {
      assertValidationErrorWithCode(
        CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = Some(100), toNonUkCharities = Some(101)))),
        "/giftAidPayments", MAXIMUM_AMOUNT_EXCEEDED
      )
    }

    "reject if carriedBackToPreviousTaxYear is bigger than totalInTaxYear" in {
      assertValidationErrorWithCode(
        CharitableGiving(giftAidPayments = Some(GiftAidPayments(totalInTaxYear = Some(100), carriedBackToPreviousTaxYear = Some(101)))),
        "/giftAidPayments", MAXIMUM_AMOUNT_EXCEEDED
      )
    }

    "reject if sharesAndSecurities.toNonUkCharities is bigger than sharesAndSecurities.totalInTaxYear" in {
      assertValidationErrorWithCode(
        CharitableGiving(sharesSecurities = Some(SharesAndSecurities(totalInTaxYear = 100, toNonUkCharities = Some(101)))),
        "/sharesSecurities", MAXIMUM_AMOUNT_EXCEEDED
      )
    }

    "reject if landAndProperties.toNonUkCharities is bigger than landAndProperties.totalInTaxYear" in {
      assertValidationErrorWithCode(
        CharitableGiving(landProperties = Some(LandAndProperties(totalInTaxYear = 100, toNonUkCharities = Some(101)))),
        "/landProperties", MAXIMUM_AMOUNT_EXCEEDED
      )
    }
  }
}
