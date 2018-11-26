/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.r1.selfassessmentapi.models.calculation

import uk.gov.hmrc.r2.selfassessmentapi.UnitSpec
import uk.gov.hmrc.r2.selfassessmentapi.models.des
import des._
import org.scalatest.OptionValues

class ApiTaxCalculationSpec extends UnitSpec with OptionValues {

  "ApiTaxCalculation" should {

    val desTaxCalculation =
      des.TaxCalculation(
        incomeTaxYTD = 1000.25,
        incomeTaxThisPeriod = 1100.25,
        calcDetail = Some(
          TaxCalculationDetail(
            a = DetailsA(
              payFromAllEmployments = None,
              benefitsAndExpensesReceived = None,
              allowableExpenses = None,
              payFromAllEmploymentsAfterExpenses = None,
              shareSchemes = None,
              profitFromSelfEmployment = None,
              profitFromPartnerships = None,
              profitFromUkLandAndProperty = None,
              foreignIncome = None,
              dividendsFromForeignCompanies = None,
              trustsAndEstates = None,
              interestReceivedFromUkBanksAndBuildingSocieties = None,
              dividendsFromUkCompanies = None,
              ukPensionsAndStateBenefits = None,
              gainsOnLifeInsurance = None,
              otherIncome = None,
              totalIncomeReceived = None,
              paymentsIntoARetirementAnnuity = None,
              foreignTaxOnEstates = None,
              incomeTaxRelief = None,
              incomeTaxReliefReducedToMaximumAllowable = None,
              annuities = None
            ),
            b = DetailsB(
              giftOfInvestmentsAndPropertyToCharity = None,
              personalAllowance = None,
              marriageAllowanceTransfer = None,
              blindPersonAllowance = None,
              blindPersonSurplusAllowanceFromSpouse = None,
              incomeExcluded = None,
              totalIncomeAllowancesUsed = None,
              totalIncomeOnWhichTaxIsDue = None,
              payPensionsExtender = None,
              giftExtender = None,
              extendedBR = None,
              payPensionsProfitAtBRT = None,
              incomeTaxOnPayPensionsProfitAtBRT = None,
              payPensionsProfitAtHRT = None,
              incomeTaxOnPayPensionsProfitAtHRT = None,
              payPensionsProfitAtART = None,
              incomeTaxOnPayPensionsProfitAtART = None,
              netPropertyFinanceCosts = None,
              interestReceivedAtStartingRate = None,
              incomeTaxOnInterestReceivedAtStartingRate = None,
              interestReceivedAtZeroRate = None,
              incomeTaxOnInterestReceivedAtZeroRate = None
            ),
            c = DetailsC(
              interestReceivedAtBRT = None,
              incomeTaxOnInterestReceivedAtBRT = None,
              interestReceivedAtHRT = None,
              incomeTaxOnInterestReceivedAtHRT = None,
              interestReceivedAtART = None,
              incomeTaxOnInterestReceivedAtART = None,
              dividendsAtZeroRate = None,
              incomeTaxOnDividendsAtZeroRate = None,
              dividendsAtBRT = None,
              incomeTaxOnDividendsAtBRT = None,
              dividendsAtHRT = None,
              incomeTaxOnDividendsAtHRT = None,
              dividendsAtART = None,
              incomeTaxOnDividendsAtART = None,
              totalIncomeOnWhichTaxHasBeenCharged = None,
              taxOnOtherIncome = None,
              incomeTaxDue = None,
              incomeTaxCharged = None,
              deficiencyRelief = None,
              topSlicingRelief = None,
              ventureCapitalTrustRelief = None,
              enterpriseInvestmentSchemeRelief = None
            ),
            d = DetailsD(
              seedEnterpriseInvestmentSchemeRelief = None,
              communityInvestmentTaxRelief = None,
              socialInvestmentTaxRelief = None,
              maintenanceAndAlimonyPaid = None,
              marriedCouplesAllowance = None,
              marriedCouplesAllowanceRelief = None,
              surplusMarriedCouplesAllowance = None,
              surplusMarriedCouplesAllowanceRelief = None,
              notionalTaxFromLifePolicies = None,
              notionalTaxFromDividendsAndOtherIncome = None,
              foreignTaxCreditRelief = None,
              incomeTaxDueAfterAllowancesAndReliefs = None,
              giftAidPaymentsAmount = None,
              giftAidTaxDue = None,
              capitalGainsTaxDue = None,
              remittanceForNonDomiciles = None,
              highIncomeChildBenefitCharge = None,
              totalGiftAidTaxReduced = None,
              incomeTaxDueAfterGiftAidReduction = None,
              annuityAmount = None,
              taxDueOnAnnuity = None,
              taxCreditsOnDividendsFromUkCompanies = None
            ),
            e = DetailsE(
              incomeTaxDueAfterDividendTaxCredits = None,
              nationalInsuranceContributionAmount = None,
              nationalInsuranceContributionCharge = None,
              nationalInsuranceContributionSupAmount = None,
              nationalInsuranceContributionSupCharge = None,
              totalClass4Charge = None,
              nationalInsuranceClass1Amount = None,
              nationalInsuranceClass2Amount = None,
              nicTotal = None,
              underpaidTaxForPreviousYears = None,
              studentLoanRepayments = None,
              pensionChargesGross = None,
              pensionChargesTaxPaid = None,
              totalPensionSavingCharges = None,
              pensionLumpSumAmount = None,
              pensionLumpSumRate = None,
              statePensionLumpSumAmount = None,
              remittanceBasisChargeForNonDomiciles = None,
              additionalTaxDueOnPensions = None,
              additionalTaxReliefDueOnPensions = None,
              incomeTaxDueAfterPensionDeductions = None,
              employmentsPensionsAndBenefits = None
            ),
            f = DetailsF(
              outstandingDebtCollectedThroughPaye = None,
              payeTaxBalance = None,
              cisAndTradingIncome = None,
              partnerships = None,
              ukLandAndPropertyTaxPaid = None,
              foreignIncomeTaxPaid = None,
              trustAndEstatesTaxPaid = None,
              overseasIncomeTaxPaid = None,
              interestReceivedTaxPaid = None,
              voidISAs = None,
              otherIncomeTaxPaid = None,
              underpaidTaxForPriorYear = None,
              totalTaxDeducted = None,
              incomeTaxOverpaid = None,
              incomeTaxDueAfterDeductions = None,
              propertyFinanceTaxDeduction = None,
              taxableCapitalGains = None,
              capitalGainAtEntrepreneurRate = None,
              incomeTaxOnCapitalGainAtEntrepreneurRate = None,
              capitalGrainsAtLowerRate = None,
              incomeTaxOnCapitalGainAtLowerRate = None,
              capitalGainAtHigherRate = None
            ),
            g = DetailsG(
              incomeTaxOnCapitalGainAtHigherTax = None,
              capitalGainsTaxAdjustment = None,
              foreignTaxCreditReliefOnCapitalGains = None,
              liabilityFromOffShoreTrusts = None,
              taxOnGainsAlreadyCharged = None,
              totalCapitalGainsTax = None,
              incomeAndCapitalGainsTaxDue = None,
              taxRefundedInYear = None,
              unpaidTaxCalculatedForEarlierYears = None,
              marriageAllowanceTransferAmount = None,
              marriageAllowanceTransferRelief = None,
              marriageAllowanceTransferMaximumAllowable = None,
              nationalRegime = None,
              allowance = None,
              limitBRT = None,
              limitHRT = None,
              rateBRT = None,
              rateHRT = None,
              rateART = None,
              limitAIA = None,
              allowanceBRT = None,
              interestAllowanceHRT = None
            ),
            h = DetailsH(
              interestAllowanceBRT = None,
              dividendAllowance = None,
              dividendBRT = None,
              dividendHRT = None,
              dividendART = None,
              class2NICsLimit = None,
              class2NICsPerWeek = None,
              class4NICsLimitBR = None,
              class4NICsLimitHR = None,
              class4NICsBRT = None,
              class4NICsHRT = None,
              proportionAllowance = None,
              proportionLimitBRT = None,
              proportionLimitHRT = None,
              proportionalTaxDue = None,
              proportionInterestAllowanceBRT = None,
              proportionInterestAllowanceHRT = None,
              proportionDividendAllowance = None,
              proportionPayPensionsProfitAtART = None,
              proportionIncomeTaxOnPayPensionsProfitAtART = None,
              proportionPayPensionsProfitAtBRT = None,
              proportionIncomeTaxOnPayPensionsProfitAtBRT = None
            ),
            i = DetailsI(
              proportionPayPensionsProfitAtHRT = None,
              proportionIncomeTaxOnPayPensionsProfitAtHRT = None,
              proportionInterestReceivedAtZeroRate = None,
              proportionIncomeTaxOnInterestReceivedAtZeroRate = None,
              proportionInterestReceivedAtBRT = None,
              proportionIncomeTaxOnInterestReceivedAtBRT = None,
              proportionInterestReceivedAtHRT = None,
              proportionIncomeTaxOnInterestReceivedAtHRT = None,
              proportionInterestReceivedAtART = None,
              proportionIncomeTaxOnInterestReceivedAtART = None,
              proportionDividendsAtZeroRate = None,
              proportionIncomeTaxOnDividendsAtZeroRate = None,
              proportionDividendsAtBRT = None,
              proportionIncomeTaxOnDividendsAtBRT = None,
              proportionDividendsAtHRT = None,
              proportionIncomeTaxOnDividendsAtHRT = None,
              proportionDividendsAtART = None,
              proportionIncomeTaxOnDividendsAtART = None,
              proportionClass2NICsLimit = None,
              proportionClass4NICsLimitBR = None,
              proportionClass4NICsLimitHR = None,
              proportionReducedAllowanceLimit = None
            ),
            j = DetailsJ(
              eoyEstimate = None
            )
          )
        ),
        previousCalc = Some(
          PreviousTaxCalculation(
            calcTimestamp = "4498-07-06T21:42:24.294Z",
            calcID = "abc",
            calcAmount = 200.00
          )
        )
      )

    "convert income tax this period from DES TaxCalculation" in {
      ApiTaxCalculation.from(desTaxCalculation).other.incomeTaxThisPeriod shouldBe desTaxCalculation.incomeTaxThisPeriod
    }

    "do not return UK property income sources if DES does not return any UK property income sources " in {
      val eoyEstimate = EndOfYearEstimate(
        incomeSource = Seq(
          IncomeSource(
            id = Some("self-assessment-income-source"),
            `type` = "03",
            taxableIncome = 100.00,
            supplied = false,
            finalised = None
          )
        ),
        totalTaxableIncome = None,
        incomeTaxAmount = None,
        nic2 = None,
        nic4 = None,
        totalNicAmount = None,
        incomeTaxNicAmount = None
      )

      val calcWithEoyEstimate = desTaxCalculation.copy(
        calcDetail = Some(desTaxCalculation.calcDetail.value.copy(
          j = desTaxCalculation.calcDetail.value.j.copy(eoyEstimate = Some(eoyEstimate)))
        )
      )

      ApiTaxCalculation.from(calcWithEoyEstimate).j.value.eoyEstimate.value.ukProperty shouldBe None
    }

    "do not return self-employment income sources if DES does not return any self-employment income sources " in {
      val eoyEstimate = EndOfYearEstimate(
        incomeSource = Seq(
          IncomeSource(
            id = Some("uk-property-income-source"),
            `type` = "05",
            taxableIncome = 100.00,
            supplied = false,
            finalised = None
          )
        ),
        totalTaxableIncome = None,
        incomeTaxAmount = None,
        nic2 = None,
        nic4 = None,
        totalNicAmount = None,
        incomeTaxNicAmount = None
      )

      val calcWithEoyEstimate = desTaxCalculation.copy(
        calcDetail = Some(desTaxCalculation.calcDetail.value.copy(
          j = desTaxCalculation.calcDetail.value.j.copy(eoyEstimate = Some(eoyEstimate)))
        )
      )

      ApiTaxCalculation.from(calcWithEoyEstimate).j.value.eoyEstimate.value.selfEmployment shouldBe None
    }

  }

}
