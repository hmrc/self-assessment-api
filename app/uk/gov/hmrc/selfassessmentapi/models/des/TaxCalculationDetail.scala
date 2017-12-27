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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class TaxCalculation(incomeTaxYTD: BigDecimal,
                          incomeTaxThisPeriod: BigDecimal,
                          calcDetail: Option[TaxCalculationDetail],
                          previousCalc: Option[PreviousTaxCalculation])

object TaxCalculation {
  implicit val format: Format[TaxCalculation] = Json.format[TaxCalculation]
}

case class PreviousTaxCalculation(calcTimestamp: String, calcID: String, calcAmount: BigDecimal)

object PreviousTaxCalculation {
  implicit val format: Format[PreviousTaxCalculation] = Json.format[PreviousTaxCalculation]
}

case class DetailsA(payFromAllEmployments: Option[BigDecimal],
                    benefitsAndExpensesReceived: Option[BigDecimal],
                    allowableExpenses: Option[BigDecimal],
                    payFromAllEmploymentsAfterExpenses: Option[BigDecimal],
                    shareSchemes: Option[BigDecimal],
                    profitFromSelfEmployment: Option[BigDecimal],
                    profitFromPartnerships: Option[BigDecimal],
                    profitFromUkLandAndProperty: Option[BigDecimal],
                    foreignIncome: Option[BigDecimal],
                    dividendsFromForeignCompanies: Option[BigDecimal],
                    trustsAndEstates: Option[BigDecimal],
                    interestReceivedFromUkBanksAndBuildingSocieties: Option[BigDecimal],
                    dividendsFromUkCompanies: Option[BigDecimal],
                    ukPensionsAndStateBenefits: Option[BigDecimal],
                    gainsOnLifeInsurance: Option[BigDecimal],
                    otherIncome: Option[BigDecimal],
                    totalIncomeReceived: Option[BigDecimal],
                    paymentsIntoARetirementAnnuity: Option[BigDecimal],
                    foreignTaxOnEstates: Option[BigDecimal],
                    incomeTaxRelief: Option[BigDecimal],
                    incomeTaxReliefReducedToMaximumAllowable: Option[BigDecimal],
                    annuities: Option[BigDecimal])

object DetailsA {
  implicit val reads: Reads[DetailsA] = Json.reads[DetailsA]
  implicit val writes: OWrites[DetailsA] = Json.writes[DetailsA]
}

case class DetailsB(giftOfInvestmentsAndPropertyToCharity: Option[BigDecimal],
                    personalAllowance: Option[BigDecimal],
                    marriageAllowanceTransfer: Option[BigDecimal],
                    blindPersonAllowance: Option[BigDecimal],
                    blindPersonSurplusAllowanceFromSpouse: Option[BigDecimal],
                    incomeExcluded: Option[BigDecimal],
                    totalIncomeAllowancesUsed: Option[BigDecimal],
                    totalIncomeOnWhichTaxIsDue: Option[BigDecimal],
                    payPensionsExtender: Option[BigDecimal],
                    giftExtender: Option[BigDecimal],
                    extendedBR: Option[BigDecimal],
                    payPensionsProfitAtBRT: Option[BigDecimal],
                    incomeTaxOnPayPensionsProfitAtBRT: Option[BigDecimal],
                    payPensionsProfitAtHRT: Option[BigDecimal],
                    incomeTaxOnPayPensionsProfitAtHRT: Option[BigDecimal],
                    payPensionsProfitAtART: Option[BigDecimal],
                    incomeTaxOnPayPensionsProfitAtART: Option[BigDecimal],
                    netPropertyFinanceCosts: Option[BigDecimal],
                    interestReceivedAtStartingRate: Option[BigDecimal],
                    incomeTaxOnInterestReceivedAtStartingRate: Option[BigDecimal],
                    interestReceivedAtZeroRate: Option[BigDecimal],
                    incomeTaxOnInterestReceivedAtZeroRate: Option[BigDecimal])

object DetailsB {
  implicit val reads: Reads[DetailsB] = Json.reads[DetailsB]
  implicit val writes: OWrites[DetailsB] = Json.writes[DetailsB]
}

