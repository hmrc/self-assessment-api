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

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.{ErrorCode, Expense, Income}

class PropertiesPeriodSpec extends JsonSpec {

  def propertiesPeriod(from: LocalDate = LocalDate.parse("2017-04-01"),
                       to: LocalDate = LocalDate.parse("2017-04-02"),
                      incomes: Map[IncomeType.IncomeType, Income] = Map(IncomeType.PremiumsOfLeaseGrant -> Income(1000)),
                      expenses: Map[ExpenseType.ExpenseType, Expense] = Map(ExpenseType.PremisesRunningCosts -> Expense(1000.50, Some(22.25))),
                      privateUseAdjustment: Option[BigDecimal] = Some(50.50),
                      balancingCharge: Option[BigDecimal] = Some(12.32)) = {
    PropertiesPeriod(from = from, to = to,
      data = PropertiesPeriodicData(
        incomes = incomes,
        expenses = expenses,
        privateUseAdjustment = privateUseAdjustment,
        balancingCharge = balancingCharge))
  }

  "PropertiesPeriod" should {
    "round trip" in {
      roundTripJson(propertiesPeriod())
    }
  }

  "validate" should {
    "reject a PropertiesPeriod where the `to` date comes before the `from` date" in {
      val period = propertiesPeriod(from = LocalDate.parse("2017-04-02"), to = LocalDate.parse("2017-04-01"))
      assertValidationErrorWithCode(period, "", ErrorCode.INVALID_PERIOD)
    }

    "reject a PropertiesPeriod with a negative privateUseAdjustment" in {
      val period = propertiesPeriod(privateUseAdjustment = Some(-50.50))
      assertValidationErrorWithCode(period, "/privateUseAdjustment", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject a PropertiesPeriod with a negative balancingCharge" in {
      val period = propertiesPeriod(balancingCharge = Some(-50.50))
      assertValidationErrorWithCode(period, "/balancingCharge", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject a PropertiesPeriod with a privateUseAdjustment containing more than 2 decimal places" in {
      val period = propertiesPeriod(privateUseAdjustment = Some(50.555))
      assertValidationErrorWithCode(period, "/privateUseAdjustment", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject a PropertiesPeriod with a balancingCharge containing more than 2 decimal places" in {
      val period = propertiesPeriod(balancingCharge = Some(50.555))
      assertValidationErrorWithCode(period, "/balancingCharge", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
    
    "accept a PropertiesPeriod where the `from` and `to` dates are equal" in {
      val date = LocalDate.parse("2017-04-01")
      val period = propertiesPeriod(from = date, to = date)
      
      assertValidationPasses(period)
    }
  }
}
