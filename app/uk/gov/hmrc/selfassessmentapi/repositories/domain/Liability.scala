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

import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxDeducted => _, _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations._

case class TaxesCalculated(pensionSavingsCharges: BigDecimal,
                           totalIncomeTax: BigDecimal,
                           totalTaxDeducted: BigDecimal,
                           totalTaxDue: BigDecimal,
                           totalTaxOverPaid: BigDecimal,
                           taxPaidByPensionScheme: BigDecimal)

object TaxesCalculated {
  implicit val format = Json.format[TaxesCalculated]
}

case class Liability(id: BSONObjectID,
                     liabilityId: LiabilityId,
                     nino: Nino,
                     taxYear: TaxYear,
                     selfEmploymentIncome: Seq[SelfEmploymentIncome],
                     ukPropertyIncome: Seq[UkPropertyIncome],
                     furnishedHolidayLettingsIncome: Seq[FurnishedHolidayLettingIncome],
                     savingsIncome: Seq[InterestFromUKBanksAndBuildingSocieties],
                     ukDividendsIncome: Seq[DividendsFromUKSources],
                     totalIncomeReceived: BigDecimal,
                     totalTaxableIncome: BigDecimal,
                     allowancesAndReliefs: AllowancesAndReliefs,
                     taxDeducted: TaxDeducted,
                     dividendTaxBandSummary: Seq[TaxBandSummary],
                     savingsTaxBandSummary: Seq[TaxBandSummary],
                     nonSavingsTaxBandSummary: Seq[TaxBandSummary],
                     pensionSavingsChargesSummary: Seq[TaxBandSummary],
                     taxes: TaxesCalculated)
    extends LiabilityResult {
  def toLiability =
    api.Liability(income = IncomeSummary(
                       incomes = IncomeFromSources(
                         nonSavings = NonSavingsIncomes(
                           selfEmployment = selfEmploymentIncome,
                           ukProperties = ukPropertyIncome,
                           furnishedHolidayLettings = furnishedHolidayLettingsIncome
                         ),
                         savings = SavingsIncomes(
                           fromUKBanksAndBuildingSocieties = savingsIncome
                         ),
                         dividends = DividendsIncomes(
                           fromUKSources = ukDividendsIncome
                         ),
                         total = totalIncomeReceived
                       ),
                       deductions = Some(
                         api.Deductions(
                           incomeTaxRelief = allowancesAndReliefs.incomeTaxRelief.getOrElse(0),
                           personalAllowance = allowancesAndReliefs.personalAllowance.getOrElse(0),
                           retirementAnnuityContract = allowancesAndReliefs.retirementAnnuityContract.getOrElse(0),
                           total = Sum(allowancesAndReliefs.incomeTaxRelief,
                                       allowancesAndReliefs.personalAllowance,
                                       allowancesAndReliefs.retirementAnnuityContract)
                         )),
                       totalIncomeOnWhichTaxIsDue = totalTaxableIncome
                     ),
                     incomeTaxCalculations = IncomeTaxCalculations(
                       nonSavings = nonSavingsTaxBandSummary,
                       savings = savingsTaxBandSummary,
                       dividends = dividendTaxBandSummary,
                       total = taxes.totalIncomeTax
                     ),
                     otherCharges = api.OtherCharges(
                       pensionSavings = pensionSavingsChargesSummary,
                       total = taxes.pensionSavingsCharges
                     ),
                     taxDeducted = api.TaxDeducted(interestFromUk = taxDeducted.interestFromUk,
                                                      fromUkProperties = taxDeducted.deductionFromUkProperties,
                                                      taxPaidByPensionScheme = taxes.taxPaidByPensionScheme,
                                                      total = taxes.totalTaxDeducted),
                     totalTaxDue = taxes.totalTaxDue,
                     totalTaxOverpaid = taxes.totalTaxOverPaid)
}

object Liability {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats

  implicit val taxDeductedFormats = Json.format[TaxDeducted]
  implicit val liabilityFormats = Json.format[Liability]