case class DetailsC(interestReceivedAtBRT: Option[BigDecimal],
                    incomeTaxOnInterestReceivedAtBRT: Option[BigDecimal],
                    interestReceivedAtHRT: Option[BigDecimal],
                    incomeTaxOnInterestReceivedAtHRT: Option[BigDecimal],
                    interestReceivedAtART: Option[BigDecimal],
                    incomeTaxOnInterestReceivedAtART: Option[BigDecimal],
                    dividendsAtZeroRate: Option[BigDecimal],
                    incomeTaxOnDividendsAtZeroRate: Option[BigDecimal],
                    dividendsAtBRT: Option[BigDecimal],
                    incomeTaxOnDividendsAtBRT: Option[BigDecimal],
                    dividendsAtHRT: Option[BigDecimal],
                    incomeTaxOnDividendsAtHRT: Option[BigDecimal],
                    dividendsAtART: Option[BigDecimal],
                    incomeTaxOnDividendsAtART: Option[BigDecimal],
                    totalIncomeOnWhichTaxHasBeenCharged: Option[BigDecimal],
                    taxOnOtherIncome: Option[BigDecimal],
                    incomeTaxDue: Option[BigDecimal],
                    incomeTaxCharged: Option[BigDecimal],
                    deficiencyRelief: Option[BigDecimal],
                    topSlicingRelief: Option[BigDecimal],
                    ventureCapitalTrustRelief: Option[BigDecimal],
                    enterpriseInvestmentSchemeRelief: Option[BigDecimal])

object DetailsC {
  implicit val reads: Reads[DetailsC] = Json.reads[DetailsC]
  implicit val writes: OWrites[DetailsC] = Json.writes[DetailsC]
}

case class DetailsD(seedEnterpriseInvestmentSchemeRelief: Option[BigDecimal],
                    communityInvestmentTaxRelief: Option[BigDecimal],
                    socialInvestmentTaxRelief: Option[BigDecimal],
                    maintenanceAndAlimonyPaid: Option[BigDecimal],
                    marriedCouplesAllowance: Option[BigDecimal],
                    marriedCouplesAllowanceRelief: Option[BigDecimal],
                    surplusMarriedCouplesAllowance: Option[BigDecimal],
                    surplusMarriedCouplesAllowanceRelief: Option[BigDecimal],
                    notionalTaxFromLifePolicies: Option[BigDecimal],
                    notionalTaxFromDividendsAndOtherIncome: Option[BigDecimal],
                    foreignTaxCreditRelief: Option[BigDecimal],
                    incomeTaxDueAfterAllowancesAndReliefs: Option[BigDecimal],
                    giftAidPaymentsAmount: Option[BigDecimal],
                    giftAidTaxDue: Option[BigDecimal],
                    capitalGainsTaxDue: Option[BigDecimal],
                    remittanceForNonDomiciles: Option[BigDecimal],
                    highIncomeChildBenefitCharge: Option[BigDecimal],
                    totalGiftAidTaxReduced: Option[BigDecimal],
                    incomeTaxDueAfterGiftAidReduction: Option[BigDecimal],
                    annuityAmount: Option[BigDecimal],
                    taxDueOnAnnuity: Option[BigDecimal],
                    taxCreditsOnDividendsFromUkCompanies: Option[BigDecimal])

object DetailsD {
  implicit val reads: Reads[DetailsD] = Json.reads[DetailsD]
  implicit val writes: OWrites[DetailsD] = Json.writes[DetailsD]
}

case class DetailsE(incomeTaxDueAfterDividendTaxCredits: Option[BigDecimal],
                    nationalInsuranceContributionAmount: Option[BigDecimal],
                    nationalInsuranceContributionCharge: Option[BigDecimal],
                    nationalInsuranceContributionSupAmount: Option[BigDecimal],
                    nationalInsuranceContributionSupCharge: Option[BigDecimal],
                    totalClass4Charge: Option[BigDecimal],
                    nationalInsuranceClass1Amount: Option[BigDecimal],
                    nationalInsuranceClass2Amount: Option[BigDecimal],
                    nicTotal: Option[BigDecimal],
                    underpaidTaxForPreviousYears: Option[BigDecimal],
                    studentLoanRepayments: Option[BigDecimal],
                    pensionChargesGross: Option[BigDecimal],
                    pensionChargesTaxPaid: Option[BigDecimal],
                    totalPensionSavingCharges: Option[BigDecimal],
                    pensionLumpSumAmount: Option[BigDecimal],
                    pensionLumpSumRate: Option[BigDecimal],
                    statePensionLumpSumAmount: Option[BigDecimal],
                    remittanceBasisChargeForNonDomiciles: Option[BigDecimal],
                    additionalTaxDueOnPensions: Option[BigDecimal],
                    additionalTaxReliefDueOnPensions: Option[BigDecimal],
                    incomeTaxDueAfterPensionDeductions: Option[BigDecimal],
                    employmentsPensionsAndBenefits: Option[BigDecimal])

object DetailsE {
  implicit val reads: Reads[DetailsE] = Json.reads[DetailsE]
  implicit val writes: OWrites[DetailsE] = Json.writes[DetailsE]
}

