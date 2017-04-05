package uk.gov.hmrc.selfassessmentapi.models

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class IncomeSpec extends JsonSpec {
  "Income" should {
    "round trip" in {
      roundTripJson(Income(500.55, Some(100.11)))
    }

    "reject payloads where the taxDeducted is greater than the amount" in {
      assertValidationErrorWithCode(Income(500.55, Some(500.56)), "", ErrorCode.INVALID_TAX_DEDUCTION_AMOUNT)
    }

    "reject payload where the amount is negative" in {
      assertValidationErrorWithCode(Income(-200.55, None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject payloads where the taxDeducted is negative" in {
      assertValidationErrorWithCode(Income(500.55, Some(-100.11)), "/taxDeducted", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "accept payloads where the taxDeducted is equal to the amount" in {
      assertValidationPasses(Income(500.55, Some(500.55)))
    }

    "accept payloads where the taxDeducted is less than the amount" in {
      assertValidationPasses(Income(500.55, Some(500.54)))
    }
  }

}
