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

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders.{FurnishedHolidayLettingBuilder, UKPropertyBuilder}

class FurnishedHolidayLettingSpec extends UnitSpec {

  "AdjustedProfit" should {
    "be equal to (Incomes + BalancingCharges + PrivateUseAdjustment) - (Expenses + CapitalAllowance)" in {

      val letting = FurnishedHolidayLettingBuilder(capitalAllowance = 100)
        .incomes(1200.01, 799.99)
        .withFinancialCosts( 200)
        .withProfessionalFees(200)
        .withPremisesRunningCosts(49.99)
        .withOtherExpenses(50)
        .balancingCharges(10, 20)
        .privateUseAdjustments(50)
        .create()

      FurnishedHolidayLetting.AdjustedProfits(letting) shouldBe 1480

    }

    "be rounded down to the nearest pound" in {
      val letting =  FurnishedHolidayLettingBuilder(capitalAllowance = 0.02)
        .incomes(1299.01)
        .create()

      FurnishedHolidayLetting.AdjustedProfits(letting) shouldBe 1298
    }

    "be 0 if (Expenses + CapitalAllowance) is greater than (Incomes + BalancingCharges + PrivateUseAdjustment)" in {

      val letting = FurnishedHolidayLettingBuilder()
        .incomes(2000)
        .withFinancialCosts(4000)
        .lossBroughtForward(1000)
        .create()

      FurnishedHolidayLetting.AdjustedProfits(letting) shouldBe 0
    }

  }

  "Incomes" should {

    "be empty there are no self employments" in {
      FurnishedHolidayLetting.Incomes(SelfAssessment()) shouldBe empty
    }

    "be computed for a single holiday letting" in {

      val letting = FurnishedHolidayLettingBuilder()
        .incomes(1200.01)
        .create()

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(letting))

      FurnishedHolidayLetting.Incomes(selfAssessment).head.profit shouldBe 1200
    }