  def create(nino: Nino,
             taxYear: TaxYear,
             selfAssessment: api.SelfAssessment,
             createdDateTime: DateTime = DateTime.now()) = {
    val id = BSONObjectID.generate
    new Liability(id = id,
                  liabilityId = id.stringify,
                  nino = nino,
                  taxYear = taxYear,
                  selfEmploymentIncome = calculations.SelfEmployment.Incomes(selfAssessment),
                  furnishedHolidayLettingsIncome = FurnishedHolidayLetting.Incomes(selfAssessment),
                  ukPropertyIncome = UKProperty.Incomes(selfAssessment),
                  savingsIncome = Savings.Incomes(selfAssessment),
                  ukDividendsIncome = Dividends.FromUK(selfAssessment),
                  allowancesAndReliefs = calculations.Deductions(selfAssessment),
                  taxDeducted = calculations.TaxDeducted(selfAssessment),
                  dividendTaxBandSummary = Dividends.IncomeTaxBandSummary(selfAssessment),
                  savingsTaxBandSummary = Savings.IncomeTaxBandSummary(selfAssessment),
                  nonSavingsTaxBandSummary = NonSavings.IncomeTaxBandSummary(selfAssessment),
                  pensionSavingsChargesSummary = PensionSavingsCharges.IncomeTaxBandSummary(selfAssessment),
                  totalTaxableIncome = Totals.TaxableIncome(selfAssessment),
                  totalIncomeReceived = Totals.IncomeReceived(selfAssessment),
                  taxes = TaxesCalculated(pensionSavingsCharges = PensionSavingsCharges.IncomeTax(selfAssessment),
                                totalIncomeTax = Totals.IncomeTax(selfAssessment),
                                totalTaxDeducted = Totals.TaxDeducted(selfAssessment),
                                totalTaxDue = Totals.TaxDue(selfAssessment),
                                totalTaxOverPaid = Totals.TaxOverpaid(selfAssessment),
                                taxPaidByPensionScheme = PensionSavingsCharges.TotalTaxPaid(selfAssessment))
                  )
  }

}

case class UkTaxPaidForEmployment(sourceId: SourceId, ukTaxPaid: BigDecimal)

object UkTaxPaidForEmployment {
  implicit val ukTaxPaidForEmploymentFormats = Json.format[UkTaxPaidForEmployment]
}

case class TaxDeducted(interestFromUk: BigDecimal = 0,
                       totalDeductionFromUkProperties: BigDecimal = 0,
                       deductionFromUkProperties: Seq[TaxPaidForUkProperty] = Nil,
                       ukTaxPaid: BigDecimal = 0,
                       ukTaxesPaidForEmployments: Seq[api.UkTaxPaidForEmployment] = Nil) {

  def totalTaxDeducted = interestFromUk + totalDeductionFromUkProperties + ukTaxPaid
}

object TaxDeducted {
  implicit val taxDeductedFormats = Json.format[TaxDeducted]
}

case class LiabilityError(code: ErrorCode, message: String)

object LiabilityError {
  implicit val calculationErrorFormats = Json.format[LiabilityError]
}

case class LiabilityErrors(id: BSONObjectID,
                           liabilityCalculationErrorId: LiabilityCalculationErrorId,
                           override val nino: Nino,
                           override val taxYear: TaxYear,
                           errors: Seq[LiabilityError])
    extends LiabilityResult

object LiabilityErrors {

  implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val calculationErrorsFormats = Json.format[LiabilityErrors]

  def create(nino: Nino, taxYear: TaxYear, errors: Seq[LiabilityError]): LiabilityErrors = {
    val id = BSONObjectID.generate
    LiabilityErrors(id = id,
                    liabilityCalculationErrorId = id.stringify,
                    nino = nino,
                    taxYear = taxYear,
                    errors = errors)
  }
}

sealed trait LiabilityResult {

  def nino: Nino

  def taxYear: TaxYear

  def fold[X](fError: LiabilityErrors => X, fLiability: Liability => X) = this match {
    case c: LiabilityErrors => fError(c)
    case l: Liability => fLiability(l)
  }
}

object LiabilityResult {

  import Liability.liabilityFormats
  import LiabilityErrors.calculationErrorsFormats

  implicit val liabilityResultFormat = Json.format[LiabilityResult]

  def unapply(liabilityResult: LiabilityResult): Option[(String, JsValue)] = {
    val (prod: Product, sub) = liabilityResult match {
      case o: Liability => (o, Json.toJson(o)(liabilityFormats))
      case o: LiabilityErrors => (o, Json.toJson(o)(calculationErrorsFormats))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(className: String, data: JsValue): LiabilityResult = {
    (className match {
      case "Liability" => Json.fromJson[Liability](data)(liabilityFormats)
      case "LiabilityErrors" =>
        Json.fromJson[LiabilityErrors](data)(calculationErrorsFormats)
    }).get
  }
}
