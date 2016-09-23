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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.ExpenseType.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.IncomeType._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SelfAssessment, UkPropertyIncome}
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders.{FurnishedHolidayLettingBuilder, UKPropertyBuilder}

class UKPropertySpec extends UnitSpec {

  def income(incomeType: IncomeType, amount: BigDecimal) = UKPropertiesIncomeSummary("", incomeType, amount)
  def privateUseAdjustment(amount: BigDecimal) = UKPropertiesPrivateUseAdjustmentSummary("", amount)
  def balancingCharge(amount: BigDecimal) = UKPropertiesBalancingChargeSummary("", amount)
  def expense(expenseType: ExpenseType, amount: BigDecimal) = UKPropertiesExpenseSummary("", expenseType, amount)

  "UK property incomes" should {
    "compute adjusted profits for UK properties" in {
      val dummyID = BSONObjectID.generate

      val property = UKPropertyBuilder(rentARoomRelief = 500, objectID = dummyID)
        .withRentIncomes(500)
        .withPremiumsOfLeaseGrantIncomes(500)
        .withReversePremiumsIncomes(500)
        .withPremisesRunningCosts(100)
        .withRepairsAndMaintenance(100)
        .withFinancialCosts(100)
        .withProfessionalFees(100)
        .withCostOfServicesFees(100)
        .withOtherExpenses(100)
        .privateUseAdjustment(500)
        .balancingCharges(500)
        .withAllowances(
          annualInvestmentAllowance = 100,
          otherCapitalAllowance = 100,
          wearAndTearAllowance = 100,
          businessPremisesRenovationAllowance = 100)
        .create()

      val selfAssessment = SelfAssessment(ukProperties = Seq(property))

      UKProperty.Incomes(selfAssessment) should contain theSameElementsAs
        Seq(UkPropertyIncome(dummyID.stringify, profit = 1000))
    }

    "ensure minimum value for the UK property profit is zero" in {
      val dummyID = BSONObjectID.generate

      val property = UKPropertyBuilder(objectID = dummyID)
        .withRentIncomes(500)
        .withPremisesRunningCosts(1000)
        .create()

      val selfAssessment = SelfAssessment(ukProperties = Seq(property))

      UKProperty.Incomes(selfAssessment) should contain theSameElementsAs
        Seq(UkPropertyIncome(dummyID.stringify, profit = 0))
    }

    "compute adjusted profits for each UK property" in {
      val dummyID = BSONObjectID.generate

      val propertyOne = UKPropertyBuilder(objectID = dummyID)
        .withRentIncomes(1000)
        .withPremisesRunningCosts(500)
        .create()

      val propertyTwo = UKPropertyBuilder(objectID = dummyID)
        .withRentIncomes(1000)
        .withPremisesRunningCosts(200)
        .create()

      val selfAssessment = SelfAssessment(ukProperties = Seq(propertyOne, propertyTwo))

      UKProperty.Incomes(selfAssessment) should contain theSameElementsAs
        Seq(UkPropertyIncome(dummyID.stringify, profit = 500), UkPropertyIncome(dummyID.stringify, profit = 800))
    }

    "computed profits should be rounded down to the nearest pound" in {
      val dummyID = BSONObjectID.generate

      val property = UKPropertyBuilder(objectID = dummyID)
        .withRentIncomes(500.55)
        .withPremiumsOfLeaseGrantIncomes(500.20)
        .withPremisesRunningCosts(100.11)
        .lossBroughtForward(200.22)
        .create()

      val selfAssessment = SelfAssessment(ukProperties = Seq(property))

      UKProperty.Incomes(selfAssessment) should contain theSameElementsAs
        Seq(UkPropertyIncome(dummyID.stringify, profit = 900))
    }
  }

  "TotalLossBroughtForward" should {
    "sum of all loss brought forward capped at total adjusted profits" in {
      val dummyID = BSONObjectID.generate

      val propertyOne = UKPropertyBuilder(objectID = dummyID)
        .withRentIncomes(5000.99)
        .lossBroughtForward(6000.34)
        .create()

      val propertyTwo = UKPropertyBuilder(objectID = dummyID)
        .withRentIncomes(3000.45)
        .lossBroughtForward(2100.34)
        .create()

      val selfAssessment = SelfAssessment(ukProperties = Seq(propertyOne, propertyTwo))

      UKProperty.CappedTotalLossBroughtForward(selfAssessment) shouldBe 8002
    }
  }

  "Excess UK FHL Loss brought forward" should {
    "not be added to UK Properties Loss brought forward" in {
      //LBF: 3000 Overflow: 0
      val ukPropertyOne = UKPropertyBuilder().withRentIncomes(10000).lossBroughtForward(3000).create()
      val ukPropertyTwo = UKPropertyBuilder().withRentIncomes(2000).lossBroughtForward(0).create()
      val ukPropertyThree = UKPropertyBuilder().withRentIncomes(12000).lossBroughtForward(0).create()

      //LBF: 24000 Overflow: 14000
      val furnishedHolidayLettingOne = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(10000).lossBroughtForward(3000).create()
      val furnishedHolidayLettingTwo = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(2000).lossBroughtForward(20000).create()
      val furnishedHolidayLettingThree = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(12000).lossBroughtForward(15000).create()

      val selfAssessment = SelfAssessment(ukProperties = Seq(ukPropertyOne, ukPropertyTwo, ukPropertyThree),
        furnishedHolidayLettings = Seq(furnishedHolidayLettingOne, furnishedHolidayLettingTwo, furnishedHolidayLettingThree))

      UKProperty.CappedTotalLossBroughtForward(selfAssessment) shouldBe 3000
    }
  }

  "UkProperty.TotalTaxPaid" should {

    "be equal to sum of all uk property tax paid" in {
      val propertyOne = UKPropertyBuilder()
        .taxesPaid(500, 600)
        .create()

      val propertyTwo = UKPropertyBuilder()
        .taxesPaid(400, 300)
        .create()

      UKProperty.TotalTaxPaid(SelfAssessment(
        ukProperties = Seq(propertyOne, propertyTwo))) shouldBe 1800
    }

    "be equal to rounded up sum of all uk property tax paid" in {
      val propertyOne = UKPropertyBuilder()
        .taxesPaid(500.09, 600.86)
        .create()

      val propertyTwo = UKPropertyBuilder()
        .taxesPaid(400.67, 300.34)
        .create()

      UKProperty.TotalTaxPaid(SelfAssessment(
        ukProperties = Seq(propertyOne, propertyTwo))) shouldBe 1801.96
    }
  }

}