    "be computed for multiple self employments" in {

      val letting1 = FurnishedHolidayLettingBuilder()
        .incomes(1200)
        .create()

      val letting2 = FurnishedHolidayLettingBuilder()
        .incomes(800)
        .create()

      val selfAssessment = SelfAssessment(furnishedHolidayLettings = Seq(letting1, letting2))

      val result = FurnishedHolidayLetting.Incomes(selfAssessment)

      result.head.profit shouldBe 1200
      result(1).profit shouldBe 800
    }
  }

  "TotalLossBroughtForward for UK FHL" should {

    "FHL Total(LBF) + Excess UK Property LBF capped at FHL Total(AdjustedProfit)" +
      "i.e CapAt(FHL.Total(LBF) + ExcessUKPropertyLBF, FHL.Total(AdjustedProfit))" in {

      val ukPropertyOne = UKPropertyBuilder().withRentIncomes(4000).lossBroughtForward(5000).create()
      val ukPropertyTwo = UKPropertyBuilder().withRentIncomes(15000).lossBroughtForward(12500).create()
      val ukPropertyThree = UKPropertyBuilder().withRentIncomes(1500).lossBroughtForward(7000).create()

      val furnishedHolidayLettingOne = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(6500).lossBroughtForward(2500).create()
      val furnishedHolidayLettingTwo = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(5000).lossBroughtForward(2000).create()
      val furnishedHolidayLettingThree = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(1500).lossBroughtForward(6000).create()
      val furnishedHolidayLettingFour = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(500).lossBroughtForward(300).create()
      val furnishedHolidayLettingFive = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(500).lossBroughtForward(200).create()

      FurnishedHolidayLetting.UK.CappedTotalLossBroughtForward(SelfAssessment(
          ukProperties = Seq(ukPropertyOne, ukPropertyTwo, ukPropertyThree),
          furnishedHolidayLettings = Seq(furnishedHolidayLettingOne, furnishedHolidayLettingTwo, furnishedHolidayLettingThree,
            furnishedHolidayLettingFour, furnishedHolidayLettingFive)
        )
      ) shouldBe 13000
    }

    "FHL Total(LBF) + Excess UK Property LBF capped at FHL Total(AdjustedProfit) when UKProperty adjusted profit is 0" in {
      val ukPropertyOne = UKPropertyBuilder().lossBroughtForward(5000).create()
      val ukPropertyTwo = UKPropertyBuilder().lossBroughtForward(1250).create()
      val ukPropertyThree = UKPropertyBuilder().lossBroughtForward(7000).create()

      val furnishedHolidayLettingOne = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(3500).lossBroughtForward(2500).create()
      val furnishedHolidayLettingTwo = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(5000).lossBroughtForward(2000).create()
      val furnishedHolidayLettingThree = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(1500).lossBroughtForward(6000).create()
      val furnishedHolidayLettingFour = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(500).lossBroughtForward(300).create()
      val furnishedHolidayLettingFive = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(500).lossBroughtForward(200).create()

      FurnishedHolidayLetting.UK.CappedTotalLossBroughtForward(SelfAssessment(
        ukProperties = Seq(ukPropertyOne, ukPropertyTwo, ukPropertyThree),
        furnishedHolidayLettings = Seq(furnishedHolidayLettingOne, furnishedHolidayLettingTwo, furnishedHolidayLettingThree,
          furnishedHolidayLettingFour, furnishedHolidayLettingFive)
      )
      ) shouldBe 10000
    }

    "FHL Total(LBF) + Excess UK Property LBF capped at FHL Total(AdjustedProfit) when there is no UK Property Income data" in {
      val furnishedHolidayLettingOne = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(3500).lossBroughtForward(2500).create()
      val furnishedHolidayLettingTwo = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(5000).lossBroughtForward(2000).create()
      val furnishedHolidayLettingThree = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(1500).lossBroughtForward(6000).create()
      val furnishedHolidayLettingFour = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(500).lossBroughtForward(300).create()
      val furnishedHolidayLettingFive = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(500).lossBroughtForward(200).create()

      FurnishedHolidayLetting.UK.CappedTotalLossBroughtForward(SelfAssessment(
        furnishedHolidayLettings = Seq(furnishedHolidayLettingOne, furnishedHolidayLettingTwo, furnishedHolidayLettingThree,
          furnishedHolidayLettingFour, furnishedHolidayLettingFive)
      )
      ) shouldBe 10000
    }
  }

  "TotalLossBroughtForward for EEA FHL" should {
    "FHL Total(LBF) capped at FHL Total(AdjustedProfit) for EEA FHL" +
      "i.e CapAt(FHL.EEA.Total(LBF), FHL.EEA.Total(AdjustedProfit))" in {

      val furnishedHolidayLettingOne = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(6500).lossBroughtForward(2500).create()
      val furnishedHolidayLettingTwo = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(5000).lossBroughtForward(2000).create()
      val furnishedHolidayLettingThree = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(1500).lossBroughtForward(6000).create()

      val furnishedHolidayLettingFour = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(3050).lossBroughtForward(3100).create()
      val furnishedHolidayLettingFive = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(1950).lossBroughtForward(2500).create()

      FurnishedHolidayLetting.EEA.CappedTotalLossBroughtForward(SelfAssessment(
        furnishedHolidayLettings = Seq(furnishedHolidayLettingOne, furnishedHolidayLettingTwo, furnishedHolidayLettingThree,
          furnishedHolidayLettingFour, furnishedHolidayLettingFive)
      )
      ) shouldBe 5000

    }
  }

  "TotalLossBroughtForward for FHL" should {
    "FHL UK TotalCappedLBF + FHL EEA TotalCappedLBF" in {

      val ukPropertyOne = UKPropertyBuilder().withRentIncomes(4000).lossBroughtForward(5000).create()
      val ukPropertyTwo = UKPropertyBuilder().withRentIncomes(15000).lossBroughtForward(12500).create()
      val ukPropertyThree = UKPropertyBuilder().withRentIncomes(1500).lossBroughtForward(7000).create()

      val furnishedHolidayLettingOne = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(6500).lossBroughtForward(2500).create()
      val furnishedHolidayLettingTwo = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(5000).lossBroughtForward(2000).create()
      val furnishedHolidayLettingThree = FurnishedHolidayLettingBuilder(location = PropertyLocationType.UK).incomes(1500).lossBroughtForward(6000).create()

      val furnishedHolidayLettingFour = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(3050).lossBroughtForward(3100).create()
      val furnishedHolidayLettingFive = FurnishedHolidayLettingBuilder(location = PropertyLocationType.EEA).incomes(1950).lossBroughtForward(2500).create()

      FurnishedHolidayLetting.CappedTotalLossBroughtForward(SelfAssessment(
        ukProperties = Seq(ukPropertyOne, ukPropertyTwo, ukPropertyThree),
        furnishedHolidayLettings = Seq(furnishedHolidayLettingOne, furnishedHolidayLettingTwo, furnishedHolidayLettingThree,
          furnishedHolidayLettingFour, furnishedHolidayLettingFive)
      )
      ) shouldBe 18000

    }
  }

}
