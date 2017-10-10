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

package uk.gov.hmrc.selfassessmentapi.models.des

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.Amount



case class TaxCalculation(calcOutput: CalcOutput)

object TaxCalculation {
  implicit val writes: Writes[TaxCalculation] = Json.writes[TaxCalculation]
  implicit val reads: Reads[TaxCalculation] = Json.reads[TaxCalculation]
}

case class CalcOutput(calcName: String,
                      calcVersion: String,
                      calcVersionDate: LocalDate,
                      calcID: String,
                      sourceName: String,
                      sourceRef: String,
                      identifier: String,
                      year: Int,
                      periodFrom: LocalDate,
                      periodTo: LocalDate,
                      calcAmount: Amount,
                      calcTimestamp: String,
                      calcResult: CalcResult)

object CalcOutput {
  implicit val writes: Writes[CalcOutput] = Json.writes[CalcOutput]
  implicit val reads: Reads[CalcOutput] = Json.reads[CalcOutput]
}

case class CalcResult(incomeTaxNicYtd: Amount,
                      incomeTaxNicDelta: Amount,
                      crystallised: Boolean,
                      nationalRegime: String,
                      totalTaxableIncome: Amount,
                      taxableIncome: TaxableIncome,
                      totalIncomeTax: Amount,
                      incomeTax: IncomeTax,
                      totalNic: Amount,
                      nic: Nic,
                      eoyEstimate: Option[EoyEstimate],
                      msgCount: Int,
                      msg: Option[Seq[Msg]],
                      previousCalc: Option[PreviousCalc],
                      annualAllowances: AnnualAllowancez)


object CalcResult {
  implicit val writes: Writes[CalcResult] = Json.writes[CalcResult]
  implicit val reads: Reads[CalcResult] = Json.reads[CalcResult]
}


case class TaxableIncome(totalIncomeReceived: Amount,
                         incomeReceived: Option[IncomeReceived],
                         totalAllowancesAndDeductions: Amount,
                         allowancesAndDeductions: AllowancesAndDeductions)

object TaxableIncome {
  implicit val writes: Writes[TaxableIncome] = Json.writes[TaxableIncome]
  implicit val reads: Reads[TaxableIncome] = Json.reads[TaxableIncome]
}


case class IncomeReceived(a: IncomeReceivedA,
                          b: IncomeReceivedB)

case class IncomeReceivedA(employmentIncome: Option[Amount],
                           employments: Option[Employments],
                           shareSchemeIncome: Option[Amount],
                           shareSchemes: Option[IncomeDetails],
                           selfEmploymentIncome: Option[Amount],
                           selfEmployment: Option[Seq[SelfEmployment]],
                           partnershipIncome: Option[Amount],
                           partnership: Option[Seq[Partnership]],
                           ukPropertyIncome: Option[Amount],
                           ukProperty: Option[UkProperty],
                           foreignIncome: Option[Amount],
                           foreign: Option[IncomeDetails],
                           foreignDividendIncome: Option[Amount])

object IncomeReceivedA {
  implicit val writes: Writes[IncomeReceivedA] = Json.writes[IncomeReceivedA]
  implicit val reads: Reads[IncomeReceivedA] = Json.reads[IncomeReceivedA]
}

case class IncomeReceivedB(foreignDividends: Option[IncomeDetails],
                           trustsIncome: Option[Amount],
                           trusts: Option[IncomeDetails],
                           bbsiIncome: Option[Amount],
                           bbsi: Option[Bbsi],
                           ukDividendIncome: Option[Amount],
                           ukDividends: Option[IncomeDetails],
                           ukPensionsIncome: Option[Amount],
                           ukPensions: Option[IncomeDetails],
                           gainsOnLifeInsuranceIncome: Option[Amount],
                           gainsOnLifeInsurance: Option[IncomeDetails],
                           otherIncome: Option[Amount])

object IncomeReceivedB {
  implicit val writes: Writes[IncomeReceivedB] = Json.writes[IncomeReceivedB]
  implicit val reads: Reads[IncomeReceivedB] = Json.reads[IncomeReceivedB]
}


object IncomeReceived {

  implicit val reads: Reads[IncomeReceived] =
    (Reads.of[IncomeReceivedA] and
      Reads.of[IncomeReceivedB])(IncomeReceived.apply _)

  implicit val writes: Writes[IncomeReceived] =
    (JsPath.write[IncomeReceivedA] and
     JsPath.write[IncomeReceivedB])(unlift(IncomeReceived.unapply))
}


