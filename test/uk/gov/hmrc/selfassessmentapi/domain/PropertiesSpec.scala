/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.domain

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.{NinoGenerator, UnitSpec}
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.properties._
import PropertyPeriodOps._

class PropertiesSpec extends UnitSpec {

  val otherAllowances = OtherPropertiesAllowances(Some(50.12), Some(50.55), Some(12.34), Some(23.47))
  val otherAdjustments = OtherPropertiesAdjustments(Some(50.12), Some(38.77), Some(12.20))
  val fhlAllowances = FHLPropertiesAllowances(Some(50.12), Some(50.55))
  val fhlAdjustments = FHLPropertiesAdjustments(Some(50.12), Some(38.77), Some(12.20))

  val otherPeriod: Other.Properties = Other.Properties(
    None,
    LocalDate.parse("2017-04-06"),
    LocalDate.parse("2018-04-05"),
    Some(
      Other.Financials(incomes = Some(Other.Incomes(rentIncome = Some(Income(1000, None)))),
                       expenses = Some(Other.Expenses(premisesRunningCosts = Some(Other.Expense(50.55))))))
  )
  val fhlPeriod: FHL.Properties = FHL.Properties(
    None,
    LocalDate.parse("2017-04-06"),
    LocalDate.parse("2018-04-05"),
    Some(
      FHL.Financials(incomes = Some(FHL.Incomes(rentIncome = Some(SimpleIncome(1234.56)))),
                     expenses = Some(FHL.Expenses(professionalFees = Some(FHL.Expense(500.12))))))
  )

  val properties: Properties = Properties(
    BSONObjectID.generate,
    NinoGenerator().nextNino(),
    DateTime.now(DateTimeZone.UTC),
    AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")),
    FHLPropertiesBucket(
      Map("fhl" -> fhlPeriod),
      Map(TaxYear("2017-18") -> FHLPropertiesAnnualSummary(Some(fhlAllowances), Some(fhlAdjustments)))),
    OtherPropertiesBucket(
      Map("other" -> otherPeriod),
      Map(TaxYear("2017-18") -> OtherPropertiesAnnualSummary(Some(otherAllowances), Some(otherAdjustments)))))

  "annualSummary" should {
    "return an empty annual summary when no annual summary exists for the provided tax year" in {
      val properties = Properties(BSONObjectID.generate, NinoGenerator().nextNino())

      properties.annualSummary(PropertyType.OTHER, TaxYear("2017-18")) shouldBe OtherPropertiesAnnualSummary(None,
                                                                                                             None)
      properties.annualSummary(PropertyType.FHL, TaxYear("2017-18")) shouldBe FHLPropertiesAnnualSummary(None, None)
    }

    "return an annual summary matching the tax year" in {
      properties.annualSummary(PropertyType.OTHER, TaxYear("2017-18")) shouldBe
        OtherPropertiesAnnualSummary(Some(otherAllowances), Some(otherAdjustments))
      properties.annualSummary(PropertyType.FHL, TaxYear("2017-18")) shouldBe
        FHLPropertiesAnnualSummary(Some(fhlAllowances), Some(fhlAdjustments))
    }
  }

  "periodExists" should {
    "return false if a period with the given id does not exist" in {
      OtherPeriodOps.periodExists("cake", properties) shouldBe false
      FHLPeriodOps.periodExists("cake", properties) shouldBe false
    }

    "return true if a period with the given id exists" in {
      OtherPeriodOps.periodExists("other", properties) shouldBe true
      FHLPeriodOps.periodExists("fhl", properties) shouldBe true
    }
  }

  "period" should {
    "return a period with the given id if it exists" in {
      OtherPeriodOps.period("other", properties) shouldBe Some(otherPeriod)
      FHLPeriodOps.period("fhl", properties) shouldBe Some(fhlPeriod)
    }

    "return None if a period with the given id does not exist" in {
      OtherPeriodOps.period("cake", properties) shouldBe None
      FHLPeriodOps.period("cake", properties) shouldBe None
    }
  }

  "setPeriodsTo" should {
    "set the periods map for the period specified by the given id" in {
      val newOtherPeriod = otherPeriod.copy(financials = otherPeriod.financials.map(_.copy(incomes = None)))
      val newFhlPeriod =
        fhlPeriod.copy(financials = fhlPeriod.financials.map(_.copy(incomes = None)))

      val otherProps =
        OtherPeriodOps.setPeriodsTo("other", newOtherPeriod, properties)
      val fhlProps = FHLPeriodOps.setPeriodsTo("fhl", newFhlPeriod, properties)

      OtherPeriodOps.period("other", otherProps) shouldBe Some(newOtherPeriod)
      FHLPeriodOps.period("fhl", fhlProps) shouldBe Some(newFhlPeriod)
    }
  }
}
