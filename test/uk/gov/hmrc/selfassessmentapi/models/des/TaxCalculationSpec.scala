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
import play.api.libs.json.{Json, _}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec


class TaxCalculationSpec extends JsonSpec {
  "TaxCalculation" should {


    val validCalcDetail = TaxCalculation(
      calcOutput = CalcOutput(
        calcName = "abcdefghijklmnopqr",
        calcVersion = "abcdef",
        calcVersionDate = new LocalDate("2016-01-01"),
        calcID = "12345678",
        sourceName = "abcdefghijklmno",
        sourceRef = "abcdefghijklmnopqrs",
        identifier = "abcdefghijklm",
        year = 2016,
        periodFrom = new LocalDate("2016-01-01"),
        periodTo = new LocalDate("2016-01-01"),
        calcAmount = BigDecimal("1000.25"),
        calcTimestamp = "4498-07-06T21:42:24.294Z",
        calcResult = CalcResult(
          incomeTaxNicYtd = BigDecimal("1000.25"),
          incomeTaxNicDelta = BigDecimal("1000.25"),
          crystallised = true,
          nationalRegime = "UK",
          totalTaxableIncome = BigDecimal("1000.25"),
          taxableIncome = TaxableIncome(
            totalIncomeReceived = BigDecimal("1000.25"),
            incomeReceived = Some(IncomeReceived(
              a = IncomeReceivedA(
                employmentIncome = Some(BigDecimal("1000.25")),
                employments = Some(Employments(
                  totalPay = BigDecimal("1000.25"),
                  totalBenefitsAndExpenses = BigDecimal("1000.25"),
                  totalAllowableExpenses = BigDecimal("1000.25"),
                  employment = Seq(Employment(
                    incomeSourceID = "abcdefghijklm",
                    latestDate = new LocalDate("2016-01-01"),
                    netPay = BigDecimal("1000.25"),
                    benefitsAndExpenses = BigDecimal("1000.25"),
                    allowableExpenses = BigDecimal("1000.25")
                  )))),
                shareSchemeIncome = Some(BigDecimal("1000.25")),
                shareSchemes = Some(IncomeDetails(
                  incomeSourceID = Some("abcdefghijklm"),
                  latestDate = Some(new LocalDate("2016-01-01"))
                )),
                selfEmploymentIncome = Some(BigDecimal("1000.25")),
                selfEmployment = Some(
                  Seq(SelfEmployment(
                    incomeSourceID = "abcdefghijklm",
                    latestDate = new LocalDate("2016-01-01"),
                    accountStartDate = Some(new LocalDate("2016-01-01")),
                    accountEndDate = Some(new LocalDate("2016-01-01")),
                    taxableIncome = BigDecimal("1000.25"),
                    finalised = Some(true)
                  )
                  )),
                partnershipIncome = Some(BigDecimal("1000.25")),
                partnership = Some(
                  Seq(
                    Partnership(
                      incomeSourceID = "abcdefghijklm",
                      latestDate = new LocalDate("2016-01-01"),
                      taxableIncome = BigDecimal("1000.25")
                    )
                  )
                ),
                ukPropertyIncome = Some(BigDecimal("1000.25")),
                ukProperty = Some(
                  UkProperty(
                    latestDate = new LocalDate("2016-01-01"),
                    taxableProfit = Some(BigDecimal("1000.25")),
                    taxableProfitFhlUk = Some(BigDecimal("1000.25")),
                    taxableProfitFhlEea = Some(BigDecimal("1000.25")),
                    finalised = Some(true)
                  )
                ),
                foreignIncome = Some(BigDecimal("1000.25")),
                foreign = Some(IncomeDetails(
                  incomeSourceID = Some("abcdefghijklm"),
                  latestDate = Some(new LocalDate("2016-01-01"))
                )),
                foreignDividendIncome = Some(BigDecimal("1000.25"))
              ),
              b = IncomeReceivedB(
                foreignDividends = Some(
                  IncomeDetails(
                    incomeSourceID = Some("abcdefghijklm"),
                    latestDate = Some(new LocalDate("2016-01-01"))
                  )
                ),
                trustsIncome = Some(BigDecimal("1000.25")),
                trusts = Some(
                  IncomeDetails(
                    incomeSourceID = Some("abcdefghijklm"),
                    latestDate = Some(new LocalDate("2016-01-01"))
                  )
                ),
                bbsiIncome = Some(BigDecimal("1000.25")),
                bbsi = Some(
                  Bbsi(
                    incomeSourceID = "abcdefghijklm",
                    latestDate = new LocalDate("2016-01-01"),
                    interestReceived = BigDecimal("1000.25")
                  )
                ),
                ukDividendIncome = Some(BigDecimal("1000.25")),
                ukDividends = Some(
                  IncomeDetails(
                    incomeSourceID = Some("abcdefghijklm"),
                    latestDate = Some(new LocalDate("2016-01-01"))
                  )
                ),
                ukPensionsIncome = Some(BigDecimal("1000.25")),
                ukPensions = Some(
                  IncomeDetails(
                    incomeSourceID = Some("abcdefghijklm"),
                    latestDate = Some(new LocalDate("2016-01-01"))
                  )
                ),
                gainsOnLifeInsuranceIncome = Some(BigDecimal("1000.25")),
                gainsOnLifeInsurance = Some(
                  IncomeDetails(
                    incomeSourceID = Some("abcdefghijklm"),
                    latestDate = Some(new LocalDate("2016-01-01"))
                  )
                ),
                otherIncome = Some(BigDecimal("1000.25"))
              )
            )),
            totalAllowancesAndDeductions = BigDecimal("1000.25"),
            allowancesAndDeductions = AllowancesAndDeductions(
              paymentsIntoARetirementAnnuity = Some(BigDecimal("1000.25")),
              foreignTaxOnEstates = Some(BigDecimal("1000.25")),
              incomeTaxRelief = Some(BigDecimal("1000.25")),
              annuities = Some(BigDecimal("1000.25")),
              giftOfInvestmentsAndPropertyToCharity = Some(BigDecimal("1000.25")),
              apportionedPersonalAllowance = BigDecimal("1000.25"),
              marriageAllowanceTransfer = Some(BigDecimal("1000.25")),
              blindPersonAllowance = Some(BigDecimal("1000.25")),
              blindPersonSurplusAllowanceFromSpouse = Some(BigDecimal("1000.25")),
              incomeExcluded = Some(BigDecimal("1000.25"))
            )
          ),
          totalIncomeTax = BigDecimal("1000.25"),
          incomeTax = IncomeTax(
            totalBeforeReliefs = BigDecimal("1000.25"),
            taxableIncome = BigDecimal("1000.25"),
            payPensionsProfit = Some(
              Profit(
                totalAmount = BigDecimal("1000.25"),
                taxableIncome = BigDecimal("1000.25"),
                band = Seq(
                  Band(
                    name = "abcdefghijklm",
                    rate = BigDecimal("99.99"),
                    threshold = Some(99999999),
                    apportionedThreshold = Some(99999999),
                    income = BigDecimal("1000.25"),
                    taxAmount = BigDecimal("1000.25")
                  )
                )
              )
            ),
            savingsAndGains = Some(
              Profit(
                totalAmount = BigDecimal("1000.25"),
                taxableIncome = BigDecimal("1000.25"),
                band = Seq(
                  Band(
                    name = "abcdefghijklm",
                    rate = BigDecimal("99.99"),
                    threshold = Some(99999999),
                    apportionedThreshold = Some(99999999),
                    income = BigDecimal("1000.25"),
                    taxAmount = BigDecimal("1000.25")
                  )
                )
              )
            ),
            dividends = Some(
              Profit(
                totalAmount = BigDecimal("1000.25"),
                taxableIncome = BigDecimal("1000.25"),
                band = Seq(
                  Band(
                    name = "abcdefghijklm",
                    rate = BigDecimal("99.99"),
                    threshold = Some(99999999),
                    apportionedThreshold = Some(99999999),
                    income = BigDecimal("1000.25"),
                    taxAmount = BigDecimal("1000.25")
                  )
                )
              )
            ),
            excludedIncome = Some(BigDecimal("1000.25")),
            totalAllowancesAndReliefs = BigDecimal("1000.25"),
            allowancesAndReliefs = Some(
              AllowancesAndReliefs(
                deficiencyRelief = Some(BigDecimal("1000.25")),
                topSlicingRelief = Some(BigDecimal("1000.25")),
                ventureCapitalTrustRelief = Some(BigDecimal("1000.25")),
                enterpriseInvestmentSchemeRelief = Some(BigDecimal("1000.25")),
                seedEnterpriseInvestmentSchemeRelief = Some(BigDecimal("1000.25")),
                communityInvestmentTaxRelief = Some(BigDecimal("1000.25")),
                socialInvestmentTaxRelief = Some(BigDecimal("1000.25")),
                maintenanceAndAlimonyPaid = Some(BigDecimal("1000.25")),
                marriedCoupleAllowanceRate = Some(BigDecimal("1000.25")),
                marriedCoupleAllowanceAmount = Some(BigDecimal("1000.25")),
                marriedCoupleAllowanceRelief = Some(BigDecimal("1000.25")),
                surplusMarriedCoupleAllowanceAmount = Some(BigDecimal("1000.25")),
                surplusMarriedCoupleAllowanceRelief = Some(BigDecimal("1000.25")),
                notionalTaxFromLifePolicies = Some(BigDecimal("1000.25")),
                notionalTaxFromDividendsAndOtherIncome = Some(BigDecimal("1000.25")),
                foreignTaxCreditRelief = Some(BigDecimal("1000.25")),
                propertyFinanceRelief = Some(BigDecimal("1000.25"))
              )
            )
          ),
          totalNic = BigDecimal("1000.25"),
          nic = Some(
            Nic(
              class2 = Some(
                Class2(
                  amount = BigDecimal("1000.25"),
                  weekRate = BigDecimal("1000.25"),
                  weeks = 1,
                  limit = 99999999,
                  apportionedLimit = 2
                )
              ),
              class4 =
                Some(
                  Class4(
                    totalAmount = BigDecimal("1000.25"),
                    band = Seq(
                      Class4Band(
                        name = "abcdefghijklm",
                        rate = BigDecimal("99.99"),
                        threshold = Some(99999999),
                        apportionedThreshold = Some(99999999),
                        income = BigDecimal("1000.25"),
                        amount = BigDecimal("1000.25")
                      )
                    )
                  )
                )
            )
          ),
          eoyEstimate = Some(
            EoyEstimate(
              incomeSource = Seq(
                IncomeSource(
                  id = Some("abcdefghijklm"),
                  `type` = Some("01"),
                  taxableIncome = Some(99999999),
                  supplied = Some(true),
                  finalised = Some(true)
                )
              ),
              totalTaxableIncome = 99999999,
              incomeTaxAmount = 99999999,
              nic2 = 99999999,
              nic4 = 99999999,
              totalNicAmount = 99999999,
              incomeTaxNicAmount = 2
            )
          ),
          msgCount = 1,
          msg = Some(
            Seq(
              Msg(
                `type` = "abcdefghijklm",
                text = "abcdefghijklm"
              )
            )
          ),
          previousCalc = Some(
            PreviousCalc(
              calcTimestamp = "4498-07-06T21:42:24.294Z",
              calcID = "00000000",
              calcAmount = BigDecimal("1000.25")
            )
          ),
          annualAllowances = AnnualAllowancez(
            personalAllowance = 99999999,
            reducedPersonalAllowanceThreshold = 99999999,
            reducedPersonalisedAllowance = 99999999
          )
        )
      )
    )

    "round trip" in {
      roundTripJson(validCalcDetail)
    }

    val jsonTransformer = __.json.update(__.read[JsObject].map { o =>
      o ++ Json.obj("allowance" -> "foo", "limitBRT" -> "100.50")
    })

    val invalidTaxCalcDetailJson = Json.toJson[TaxCalculation](validCalcDetail).transform(jsonTransformer).get

    "reject non-integer fields for xxx" in {
      assertValidationErrorsWithMessage(invalidTaxCalcDetailJson, Map("" -> Seq("Value is not an integer")))
    }
  }
}