case class Employments(totalPay: Amount,
                       totalBenefitsAndExpenses: Amount,
                       totalAllowableExpenses: Amount,
                       employment: Seq[Employment])

object Employments {
  implicit val writes: Writes[Employments] = Json.writes[Employments]
  implicit val reads: Reads[Employments] = Json.reads[Employments]
}


case class Employment(incomeSourceID: String,
                      latestDate: LocalDate,
                      netPay: Amount,
                      benefitsAndExpenses: Amount,
                      allowableExpenses: Amount)

object Employment {
  implicit val writes: Writes[Employment] = Json.writes[Employment]
  implicit val reads: Reads[Employment] = Json.reads[Employment]
}


case class IncomeDetails(incomeSourceID: Option[String],
                         latestDate: Option[LocalDate])

object IncomeDetails {
  implicit val writes: Writes[IncomeDetails] = Json.writes[IncomeDetails]
  implicit val reads: Reads[IncomeDetails] = Json.reads[IncomeDetails]
}


case class SelfEmployment(incomeSourceID: String,
                          latestDate: LocalDate,
                          accountStartDate: Option[LocalDate],
                          accountEndDate: Option[LocalDate],
                          taxableIncome: Amount,
                          finalised: Option[Boolean])

object SelfEmployment {
  implicit val writes: Writes[SelfEmployment] = Json.writes[SelfEmployment]
  implicit val reads: Reads[SelfEmployment] = Json.reads[SelfEmployment]
}


case class Partnership(incomeSourceID: String,
                       latestDate: LocalDate,
                       taxableIncome: Amount)

object Partnership {
  implicit val writes: Writes[Partnership] = Json.writes[Partnership]
  implicit val reads: Reads[Partnership] = Json.reads[Partnership]
}


case class UkProperty(latestDate: LocalDate,
                      taxableProfit: Option[Amount],
                      taxableProfitFhlUk: Option[Amount],
                      taxableProfitFhlEea: Option[Amount],
                      finalised: Option[Boolean])

object UkProperty {
  implicit val writes: Writes[UkProperty] = Json.writes[UkProperty]
  implicit val reads: Reads[UkProperty] = Json.reads[UkProperty]
}


case class Bbsi(incomeSourceID: String,
                latestDate: LocalDate,
                interestReceived: Amount)

object Bbsi {
  implicit val writes: Writes[Bbsi] = Json.writes[Bbsi]
  implicit val reads: Reads[Bbsi] = Json.reads[Bbsi]
}

case class AllowancesAndDeductions(paymentsIntoARetirementAnnuity: Option[Amount],
                                   foreignTaxOnEstates: Option[Amount],
                                   incomeTaxRelief: Option[Amount],
                                   annuities: Option[Amount],
                                   giftOfInvestmentsAndPropertyToCharity: Option[Amount],
                                   apportionedPersonalAllowance: Amount,
                                   marriageAllowanceTransfer: Option[Amount],
                                   blindPersonAllowance: Option[Amount],
                                   blindPersonSurplusAllowanceFromSpouse: Option[Amount],
                                   incomeExcluded: Option[Amount])

object AllowancesAndDeductions {
  implicit val writes: Writes[AllowancesAndDeductions] = Json.writes[AllowancesAndDeductions]
  implicit val reads: Reads[AllowancesAndDeductions] = Json.reads[AllowancesAndDeductions]
}


case class IncomeTax(totalBeforeReliefs: Amount,
                     taxableIncome: Amount,
                     payPensionsProfit: Option[Profit],
                     savingsAndGains: Option[Profit],
                     dividends: Option[Profit],
                     excludedIncome: Option[Amount],
                     totalAllowancesAndReliefs: Amount,
                     allowancesAndReliefs: Option[AllowancesAndReliefs])

object IncomeTax {
  implicit val writes: Writes[IncomeTax] = Json.writes[IncomeTax]
  implicit val reads: Reads[IncomeTax] = Json.reads[IncomeTax]
}


case class Profit(totalAmount: Amount,
                  taxableIncome: Amount,
                  band: Seq[Band])

object Profit {
  implicit val writes: Writes[Profit] = Json.writes[Profit]
  implicit val reads: Reads[Profit] = Json.reads[Profit]
}


case class Band(name: String,
                rate: Amount,
                threshold: Option[Int],
                apportionedThreshold: Option[Int],
                income: Amount,
                taxAmount: Amount)

