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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps.Math

case class MongoLiability(id: BSONObjectID,
                          liabilityId: LiabilityId,
                          saUtr: SaUtr,
                          taxYear: TaxYear,
                          createdDateTime: DateTime,
                          profitFromSelfEmployments: Seq[SelfEmploymentIncome] = Nil,
                          interestFromUKBanksAndBuildingSocieties: Seq[InterestFromUKBanksAndBuildingSocieties] = Nil,
                          dividendsFromUKSources: Seq[DividendsFromUKSources] = Nil,
                          totalIncomeReceived: Option[BigDecimal] = None,
                          payPensionProfitsReceived: Option[BigDecimal] = None,
                          totalTaxableIncome: Option[BigDecimal] = None,
                          totalAllowancesAndReliefs: Option[BigDecimal] = None,
                          deductionsRemaining: Option[BigDecimal] = None,
                          totalIncomeOnWhichTaxIsDue: Option[BigDecimal] = None,
                          payPensionsProfitsIncome: Seq[TaxBandAllocation] = Nil,
                          savingsIncome: Seq[TaxBandAllocation] = Nil,
                          dividendsIncome: Seq[TaxBandAllocation] = Nil,
                          allowancesAndReliefs: AllowancesAndReliefs = AllowancesAndReliefs(),
                          incomeTaxDeducted: Option[IncomeTaxDeducted] = None) extends Math {

  def toLiability =
    Liability(
      id = Some(liabilityId),
      income = IncomeSummary(
        incomes = IncomeFromSources(
          selfEmployment = profitFromSelfEmployments.map(_.toIncome),
          interestFromUKBanksAndBuildingSocieties = interestFromUKBanksAndBuildingSocieties,
          dividendsFromUKSources = dividendsFromUKSources,
          employment = Nil
        ),
        deductions = Some(Deductions(
          incomeTaxRelief = allowancesAndReliefs.incomeTaxRelief.getOrElse(0),
          personalAllowance = allowancesAndReliefs.personalAllowance.getOrElse(0),
          totalDeductions = sum(allowancesAndReliefs.incomeTaxRelief, allowancesAndReliefs.personalAllowance)
        )),
        totalIncomeReceived = totalIncomeReceived.getOrElse(0),
        totalIncomeOnWhichTaxIsDue = totalIncomeOnWhichTaxIsDue.getOrElse(0)
      ),
      incomeTaxCalculations = IncomeTaxCalculations(
        payPensionsProfits = payPensionsProfitsIncome.map(_.toTaxBandSummary),
        savingsIncome = savingsIncome.map(_.toTaxBandSummary),
        dividends = dividendsIncome.map(_.toTaxBandSummary),
        incomeTaxCharged = (payPensionsProfitsIncome ++ savingsIncome ++ dividendsIncome).map(_.tax).sum
      ),
      credits = Nil,
      class4Nic = CalculatedAmount(calculations = Nil, total = 0),
      incomeTaxDeducted = incomeTaxDeducted.getOrElse(IncomeTaxDeducted(0, 0)),
      totalTaxDue = 0
    )

  def totalSavingsIncome = interestFromUKBanksAndBuildingSocieties.map(_.totalInterest).sum
}

case class SelfEmploymentIncome(sourceId: SourceId, taxableProfit: BigDecimal, profit: BigDecimal, lossBroughtForward: BigDecimal) {

  def toIncome = Income(sourceId, taxableProfit, profit)
}

case class TaxBandAllocation(amount: BigDecimal, taxBand: TaxBand) extends Math {

  def toTaxBandSummary = uk.gov.hmrc.selfassessmentapi.domain.TaxBandSummary(taxBand.name, amount, s"${taxBand.chargedAt}%", tax)

  def tax: BigDecimal = roundDown(amount * taxBand.chargedAt / 100)

  def available: BigDecimal = positiveOrZero(taxBand.width - amount)
}

case class AllowancesAndReliefs(personalAllowance: Option[BigDecimal] = None, personalSavingsAllowance: Option[BigDecimal] = None, incomeTaxRelief: Option[BigDecimal] = None, savingsStartingRate: Option[BigDecimal] = None)

object MongoLiability {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val incomeFormats = Json.format[SelfEmploymentIncome]
  implicit val taxBandAllocationFormats = Json.format[TaxBandAllocation]
  implicit val allowancesAndReliefsFormats = Json.format[AllowancesAndReliefs]
  implicit val liabilityFormats = Json.format[MongoLiability]

  def create(saUtr: SaUtr, taxYear: TaxYear): MongoLiability = {
    val id = BSONObjectID.generate
    MongoLiability(
      id = id,
      liabilityId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      createdDateTime = DateTime.now(DateTimeZone.UTC)
    )
  }
}