case class DetailsF(outstandingDebtCollectedThroughPaye: Option[BigDecimal],
                    payeTaxBalance: Option[BigDecimal],
                    cisAndTradingIncome: Option[BigDecimal],
                    partnerships: Option[BigDecimal],
                    ukLandAndPropertyTaxPaid: Option[BigDecimal],
                    foreignIncomeTaxPaid: Option[BigDecimal],
                    trustAndEstatesTaxPaid: Option[BigDecimal],
                    overseasIncomeTaxPaid: Option[BigDecimal],
                    interestReceivedTaxPaid: Option[BigDecimal],
                    voidISAs: Option[BigDecimal],
                    otherIncomeTaxPaid: Option[BigDecimal],
                    underpaidTaxForPriorYear: Option[BigDecimal],
                    totalTaxDeducted: Option[BigDecimal],
                    incomeTaxOverpaid: Option[BigDecimal],
                    incomeTaxDueAfterDeductions: Option[BigDecimal],
                    propertyFinanceTaxDeduction: Option[BigDecimal],
                    taxableCapitalGains: Option[BigDecimal],
                    capitalGainAtEntrepreneurRate: Option[BigDecimal],
                    incomeTaxOnCapitalGainAtEntrepreneurRate: Option[BigDecimal],
                    capitalGrainsAtLowerRate: Option[BigDecimal],
                    incomeTaxOnCapitalGainAtLowerRate: Option[BigDecimal],
                    capitalGainAtHigherRate: Option[BigDecimal])

object DetailsF {
  implicit val reads: Reads[DetailsF] = Json.reads[DetailsF]
  implicit val writes: OWrites[DetailsF] = Json.writes[DetailsF]
}

case class DetailsG(incomeTaxOnCapitalGainAtHigherTax: Option[BigDecimal],
                    capitalGainsTaxAdjustment: Option[BigDecimal],
                    foreignTaxCreditReliefOnCapitalGains: Option[BigDecimal],
                    liabilityFromOffShoreTrusts: Option[BigDecimal],
                    taxOnGainsAlreadyCharged: Option[BigDecimal],
                    totalCapitalGainsTax: Option[BigDecimal],
                    incomeAndCapitalGainsTaxDue: Option[BigDecimal],
                    taxRefundedInYear: Option[BigDecimal],
                    unpaidTaxCalculatedForEarlierYears: Option[BigDecimal],
                    marriageAllowanceTransferAmount: Option[BigDecimal],
                    marriageAllowanceTransferRelief: Option[BigDecimal],
                    marriageAllowanceTransferMaximumAllowable: Option[BigDecimal],
                    nationalRegime: Option[String], // TODO: ALL BELOW -- See validation
                    allowance: Option[BigInt],
                    limitBRT: Option[BigInt],
                    limitHRT: Option[BigInt],
                    rateBRT: Option[BigDecimal],
                    rateHRT: Option[BigDecimal],
                    rateART: Option[BigDecimal],
                    limitAIA: Option[BigInt],
                    allowanceBRT: Option[BigInt],
                    interestAllowanceHRT: Option[BigInt])

object DetailsG {
  implicit val reads: Reads[DetailsG] = Json.reads[DetailsG]
  implicit val writes: OWrites[DetailsG] = Json.writes[DetailsG]
}

case class DetailsH(interestAllowanceBRT: Option[BigInt],
                    dividendAllowance: Option[BigInt],
                    dividendBRT: Option[BigDecimal],
                    dividendHRT: Option[BigDecimal],
                    dividendART: Option[BigDecimal],
                    class2NICsLimit: Option[BigInt],
                    class2NICsPerWeek: Option[BigDecimal],
                    class4NICsLimitBR: Option[BigInt],
                    class4NICsLimitHR: Option[BigInt],
                    class4NICsBRT: Option[BigDecimal],
                    class4NICsHRT: Option[BigDecimal],
                    proportionAllowance: Option[BigInt],
                    proportionLimitBRT: Option[BigInt],
                    proportionLimitHRT: Option[BigInt],
                    proportionalTaxDue: Option[BigDecimal],
                    proportionInterestAllowanceBRT: Option[BigInt],
                    proportionInterestAllowanceHRT: Option[BigInt],
                    proportionDividendAllowance: Option[BigInt],
                    proportionPayPensionsProfitAtART: Option[BigInt],
                    proportionIncomeTaxOnPayPensionsProfitAtART: Option[BigInt],
                    proportionPayPensionsProfitAtBRT: Option[BigInt],
                    proportionIncomeTaxOnPayPensionsProfitAtBRT: Option[BigInt])