object Band {
  implicit val writes: Writes[Band] = Json.writes[Band]
  implicit val reads: Reads[Band] = Json.reads[Band]
}


case class AllowancesAndReliefs(deficiencyRelief: Option[Amount],
                                topSlicingRelief: Option[Amount],
                                ventureCapitalTrustRelief: Option[Amount],
                                enterpriseInvestmentSchemeRelief: Option[Amount],
                                seedEnterpriseInvestmentSchemeRelief: Option[Amount],
                                communityInvestmentTaxRelief: Option[Amount],
                                socialInvestmentTaxRelief: Option[Amount],
                                maintenanceAndAlimonyPaid: Option[Amount],
                                marriedCoupleAllowanceRate: Option[Amount],
                                marriedCoupleAllowanceAmount: Option[Amount],
                                marriedCoupleAllowanceRelief: Option[Amount],
                                surplusMarriedCoupleAllowanceAmount: Option[Amount],
                                surplusMarriedCoupleAllowanceRelief: Option[Amount],
                                notionalTaxFromLifePolicies: Option[Amount],
                                notionalTaxFromDividendsAndOtherIncome: Option[Amount],
                                foreignTaxCreditRelief: Option[Amount],
                                propertyFinanceRelief: Option[Amount])

object AllowancesAndReliefs {
  implicit val writes: Writes[AllowancesAndReliefs] = Json.writes[AllowancesAndReliefs]
  implicit val reads: Reads[AllowancesAndReliefs] = Json.reads[AllowancesAndReliefs]
}


case class Nic(class2: Option[Class2], class4: Class4)

object Nic {
  implicit val writes: Writes[Nic] = Json.writes[Nic]
  implicit val reads: Reads[Nic] = Json.reads[Nic]
}

case class Class2(amount: Amount,
                  weekRate: Amount,
                  weeks: Int,
                  limit: Int,
                  apportionedLimit: Int)

object Class2 {
  implicit val writes: Writes[Class2] = Json.writes[Class2]
  implicit val reads: Reads[Class2] = Json.reads[Class2]
}


case class Class4(totalAmount: Amount,
                  band: Seq[Class4Band])

object Class4 {
  implicit val writes: Writes[Class4] = Json.writes[Class4]
  implicit val reads: Reads[Class4] = Json.reads[Class4]
}


case class Class4Band(name: String,
                      rate: Amount,
                      threshold: Option[Int],
                      apportionedThreshold: Option[Int],
                      income: Amount,
                      amount: Amount)

object Class4Band {
  implicit val writes: Writes[Class4Band] = Json.writes[Class4Band]
  implicit val reads: Reads[Class4Band] = Json.reads[Class4Band]
}


case class EoyEstimate(incomeSource: Seq[IncomeSource],
                       totalTaxableIncome: Int,
                       incomeTaxAmount: Int,
                       nic2: Int,
                       nic4: Int,
                       totalNicAmount: Int,
                       incomeTaxNicAmount: Int)

object EoyEstimate {
  implicit val writes: Writes[EoyEstimate] = Json.writes[EoyEstimate]
  implicit val reads: Reads[EoyEstimate] = Json.reads[EoyEstimate]
}


case class IncomeSource(id: Option[String],
                        `type`: Option[String],
                        taxableIncome: Option[Int],
                        supplied: Option[Boolean],
                        finalised: Option[Boolean])

object IncomeSource {
  implicit val writes: Writes[IncomeSource] = Json.writes[IncomeSource]
  implicit val reads: Reads[IncomeSource] = Json.reads[IncomeSource]
}


case class Msg(`type`: String, text: String)

object Msg {
  implicit val writes: Writes[Msg] = Json.writes[Msg]
  implicit val reads: Reads[Msg] = Json.reads[Msg]
}


case class PreviousCalc(calcTimestamp: String,
                        calcID: String,
                        calcAmount: Amount)

object PreviousCalc {
  implicit val writes: Writes[PreviousCalc] = Json.writes[PreviousCalc]
  implicit val reads: Reads[PreviousCalc] = Json.reads[PreviousCalc]
}


case class AnnualAllowancez(personalAllowance: Int,
                            reducedPersonalAllowanceThreshold: Int,
                            reducedPersonalisedAllowance: Int)


object AnnualAllowancez {
  implicit val writes: Writes[AnnualAllowancez] = Json.writes[AnnualAllowancez]
  implicit val reads: Reads[AnnualAllowancez] = Json.reads[AnnualAllowancez]
}

