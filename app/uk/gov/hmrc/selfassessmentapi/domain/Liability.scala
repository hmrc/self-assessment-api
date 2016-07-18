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

package uk.gov.hmrc.selfassessmentapi.domain

import play.api.libs.json.Json

case class Amount(`type`: String, amount: BigDecimal)

object Amount {
  implicit val format = Json.format[Amount]
}

case class Calculation(`type`: String, amount: BigDecimal, percentage: BigDecimal, total: BigDecimal)

object Calculation {
  implicit val format = Json.format[Calculation]
}

case class Income(sourceId: String, taxableProfit: BigDecimal, profit: BigDecimal)

object Income {
  implicit val format = Json.format[Income]
}

case class InterestFromUKBanksAndBuildingSocieties(sourceId: String, totalInterest: BigDecimal)

object InterestFromUKBanksAndBuildingSocieties {
  implicit val format = Json.format[InterestFromUKBanksAndBuildingSocieties]
}

case class DividendsFromUKSources(sourceId: String, totalDividend: BigDecimal)

object DividendsFromUKSources {
  implicit val format = Json.format[DividendsFromUKSources]
}

case class IncomeFromSources(selfEmployment: Seq[Income], employment: Seq[Income], interestFromUKBanksAndBuildingSocieties: Seq[InterestFromUKBanksAndBuildingSocieties],
                             dividendsFromUKSources: Seq[DividendsFromUKSources])

object IncomeFromSources {
  implicit val format = Json.format[IncomeFromSources]
}

case class Deductions(incomeTaxRelief: BigDecimal, personalAllowance: BigDecimal, totalDeductions: BigDecimal) {
  require(totalDeductions >= incomeTaxRelief, "totalDeductions must be greater than or equal to incomeTaxRelief at all times")
}

object Deductions {
  implicit val format = Json.format[Deductions]
}

case class TaxBandSummary(taxBand: String, taxableAmount: BigDecimal, chargedAt: String, tax: BigDecimal)

object TaxBandSummary {
  implicit val format = Json.format[TaxBandSummary]
}

case class IncomeTaxCalculations(payPensionsProfits: Seq[TaxBandSummary], savingsIncome: Seq[TaxBandSummary], dividends: Seq[TaxBandSummary], incomeTaxCharged: BigDecimal)

object IncomeTaxCalculations {
  implicit val format = Json.format[IncomeTaxCalculations]
}

case class IncomeSummary(incomes: IncomeFromSources, deductions: Option[Deductions], totalIncomeReceived: BigDecimal, totalIncomeOnWhichTaxIsDue: BigDecimal)

object IncomeSummary {
  implicit val format = Json.format[IncomeSummary]
}

case class CalculatedAmount(calculations: Seq[Calculation], total: BigDecimal)

object CalculatedAmount {
  implicit val format = Json.format[CalculatedAmount]
}

case class IncomeTaxDeducted(interestFromUk: BigDecimal, total: BigDecimal)

object IncomeTaxDeducted {
  implicit val format = Json.format[IncomeTaxDeducted]
}

case class Liability(id: Option[LiabilityId] = None, income: IncomeSummary, incomeTaxCalculations: IncomeTaxCalculations, credits: Seq[Amount], class4Nic: CalculatedAmount, incomeTaxDeducted: IncomeTaxDeducted, totalTaxDue: BigDecimal)

object Liability {
  implicit val format = Json.format[Liability]

  def example(id: LiabilityId): Liability =
    Liability(
      id = Some(id),
      income = IncomeSummary(
        incomes = IncomeFromSources(
          selfEmployment = Seq(
            Income("self-employment-1", 8200, 10000),
            Income("self-employment-2", 25000, 28000)
          ),
          interestFromUKBanksAndBuildingSocieties = Seq(
            InterestFromUKBanksAndBuildingSocieties("interest-income-1", 100),
            InterestFromUKBanksAndBuildingSocieties("interest-income-2", 200)
          ),
          dividendsFromUKSources = Seq(
            DividendsFromUKSources("dividend-income-1", 1000),
            DividendsFromUKSources("dividend-income-2", 2000)
          ),
          employment = Seq(
            Income("employment-1", 5000, 5000)
          )
        ),
        deductions = Some(Deductions(
          incomeTaxRelief = BigDecimal(5000),
          personalAllowance = BigDecimal(9440),
          totalDeductions = BigDecimal(14440)
        )),
        totalIncomeReceived = BigDecimal(93039),
        totalIncomeOnWhichTaxIsDue = BigDecimal(80000)
      ),
      incomeTaxCalculations = IncomeTaxCalculations(
        payPensionsProfits = Seq(
          TaxBandSummary(taxBand = "basicRate", taxableAmount = 10000, chargedAt = "20%", tax = 2000),
          TaxBandSummary("higherRate", 10000, "40%", 4000),
          TaxBandSummary("additionalHigherRate", 10000, "45%", 4500)
        ),
        savingsIncome = Seq(
          TaxBandSummary("startingRate", 10000, "0%", 0),
          TaxBandSummary("nilRate", 10000, "0%", 0),
          TaxBandSummary("basicRate", 10000, "20%", 2000),
          TaxBandSummary("higherRate", 10000, "40%", 4000),
          TaxBandSummary("additionalHigherRate", 10000, "45%", 4500)
        ),
        dividends = Seq(
          TaxBandSummary("nilRate", 10000, "0%", 0),
          TaxBandSummary("basicRate", 10000, "20%", 2000),
          TaxBandSummary("higherRate", 10000, "40%", 4000),
          TaxBandSummary("additionalHigherRate", 10000, "45%", 4500)
        ),
        incomeTaxCharged = 31500
      ),
      credits = Seq(
        Amount("dividend", BigDecimal(46.6)),
        Amount("interest-charged", BigDecimal(12.25))
      ),
      class4Nic = CalculatedAmount(
        calculations = Seq(
          Calculation("class-4-nic", BigDecimal(33695), BigDecimal(9), BigDecimal(3032.55)),
          Calculation("class-4-nic", BigDecimal(41030), BigDecimal(2), BigDecimal(820.60))
        ),
        total = BigDecimal(3853.15)
      ),
      incomeTaxDeducted = IncomeTaxDeducted(interestFromUk = 0, total = 0),
      totalTaxDue = BigDecimal(25796.95)
    )
}