object DetailsH {
  implicit val reads: Reads[DetailsH] = Json.reads[DetailsH]
  implicit val writes: OWrites[DetailsH] = Json.writes[DetailsH]
}

case class DetailsI(proportionPayPensionsProfitAtHRT: Option[BigInt],
                    proportionIncomeTaxOnPayPensionsProfitAtHRT: Option[BigInt],
                    proportionInterestReceivedAtZeroRate: Option[BigInt],
                    proportionIncomeTaxOnInterestReceivedAtZeroRate: Option[BigInt],
                    proportionInterestReceivedAtBRT: Option[BigInt],
                    proportionIncomeTaxOnInterestReceivedAtBRT: Option[BigInt],
                    proportionInterestReceivedAtHRT: Option[BigInt],
                    proportionIncomeTaxOnInterestReceivedAtHRT: Option[BigInt],
                    proportionInterestReceivedAtART: Option[BigInt],
                    proportionIncomeTaxOnInterestReceivedAtART: Option[BigInt],
                    proportionDividendsAtZeroRate: Option[BigInt],
                    proportionIncomeTaxOnDividendsAtZeroRate: Option[BigInt],
                    proportionDividendsAtBRT: Option[BigInt],
                    proportionIncomeTaxOnDividendsAtBRT: Option[BigInt],
                    proportionDividendsAtHRT: Option[BigInt],
                    proportionIncomeTaxOnDividendsAtHRT: Option[BigInt],
                    proportionDividendsAtART: Option[BigInt],
                    proportionIncomeTaxOnDividendsAtART: Option[BigInt],
                    proportionClass2NICsLimit: Option[BigInt],
                    proportionClass4NICsLimitBR: Option[BigInt],
                    proportionClass4NICsLimitHR: Option[BigInt],
                    proportionReducedAllowanceLimit: Option[BigInt])

object DetailsI {
  implicit val reads: Reads[DetailsI] = Json.reads[DetailsI]
  implicit val writes: OWrites[DetailsI] = Json.writes[DetailsI]
}

case class IncomeSource(
  id: Option[String],
  `type`: String,
  taxableIncome: BigDecimal,
  supplied: Boolean,
  finalised: Option[Boolean]
)

object IncomeSource {

  implicit val reads: Reads[IncomeSource] = Json.reads[IncomeSource]
  implicit val writes: Writes[IncomeSource] = Json.writes[IncomeSource]

}

case class EndOfYearEstimate(
  incomeSource: Seq[IncomeSource],
  totalTaxableIncome: Option[BigDecimal],
  incomeTaxAmount: Option[BigDecimal],
  nic2: Option[BigDecimal],
  nic4: Option[BigDecimal],
  totalNicAmount: Option[BigDecimal],
  incomeTaxNicAmount: Option[BigDecimal]
)

object EndOfYearEstimate {

  implicit val reads: Reads[EndOfYearEstimate] = Json.reads[EndOfYearEstimate]
  implicit val writes: Writes[EndOfYearEstimate] = Json.writes[EndOfYearEstimate]

}

case class DetailsJ(eoyEstimate: Option[EndOfYearEstimate])

object DetailsJ {
  implicit val reads: Reads[DetailsJ] = Json.reads[DetailsJ]
  implicit val writes: OWrites[DetailsJ] = Json.writes[DetailsJ]
}

case class TaxCalculationDetail(a: DetailsA,
                                b: DetailsB,
                                c: DetailsC,
                                d: DetailsD,
                                e: DetailsE,
                                f: DetailsF,
                                g: DetailsG,
                                h: DetailsH,
                                i: DetailsI,
                                j: DetailsJ)

object TaxCalculationDetail {
  implicit val reads: Reads[TaxCalculationDetail] =
    (Reads.of[DetailsA] and
      Reads.of[DetailsB] and
      Reads.of[DetailsC] and
      Reads.of[DetailsD] and
      Reads.of[DetailsE] and
      Reads.of[DetailsF] and
      Reads.of[DetailsG] and
      Reads.of[DetailsH] and
      Reads.of[DetailsI] and
      Reads.of[DetailsJ])(TaxCalculationDetail.apply _)

  implicit val writes: Writes[TaxCalculationDetail] =
    (JsPath.write[DetailsA] and JsPath.write[DetailsB] and JsPath.write[DetailsC] and JsPath.write[DetailsD] and
      JsPath.write[DetailsE] and JsPath.write[DetailsF] and JsPath.write[DetailsG] and JsPath
      .write[DetailsH] and JsPath.write[DetailsI] and JsPath.write[DetailsJ])(unlift(TaxCalculationDetail.unapply))
}
