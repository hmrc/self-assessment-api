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

package uk.gov.hmrc.r1.selfassessmentapi.models.des

import play.api.libs.json.{Json, _}
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class TaxCalculationSpec extends JsonSpec {
  "TaxCalculation" should {

    val validCalcDetail = TaxCalculationDetail(
      DetailsA(
        payFromAllEmployments = Some(200.22),
        benefitsAndExpensesReceived = Some(200.22),
        allowableExpenses = Some(200.22),
        payFromAllEmploymentsAfterExpenses = Some(200.22),
        shareSchemes = Some(200.22),
        profitFromSelfEmployment = Some(200.22),
        profitFromPartnerships = Some(200.22),
        profitFromUkLandAndProperty = Some(200.22),
        foreignIncome = Some(200.22),
        dividendsFromForeignCompanies = Some(200.22),
        trustsAndEstates = Some(200.22),
        interestReceivedFromUkBanksAndBuildingSocieties = Some(200.22),
        dividendsFromUkCompanies = Some(200.22),
        ukPensionsAndStateBenefits = Some(200.22),
        gainsOnLifeInsurance = Some(200.22),
        otherIncome = Some(200.22),
        totalIncomeReceived = Some(200.22),
        paymentsIntoARetirementAnnuity = Some(200.22),
        foreignTaxOnEstates = Some(200.22),
        incomeTaxRelief = Some(200.22),
        incomeTaxReliefReducedToMaximumAllowable = Some(200.22),
        annuities = Some(200.22)
      ),
      DetailsB(
        giftOfInvestmentsAndPropertyToCharity = Some(200.22),
        personalAllowance = Some(200.22),
        marriageAllowanceTransfer = Some(200.22),
        blindPersonAllowance = Some(200.22),
        blindPersonSurplusAllowanceFromSpouse = Some(200.22),
        incomeExcluded = Some(200.22),
        totalIncomeAllowancesUsed = Some(200.22),
        totalIncomeOnWhichTaxIsDue = Some(200.22),
        payPensionsExtender = Some(200.22),
        giftExtender = Some(200.22),
        extendedBR = Some(200.22),
        payPensionsProfitAtBRT = Some(200.22),
        incomeTaxOnPayPensionsProfitAtBRT = Some(200.22),
        payPensionsProfitAtHRT = Some(200.22),
        incomeTaxOnPayPensionsProfitAtHRT = Some(200.22),
        payPensionsProfitAtART = Some(200.22),
        incomeTaxOnPayPensionsProfitAtART = Some(200.22),
        netPropertyFinanceCosts = Some(200.22),
        interestReceivedAtStartingRate = Some(200.22),
        incomeTaxOnInterestReceivedAtStartingRate = Some(200.22),
        interestReceivedAtZeroRate = Some(200.22),
        incomeTaxOnInterestReceivedAtZeroRate = Some(200.22)
      ),
      DetailsC(
        interestReceivedAtBRT = Some(200.22),
        incomeTaxOnInterestReceivedAtBRT = Some(200.22),
        interestReceivedAtHRT = Some(200.22),
        incomeTaxOnInterestReceivedAtHRT = Some(200.22),
        interestReceivedAtART = Some(200.22),
        incomeTaxOnInterestReceivedAtART = Some(200.22),
        dividendsAtZeroRate = Some(200.22),
        incomeTaxOnDividendsAtZeroRate = Some(200.22),
        dividendsAtBRT = Some(200.22),
        incomeTaxOnDividendsAtBRT = Some(200.22),
        dividendsAtHRT = Some(200.22),
        incomeTaxOnDividendsAtHRT = Some(200.22),
        dividendsAtART = Some(200.22),
        incomeTaxOnDividendsAtART = Some(200.22),
        totalIncomeOnWhichTaxHasBeenCharged = Some(200.22),
        taxOnOtherIncome = Some(200.22),
        incomeTaxDue = Some(200.22),
        incomeTaxCharged = Some(200.22),
        deficiencyRelief = Some(200.22),
        topSlicingRelief = Some(200.22),
        ventureCapitalTrustRelief = Some(200.22),
        enterpriseInvestmentSchemeRelief = Some(200.22)
      ),
      DetailsD(
        seedEnterpriseInvestmentSchemeRelief = Some(200.22),
        communityInvestmentTaxRelief = Some(200.22),
        socialInvestmentTaxRelief = Some(200.22),
        maintenanceAndAlimonyPaid = Some(200.22),
        marriedCouplesAllowance = Some(200.22),
        marriedCouplesAllowanceRelief = Some(200.22),
        surplusMarriedCouplesAllowance = Some(200.22),
        surplusMarriedCouplesAllowanceRelief = Some(200.22),
        notionalTaxFromLifePolicies = Some(200.22),
        notionalTaxFromDividendsAndOtherIncome = Some(200.22),
        foreignTaxCreditRelief = Some(200.22),
        incomeTaxDueAfterAllowancesAndReliefs = Some(200.22),
        giftAidPaymentsAmount = Some(200.22),
        giftAidTaxDue = Some(200.22),
        capitalGainsTaxDue = Some(200.22),
        remittanceForNonDomiciles = Some(200.22),
        highIncomeChildBenefitCharge = Some(200.22),
        totalGiftAidTaxReduced = Some(200.22),
        incomeTaxDueAfterGiftAidReduction = Some(200.22),
        annuityAmount = Some(200.22),
        taxDueOnAnnuity = Some(200.22),
        taxCreditsOnDividendsFromUkCompanies = Some(200.22)
      ),
      DetailsE(
        incomeTaxDueAfterDividendTaxCredits = Some(200.22),
        nationalInsuranceContributionAmount = Some(200.22),
        nationalInsuranceContributionCharge = Some(200.22),
        nationalInsuranceContributionSupAmount = Some(200.22),
        nationalInsuranceContributionSupCharge = Some(200.22),
        totalClass4Charge = Some(200.22),
        nationalInsuranceClass1Amount = Some(200.22),
        nationalInsuranceClass2Amount = Some(200.22),
        nicTotal = Some(200.22),
        underpaidTaxForPreviousYears = Some(200.22),
        studentLoanRepayments = Some(200.22),
        pensionChargesGross = Some(200.22),
        pensionChargesTaxPaid = Some(200.22),
        totalPensionSavingCharges = Some(200.22),
        pensionLumpSumAmount = Some(200.22),
        pensionLumpSumRate = Some(200.22),
        statePensionLumpSumAmount = Some(200.22),
        remittanceBasisChargeForNonDomiciles = Some(200.22),
        additionalTaxDueOnPensions = Some(200.22),
        additionalTaxReliefDueOnPensions = Some(200.22),
        incomeTaxDueAfterPensionDeductions = Some(200.22),
        employmentsPensionsAndBenefits = Some(200.22)
      ),
      DetailsF(
        outstandingDebtCollectedThroughPaye = Some(200.22),
        payeTaxBalance = Some(200.22),
        cisAndTradingIncome = Some(200.22),
        partnerships = Some(200.22),
        ukLandAndPropertyTaxPaid = Some(200.22),
        foreignIncomeTaxPaid = Some(200.22),
        trustAndEstatesTaxPaid = Some(200.22),
        overseasIncomeTaxPaid = Some(200.22),
        interestReceivedTaxPaid = Some(200.22),
        voidISAs = Some(200.22),
        otherIncomeTaxPaid = Some(200.22),
        underpaidTaxForPriorYear = Some(200.22),
        totalTaxDeducted = Some(200.22),
        incomeTaxOverpaid = Some(200.22),
        incomeTaxDueAfterDeductions = Some(200.22),
        propertyFinanceTaxDeduction = Some(200.22),
        taxableCapitalGains = Some(200.22),
        capitalGainAtEntrepreneurRate = Some(200.22),
        incomeTaxOnCapitalGainAtEntrepreneurRate = Some(200.22),
        capitalGrainsAtLowerRate = Some(200.22),
        incomeTaxOnCapitalGainAtLowerRate = Some(200.22),
        capitalGainAtHigherRate = Some(200.22)
      ),
      DetailsG(
        incomeTaxOnCapitalGainAtHigherTax = Some(200.22),
        capitalGainsTaxAdjustment = Some(200.22),
        foreignTaxCreditReliefOnCapitalGains = Some(200.22),
        liabilityFromOffShoreTrusts = Some(200.22),
        taxOnGainsAlreadyCharged = Some(200.22),
        totalCapitalGainsTax = Some(200.22),
        incomeAndCapitalGainsTaxDue = Some(200.22),
        taxRefundedInYear = Some(200.22),
        unpaidTaxCalculatedForEarlierYears = Some(200.22),
        marriageAllowanceTransferAmount = Some(200.22),
        marriageAllowanceTransferRelief = Some(200.22),
        marriageAllowanceTransferMaximumAllowable = Some(200.22),
        nationalRegime = Some("abc"),
        allowance = Some(200),
        limitBRT = Some(200),
        limitHRT = Some(200),
        rateBRT = Some(200.22),
        rateHRT = Some(200.22),
        rateART = Some(200.22),
        limitAIA = Some(200),
        allowanceBRT = Some(200),
        interestAllowanceHRT = Some(200)
      ),
      DetailsH(
        interestAllowanceBRT = Some(200),
        dividendAllowance = Some(200),
        dividendBRT = Some(200.22),
        dividendHRT = Some(200.22),
        dividendART = Some(200.22),
        class2NICsLimit = Some(200),
        class2NICsPerWeek = Some(200.22),
        class4NICsLimitBR = Some(200),
        class4NICsLimitHR = Some(200),
        class4NICsBRT = Some(200.22),
        class4NICsHRT = Some(200.22),
        proportionAllowance = Some(200),
        proportionLimitBRT = Some(200),
        proportionLimitHRT = Some(200),
        proportionalTaxDue = Some(200.22),
        proportionInterestAllowanceBRT = Some(200),
        proportionInterestAllowanceHRT = Some(200),
        proportionDividendAllowance = Some(200),
        proportionPayPensionsProfitAtART = Some(200),
        proportionIncomeTaxOnPayPensionsProfitAtART = Some(200),
        proportionPayPensionsProfitAtBRT = Some(200),
        proportionIncomeTaxOnPayPensionsProfitAtBRT = Some(200)
      ),
      DetailsI(
        proportionPayPensionsProfitAtHRT = Some(200),
        proportionIncomeTaxOnPayPensionsProfitAtHRT = Some(200),
        proportionInterestReceivedAtZeroRate = Some(200),
        proportionIncomeTaxOnInterestReceivedAtZeroRate = Some(200),
        proportionInterestReceivedAtBRT = Some(200),
        proportionIncomeTaxOnInterestReceivedAtBRT = Some(200),
        proportionInterestReceivedAtHRT = Some(200),
        proportionIncomeTaxOnInterestReceivedAtHRT = Some(200),
        proportionInterestReceivedAtART = Some(200),
        proportionIncomeTaxOnInterestReceivedAtART = Some(200),
        proportionDividendsAtZeroRate = Some(200),
        proportionIncomeTaxOnDividendsAtZeroRate = Some(200),
        proportionDividendsAtBRT = Some(200),
        proportionIncomeTaxOnDividendsAtBRT = Some(200),
        proportionDividendsAtHRT = Some(200),
        proportionIncomeTaxOnDividendsAtHRT = Some(200),
        proportionDividendsAtART = Some(200),
        proportionIncomeTaxOnDividendsAtART = Some(200),
        proportionClass2NICsLimit = Some(200),
        proportionClass4NICsLimitBR = Some(200),
        proportionClass4NICsLimitHR = Some(200),
        proportionReducedAllowanceLimit = Some(200)
      ),
      DetailsJ(
        eoyEstimate = Some(
          EndOfYearEstimate(
            incomeSource = Seq(IncomeSource(
              id = Some("incomeSourceId"),
              `type` = "01",
              taxableIncome = 1000.25,
              supplied = false,
              finalised = Some(true)
            )),
            totalTaxableIncome = Some(1000.25),
            incomeTaxAmount = Some(1000.25),
            nic2 = Some(1000.25),
            nic4 = Some(1000.25),
            totalNicAmount = Some(1000.25),
            incomeTaxNicAmount = Some(1000.25)
          )
        )
      )
    )

    "round trip" in {
      val calc = TaxCalculation(
        incomeTaxYTD = 500.55,
        incomeTaxThisPeriod = 239.21,
        calcDetail = Some(validCalcDetail),
        previousCalc = Some(PreviousTaxCalculation("abc", "abc", 123.45))
      )

      roundTripJson(calc)
    }

    val jsonTransformer = __.json.update(__.read[JsObject].map { o =>
      o ++ Json.obj("allowance" -> "foo", "limitBRT" -> "100.50")
    })

    val invalidTaxCalcDetailJson = Json.toJson[TaxCalculationDetail](validCalcDetail).transform(jsonTransformer).get

    "reject non-integer fields for xxx" in {
      assertValidationErrorsWithMessage(invalidTaxCalcDetailJson, Map("" -> Seq("Value is not an integer")))
    }
  }
}
