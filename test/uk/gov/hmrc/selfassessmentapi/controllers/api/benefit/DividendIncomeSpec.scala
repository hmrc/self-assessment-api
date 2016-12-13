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

package uk.gov.hmrc.selfassessmentapi.controllers.api.benefit

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.DividendIncomeType.FromUKCompanies
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.{DividendIncome, DividendIncomeType}

class DividendIncomeSpec extends JsonSpec {

  "format" should {

    "round trip valid Expenses json" in {
      roundTripJson(DividendIncome(`type` = FromUKCompanies, amount = BigDecimal(1000.99)))
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationErrorWithCode(
          DividendIncome(`type` = FromUKCompanies, amount = testAmount),
          "/amount", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject invalid DividendIncome type" in {
      val json = Json.parse(
        """
          |{ "type": "FOO",
          |"amount" : 10000.45
          |}
        """.stripMargin)

      assertValidationErrorsWithCode[DividendIncome](
        json, Map("/type" -> INVALID_VALUE))
    }

    "reject negative amount" in {
      val seIncome = DividendIncome(`type` = FromUKCompanies, amount = BigDecimal(-1000.12))
      assertValidationErrorWithCode[DividendIncome](
        seIncome, "/amount", INVALID_MONETARY_AMOUNT)
    }
  }
}
