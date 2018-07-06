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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.calculation.ApiTaxCalculation.{PropertyIncomeSource, SelfEmploymentIncomeSource}
import uk.gov.hmrc.http.HttpResponse

class TaxCalculationResponseSpec extends UnitSpec  {

  "TaxCalculationResponse" should {

    val json =
      Json.parse("""
      |{
      |    "calcName": "abcdefghijklmnopqr",
      |    "calcVersion": "abcdef",
      |    "calcVersionDate": "2016-01-01",
      |    "calcID": "12345678",
      |    "sourceName": "abcdefghijklmno",
      |    "sourceRef": "abcdefghijklmnopqrs",
      |    "identifier": "abcdefghijklm",
      |    "year": 2016,
      |    "periodFrom": "2016-01-01",
      |    "periodTo": "2016-01-01",
      |    "calcAmount": 1000.25,
      |    "calcTimestamp": "4498-07-06T21:42:24.294Z",
      |    "calcResult": {
      |        "incomeTaxYTD": 1000.25,
      |        "incomeTaxThisPeriod": 1000.25,
      |        "calcDetail": {
      |            "payFromAllEmployments": 200.22,
      |            "benefitsAndExpensesReceived": 200.22,
      |            "allowableExpenses": 200.22,
      |            "payFromAllEmploymentsAfterExpenses": 200.22,
      |            "shareSchemes": 200.22,
      |            "profitFromSelfEmployment": 200.22,
      |            "profitFromPartnerships": 200.22,
      |            "profitFromUkLandAndProperty": 200.22,
      |            "dividendsFromForeignCompanies": 200.22,
      |            "foreignIncome": 200.22,
      |            "trustsAndEstates": 200.22,
      |            "interestReceivedFromUkBanksAndBuildingSocieties": 200.22,
      |            "dividendsFromUkCompanies": 200.22,
      |            "ukPensionsAndStateBenefits": 200.22,
      |            "gainsOnLifeInsurance": 200.22,
      |            "otherIncome": 200.22,
      |            "totalIncomeReceived": 200.22,
      |            "paymentsIntoARetirementAnnuity": 200.22,
      |            "foreignTaxOnEstates": 200.22,
      |            "incomeTaxRelief": 200.22,
      |            "incomeTaxReliefReducedToMaximumAllowable": 200.22,
      |            "annuities": 200.22,
      |            "giftOfInvestmentsAndPropertyToCharity": 200.22,
      |            "personalAllowance": 200.22,
      |            "marriageAllowanceTransfer": 200.22,
      |            "blindPersonAllowance": 200.22,
      |            "blindPersonSurplusAllowanceFromSpouse": 200.22,
      |            "incomeExcluded": 200.22,
      |            "totalIncomeAllowancesUsed": 200.22,
      |            "totalIncomeOnWhichTaxIsDue": 200.22,
      |            "payPensionsExtender": 200.22,
      |            "giftExtender": 200.22,
      |            "extendedBR": 200.22,
      |            "payPensionsProfitAtBRT": 200.22,
      |            "incomeTaxOnPayPensionsProfitAtBRT": 200.22,
      |            "payPensionsProfitAtHRT": 200.22,
      |            "incomeTaxOnPayPensionsProfitAtHRT": 200.22,
      |            "payPensionsProfitAtART": 200.22,
      |            "incomeTaxOnPayPensionsProfitAtART": 200.22,
      |            "netPropertyFinanceCosts": 200.22,
      |            "interestReceivedAtStartingRate": 200.22,
      |            "incomeTaxOnInterestReceivedAtStartingRate": 200.22,
      |            "interestReceivedAtZeroRate": 200.22,
      |            "incomeTaxOnInterestReceivedAtZeroRate": 200.22,
      |            "interestReceivedAtBRT": 200.22,
      |            "incomeTaxOnInterestReceivedAtBRT": 200.22,
      |            "interestReceivedAtHRT": 200.22,
      |            "incomeTaxOnInterestReceivedAtHRT": 200.22,
      |            "interestReceivedAtART": 200.22,
      |            "incomeTaxOnInterestReceivedAtART": 200.22,
      |            "dividendsAtZeroRate": 200.22,
      |            "incomeTaxOnDividendsAtZeroRate": 200.22,
      |            "dividendsAtBRT": 200.22,
      |            "incomeTaxOnDividendsAtBRT": 200.22,
      |            "dividendsAtHRT": 200.22,
      |            "incomeTaxOnDividendsAtHRT": 200.22,
      |            "dividendsAtART": 200.22,
      |            "incomeTaxOnDividendsAtART": 200.22,
      |            "totalIncomeOnWhichTaxHasBeenCharged": 200.22,
      |            "taxOnOtherIncome": 200.22,
      |            "incomeTaxDue": 200.22,
      |            "incomeTaxCharged": 200.22,
      |            "deficiencyRelief": 200.22,
      |            "topSlicingRelief": 200.22,
      |            "ventureCapitalTrustRelief": 200.22,
      |            "enterpriseInvestmentSchemeRelief": 200.22,
      |            "seedEnterpriseInvestmentSchemeRelief": 200.22,
      |            "communityInvestmentTaxRelief": 200.22,
      |            "socialInvestmentTaxRelief": 200.22,
      |            "maintenanceAndAlimonyPaid": 200.22,
      |            "marriedCouplesAllowance": 200.22,
      |            "marriedCouplesAllowanceRelief": 200.22,
      |            "surplusMarriedCouplesAllowance": 200.22,
      |            "surplusMarriedCouplesAllowanceRelief": 200.22,
      |            "notionalTaxFromLifePolicies": 200.22,
      |            "notionalTaxFromDividendsAndOtherIncome": 200.22,
      |            "foreignTaxCreditRelief": 200.22,
      |            "incomeTaxDueAfterAllowancesAndReliefs": 200.22,
      |            "giftAidPaymentsAmount": 200.22,
      |            "giftAidTaxDue": 200.22,
      |            "capitalGainsTaxDue": 200.22,
      |            "remittanceForNonDomiciles": 200.22,
      |            "highIncomeChildBenefitCharge": 200.22,
      |            "totalGiftAidTaxReduced": 200.22,
      |            "incomeTaxDueAfterGiftAidReduction": 200.22,
      |            "annuityAmount": 200.22,
      |            "taxDueOnAnnuity": 200.22,
      |            "taxCreditsOnDividendsFromUkCompanies": 200.22,
      |            "incomeTaxDueAfterDividendTaxCredits": 200.22,
      |            "nationalInsuranceContributionAmount": 200.22,
      |            "nationalInsuranceContributionCharge": 200.22,
      |            "nationalInsuranceContributionSupAmount": 200.22,
      |            "nationalInsuranceContributionSupCharge": 200.22,
      |            "totalClass4Charge": 200.22,
      |            "nationalInsuranceClass1Amount": 200.22,
      |            "nationalInsuranceClass2Amount": 200.22,
      |            "nicTotal": 200.22,
      |            "underpaidTaxForPreviousYears": 200.22,
      |            "studentLoanRepayments": 200.22,
      |            "pensionChargesGross": 200.22,
      |            "pensionChargesTaxPaid": 200.22,
      |            "totalPensionSavingCharges": 200.22,
      |            "pensionLumpSumAmount": 200.22,
      |            "pensionLumpSumRate": 200.22,
      |            "statePensionLumpSumAmount": 200.22,
      |            "remittanceBasisChargeForNonDomiciles": 200.22,
      |            "additionalTaxDueOnPensions": 200.22,
      |            "additionalTaxReliefDueOnPensions": 200.22,
      |            "incomeTaxDueAfterPensionDeductions": 200.22,
      |            "employmentsPensionsAndBenefits": 200.22,
      |            "outstandingDebtCollectedThroughPaye": 200.22,
      |            "payeTaxBalance": 200.22,
      |            "cisAndTradingIncome": 200.22,
      |            "partnerships": 200.22,
      |            "ukLandAndPropertyTaxPaid": 200.22,
      |            "foreignIncomeTaxPaid": 200.22,
      |            "trustAndEstatesTaxPaid": 200.22,
      |            "overseasIncomeTaxPaid": 200.22,
      |            "interestReceivedTaxPaid": 200.22,
      |            "voidISAs": 200.22,
      |            "otherIncomeTaxPaid": 200.22,
      |            "underpaidTaxForPriorYear": 200.22,
      |            "totalTaxDeducted": 200.22,
      |            "incomeTaxOverpaid": 200.22,
      |            "incomeTaxDueAfterDeductions": 200.22,
      |            "propertyFinanceTaxDeduction": 200.22,
      |            "taxableCapitalGains": 200.22,
      |            "capitalGainAtEntrepreneurRate": 200.22,
      |            "incomeTaxOnCapitalGainAtEntrepreneurRate": 200.22,
      |            "capitalGrainsAtLowerRate": 200.22,
      |            "incomeTaxOnCapitalGainAtLowerRate": 200.22,
      |            "capitalGainAtHigherRate": 200.22,
      |            "incomeTaxOnCapitalGainAtHigherTax": 200.22,
      |            "capitalGainsTaxAdjustment": 200.22,
      |            "foreignTaxCreditReliefOnCapitalGains": 200.22,
      |            "liabilityFromOffShoreTrusts": 200.22,
      |            "taxOnGainsAlreadyCharged": 200.22,
      |            "totalCapitalGainsTax": 200.22,
      |            "incomeAndCapitalGainsTaxDue": 200.22,
      |            "taxRefundedInYear": 200.22,
      |            "unpaidTaxCalculatedForEarlierYears": 200.22,
      |            "marriageAllowanceTransferAmount": 200.22,
      |            "marriageAllowanceTransferRelief": 200.22,
      |            "marriageAllowanceTransferMaximumAllowable": 200.22,
      |            "nationalRegime": "abc",
      |            "allowance": 200,
      |            "limitBRT": 200,
      |            "limitHRT": 200,
      |            "rateBRT": 200.22,
      |            "rateHRT": 200.22,
      |            "rateART": 200.22,
      |            "limitAIA": 200,
      |            "allowanceBRT": 200,
      |            "interestAllowanceHRT": 200,
      |            "interestAllowanceBRT": 200,
      |            "dividendAllowance": 200,
      |            "dividendBRT": 200.22,
      |            "dividendHRT": 200.22,
      |            "dividendART": 200.22,
      |            "class2NICsLimit": 200,
      |            "class2NICsPerWeek": 200.22,
      |            "class4NICsLimitBR": 200,
      |            "class4NICsLimitHR": 200,
      |            "class4NICsBRT": 200.22,
      |            "class4NICsHRT": 200.22,
      |            "proportionAllowance": 200,
      |            "proportionLimitBRT": 200,
      |            "proportionLimitHRT": 200,
      |            "proportionalTaxDue": 200.22,
      |            "proportionInterestAllowanceBRT": 200,
      |            "proportionInterestAllowanceHRT": 200,
      |            "proportionDividendAllowance": 200,
      |            "proportionPayPensionsProfitAtART": 200,
      |            "proportionIncomeTaxOnPayPensionsProfitAtART": 200,
      |            "proportionPayPensionsProfitAtBRT": 200,
      |            "proportionIncomeTaxOnPayPensionsProfitAtBRT": 200,
      |            "proportionPayPensionsProfitAtHRT": 200,
      |            "proportionIncomeTaxOnPayPensionsProfitAtHRT": 200,
      |            "proportionInterestReceivedAtZeroRate": 200,
      |            "proportionIncomeTaxOnInterestReceivedAtZeroRate": 200,
      |            "proportionInterestReceivedAtBRT": 200,
      |            "proportionIncomeTaxOnInterestReceivedAtBRT": 200,
      |            "proportionInterestReceivedAtHRT": 200,
      |            "proportionIncomeTaxOnInterestReceivedAtHRT": 200,
      |            "proportionInterestReceivedAtART": 200,
      |            "proportionIncomeTaxOnInterestReceivedAtART": 200,
      |            "proportionDividendsAtZeroRate": 200,
      |            "proportionIncomeTaxOnDividendsAtZeroRate": 200,
      |            "proportionDividendsAtBRT": 200,
      |            "proportionIncomeTaxOnDividendsAtBRT": 200,
      |            "proportionDividendsAtHRT": 200,
      |            "proportionIncomeTaxOnDividendsAtHRT": 200,
      |            "proportionDividendsAtART": 200,
      |            "proportionIncomeTaxOnDividendsAtART": 200,
      |            "proportionClass2NICsLimit": 200,
      |            "proportionClass4NICsLimitBR": 200,
      |            "proportionClass4NICsLimitHR": 200,
      |            "proportionReducedAllowanceLimit": 200,
      |            "eoyEstimate": {
      |              "incomeSource": [
      |              {
      |                "id": "selfEmploymentId1",
      |                "type": "01",
      |                "taxableIncome": 89999999.99,
      |                "supplied": true,
      |                "finalised": true
      |              },
      |              {
      |                "id": "selfEmploymentId2",
      |                "type": "01",
      |                "taxableIncome": 89999999.99,
      |                "supplied": true,
      |                "finalised": true
      |              },
      |              {
      |                "id": "ukPropertyId",
      |                "type": "02",
      |                "taxableIncome": 89999999.99,
      |                "supplied": true,
      |                "finalised": true
      |              },
      |              {
      |                "id": "otherIncomeId",
      |                "type": "02",
      |                "taxableIncome": 89999999.99,
      |                "supplied": true,
      |                "finalised": true
      |              }
      |            ],
      |            "totalTaxableIncome": 89999999.99,
      |            "incomeTaxAmount": 89999999.99,
      |            "nic2": 89999999.99,
      |            "nic4": 89999999.99,
      |            "totalNicAmount": 9999999.99,
      |            "incomeTaxNicAmount": 999999.99
      |          }
      |        },
      |        "previousCalc": {
      |            "calcTimestamp": "4498-07-06T21:42:24.294Z",
      |            "calcID": "12345678",
      |            "calcAmount": 1000.25
      |        }
      |    }
      |}
    """.stripMargin)

    val response = TaxCalculationResponse(HttpResponse(200, responseJson = Some(json)))

    "end of year estimates parse UK property income" in {
      val estimate = response.calculation.value.j.value.eoyEstimate
      estimate.value.ukProperty.value should (contain (PropertyIncomeSource(89999999.99, true, Some(true))))
    }

    "end of year estimates parse self-employment income" in {
      val estimate = response.calculation.value.j.value.eoyEstimate
      estimate.value.selfEmployment.value should (contain (SelfEmploymentIncomeSource(Some("selfEmploymentId1"), 89999999.99, true, Some(true))))
    }

  }

}
