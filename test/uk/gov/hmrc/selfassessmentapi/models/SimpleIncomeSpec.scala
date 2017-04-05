package uk.gov.hmrc.selfassessmentapi.models

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SimpleIncomeSpec extends JsonSpec {
  "SimpleIncome" should {
    "round trip" in {
      roundTripJson(SimpleIncome(500.55))
    }

    "reject a negative amount" in {
      assertValidationErrorWithCode(SimpleIncome(-20.20), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
