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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.models.CessationReason
import uk.gov.hmrc.selfassessmentapi.models.CessationReason._

object Jsons {

  object Errors {

    private def error(error: (String, String)) = {
      s"""
         |    {
         |      "code": "${error._1}",
         |      "path": "${error._2}"
         |    }
         """.stripMargin
    }

    private def errorWithMessage(code: String, message: String) =
      s"""
         |{
         |  "code": "$code",
         |  "message": "$message"
         |}
       """.stripMargin

    val invalidNino: String =
      errorWithMessage("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
    val ninoInvalid: String = errorWithMessage("NINO_INVALID", "The provided Nino is invalid")
    val invalidPayload: String =
      errorWithMessage("INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
    val invalidRequest: String = errorWithMessage("INVALID_REQUEST", "Invalid request")
    val ninoNotFound: String =
      errorWithMessage("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found.")
    val desNotFound: String =
      errorWithMessage("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")
    val duplicateTradingName: String = errorWithMessage("CONFLICT", "Duplicated trading name.")
    val notFound: String = errorWithMessage("NOT_FOUND", "Resource was not found")
    val invalidPeriod: String = businessErrorWithMessage(
      "INVALID_PERIOD" -> "Periods should be contiguous and have no gaps between one another")
    val overlappingPeriod: String = businessErrorWithMessage(
      "OVERLAPPING_PERIOD" -> "Period overlaps with existing periods.")
    val nonContiguousPeriod: String = businessErrorWithMessage(
      "NOT_CONTIGUOUS_PERIOD" -> "Periods should be contiguous.")
    val misalignedPeriod: String = businessErrorWithMessage(
      "MISALIGNED_PERIOD" -> "Period is not within the accounting period.")
    val misalignedAndOverlappingPeriod: String = businessErrorWithMessage(
      "MISALIGNED_PERIOD" -> "Period is not within the accounting period.",
      "OVERLAPPING_PERIOD" -> "Period overlaps with existing periods.")
    val invalidOriginatorId: String =
      errorWithMessage("INVALID_ORIGINATOR_ID", "Submission has not passed validation. Invalid header Originator-Id.")
    val internalServerError: String = errorWithMessage("INTERNAL_SERVER_ERROR", "An internal server error occurred")
    val invalidCalcId: String = errorWithMessage("INVALID_CALCID", "Submission has not passed validation")
    val unauthorised: String = errorWithMessage("UNAUTHORIZED", "Bearer token is missing or not authorized")
    val clientNotSubscribed: String = errorWithMessage("CLIENT_NOT_SUBSCRIBED", "The client is not subscribed to MTD")
    val agentNotAuthorised: String = errorWithMessage("AGENT_NOT_AUTHORIZED", "The agent is not authorized")
    val agentNotSubscribed: String =
      errorWithMessage("AGENT_NOT_SUBSCRIBED", "The agent is not subscribed to agent services")

    def invalidRequest(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "INVALID_REQUEST",
         |  "message": "Invalid request",
         |  "errors": [
         |    ${errors.map { error }.mkString(",")}
         |  ]
         |}
         """.stripMargin
    }

    def businessError(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "BUSINESS_ERROR",
         |  "message": "Business validation error",
         |  "errors": [
         |    ${errors.map { error }.mkString(",")}
         |  ]
         |}
         """.stripMargin
    }

    def businessErrorWithMessage(errors: (String, String)*): String = {
      s"""
         |{
         |  "code": "BUSINESS_ERROR",
         |  "message": "Business validation error",
         |  "errors": [
         |    ${errors
           .map {
             case (code, msg) => errorWithMessage(code, msg)
           }
           .mkString(",")}
         |  ]
         |}
         """.stripMargin
    }
  }

  object Banks {
    def apply(accountName: String = "Savings Account"): JsValue = {
      Json.parse(s"""
           |{
           |  "accountName": "$accountName"
           |}
         """.stripMargin)
    }

    def annualSummary(taxedUkInterest: Option[BigDecimal], untaxedUkInterest: Option[BigDecimal]): JsValue = {

      val taxed = taxedUkInterest.map { taxed =>
        val separator = if (untaxedUkInterest.isDefined) "," else ""

        s"""
           |  "taxedUkInterest": $taxed$separator
         """.stripMargin
      }

      val untaxed = untaxedUkInterest.map { untaxed =>
        s"""
           |  "untaxedUkInterest": $untaxed
         """.stripMargin
      }

      Json.parse(s"""
           |{
           |  ${taxed.getOrElse("")}
           |  ${untaxed.getOrElse("")}
           |}
       """.stripMargin)
    }
  }

  object Properties {
    def apply(): JsValue = Json.obj()

    def fhlPeriod(fromDate: Option[String] = None,
                  toDate: Option[String] = None,
                  rentIncome: BigDecimal = 0,
                  premisesRunningCosts: BigDecimal = 0,
                  repairsAndMaintenance: BigDecimal = 0,
                  financialCosts: BigDecimal = 0,
                  professionalFees: BigDecimal = 0,
                  otherCost: BigDecimal = 0,
                  consolidatedExpenses: Option[BigDecimal] = None): JsValue = {

      val from =
        fromDate
          .map { date =>
            s"""
               | "from": "$date",
         """.stripMargin
          }
          .getOrElse("")

      val to =
        toDate
          .map { date =>
            s"""
               | "to": "$date",
         """.stripMargin
          }
          .getOrElse("")


      val ce =
        consolidatedExpenses
          .map { ce =>
            s"""
               | , "consolidatedExpenses": "$ce"
         """.stripMargin
          }
          .getOrElse("")

      Json.parse(s"""
           |{
           |  $from
           |  $to
           |  "incomes": {
           |    "rentIncome": { "amount": $rentIncome }
           |  },
           |  "expenses": {
           |    "premisesRunningCosts": { "amount": $premisesRunningCosts },
           |    "repairsAndMaintenance": { "amount": $repairsAndMaintenance },
           |    "financialCosts": { "amount": $financialCosts },
           |    "professionalFees": { "amount": $professionalFees },
           |    "other": { "amount": $otherCost }
           |  }
           |  $ce
           |}
       """.stripMargin)
    }

    def otherPeriod(fromDate: Option[String] = None,
                    toDate: Option[String] = None,
                    rentIncome: BigDecimal = 0,
                    rentIncomeTaxDeducted: BigDecimal = 0,
                    premiumsOfLeaseGrant: Option[BigDecimal] = None,
                    reversePremiums: BigDecimal = 0,
                    premisesRunningCosts: BigDecimal = 0,
                    repairsAndMaintenance: BigDecimal = 0,
                    financialCosts: BigDecimal = 0,
                    professionalFees: BigDecimal = 0,
                    costOfServices: BigDecimal = 0,
                    otherCost: BigDecimal = 0,
                    consolidatedExpenses: Option[BigDecimal] = None): JsValue = {

      val from =
        fromDate
          .map { date =>
            s"""
               | "from": "$date",
         """.stripMargin
          }
          .getOrElse("")

      val to =
        toDate
          .map { date =>
            s"""
               | "to": "$date",
         """.stripMargin
          }
          .getOrElse("")

      val ce =
        consolidatedExpenses
          .map { ce =>
            s"""
               | , "consolidatedExpenses": "$ce"
         """.stripMargin
          }
          .getOrElse("")


      Json.parse(s"""
           |{
           |  $from
           |  $to
           |  "incomes": {
           |    "rentIncome": { "amount": $rentIncome, "taxDeducted": $rentIncomeTaxDeducted },
           |    ${premiumsOfLeaseGrant
                      .map(income => s""" "premiumsOfLeaseGrant": { "amount": $income },""")
                      .getOrElse("")}
           |    "reversePremiums": { "amount": $reversePremiums }
           |  },
           |  "expenses": {
           |    "premisesRunningCosts": { "amount": $premisesRunningCosts },
           |    "repairsAndMaintenance": { "amount": $repairsAndMaintenance },
           |    "financialCosts": { "amount": $financialCosts },
           |    "professionalFees": { "amount": $professionalFees },
           |    "costOfServices": { "amount": $costOfServices },
           |    "other": { "amount": $otherCost }
           |  }
           |  $ce
           |}
       """.stripMargin)
    }

    def periodSummary(dates: (String, String)*): JsValue = {
      val nestedDates = dates
        .map { date =>
          s"""
             |{
             |  "id": "${date._1}_${date._2}",
             |  "from": "${date._1}",
             |  "to": "${date._2}"
             |}
           """.stripMargin
        }
        .mkString(",")

      Json.parse(
        s"""
           |[
           |  $nestedDates
           |]
         """.stripMargin
      )
    }

    def fhlAnnualSummary(annualInvestmentAllowance: BigDecimal = 0.0,
                         otherCapitalAllowance: BigDecimal = 0.0,
                         lossBroughtForward: BigDecimal = 0.0,
                         privateUseAdjustment: BigDecimal = 0.0,
                         balancingCharge: BigDecimal = 0.0): JsValue = {
      Json.parse(s"""
           |{
           |  "allowances": {
           |    "annualInvestmentAllowance": $annualInvestmentAllowance,
           |    "otherCapitalAllowance": $otherCapitalAllowance
           |  },
           |  "adjustments": {
           |   "lossBroughtForward": $lossBroughtForward,
           |   "privateUseAdjustment": $privateUseAdjustment,
           |   "balancingCharge": $balancingCharge
           |  }
           |}
    """.stripMargin)
    }

    def otherAnnualSummary(annualInvestmentAllowance: BigDecimal = 0.0,
                           businessPremisesRenovationAllowance: BigDecimal = 0.0,
                           otherCapitalAllowance: BigDecimal = 0.0,
                           zeroEmissionsGoodsVehicleAllowance: BigDecimal = 0.0,
                           costOfReplacingDomesticItems: BigDecimal = 0.0,
                           lossBroughtForward: BigDecimal = 0.0,
                           privateUseAdjustment: BigDecimal = 0.0,
                           balancingCharge: BigDecimal = 0.0): JsValue = {
      Json.parse(s"""
           |{
           |  "allowances": {
           |    "annualInvestmentAllowance": $annualInvestmentAllowance,
           |    "businessPremisesRenovationAllowance": $businessPremisesRenovationAllowance,
           |    "otherCapitalAllowance": $otherCapitalAllowance,
           |    "costOfReplacingDomesticItems": $costOfReplacingDomesticItems,
           |    "zeroEmissionsGoodsVehicleAllowance": $zeroEmissionsGoodsVehicleAllowance
           |  },
           |  "adjustments": {
           |   "lossBroughtForward": $lossBroughtForward,
           |   "privateUseAdjustment": $privateUseAdjustment,
           |   "balancingCharge": $balancingCharge
           |  }
           |}
    """.stripMargin)
    }

  }

  object SelfEmployment {
    def apply(accPeriodStart: String = "2017-04-06",
              accPeriodEnd: String = "2018-04-05",
              accountingType: String = "CASH",
              commencementDate: Option[String] = Some("2017-01-01"),
              cessationDate: Option[String] = Some("2017-01-02"),
              tradingName: String = "Acme Ltd",
              businessDescription: Option[String] = Some("Accountancy services"),
              businessAddressLineOne: Option[String] = Some("1 Acme Rd."),
              businessAddressLineTwo: Option[String] = Some("London"),
              businessAddressLineThree: Option[String] = Some("Greater London"),
              businessAddressLineFour: Option[String] = Some("United Kingdom"),
              businessPostcode: Option[String] = Some("A9 9AA")): JsValue = {

      val cessation = cessationDate.map(date => s"""
                                                   |  "cessationDate": "$date",
         """.stripMargin).getOrElse("")

      val commencement = commencementDate.map(date => s"""
                                                         |  "commencementDate": "$date",
       """.stripMargin).getOrElse("")

      val businessDesc = businessDescription.map(desc => s"""
                                                            |  "businessDescription": "$desc",
       """.stripMargin).getOrElse("")

      val addrOne = businessAddressLineOne.map(line => s"""
                                                          |  "businessAddressLineOne": "$line",
       """.stripMargin).getOrElse("")

      val addrTwo = businessAddressLineTwo.map(line => s"""
                                                          |  "businessAddressLineTwo": "$line",
       """.stripMargin).getOrElse("")

      val addrThree = businessAddressLineThree.map(line => s"""
                                                              |  "businessAddressLineThree": "$line",
       """.stripMargin).getOrElse("")

      val addrFour = businessAddressLineFour.map(line => s"""
                                                            |  "businessAddressLineFour": "$line",
       """.stripMargin).getOrElse("")

      val addrPostcode = businessPostcode.map(code => s"""
                                                         |  "businessPostcode": "$code",
       """.stripMargin).getOrElse("")

      Json.parse(s"""
                    |{
                    |  "accountingPeriod": {
                    |    "start": "$accPeriodStart",
                    |    "end": "$accPeriodEnd"
                    |  },
                    |  $cessation
                    |  $commencement
                    |  $businessDesc
                    |  $addrOne
                    |  $addrTwo
                    |  $addrThree
                    |  $addrFour
                    |  $addrPostcode
                    |  "accountingType": "$accountingType",
                    |  "tradingName": "$tradingName"
                    |}
         """.stripMargin)
    }

    def update(tradingName: String = "Acme Ltd",
               businessDescription: String = "Accountancy services",
               businessAddressLineOne: String = "1 Acme Rd.",
               businessAddressLineTwo: String = "London",
               businessAddressLineThree: String = "Greater London",
               businessAddressLineFour: String = "United Kingdom",
               businessPostcode: String = "A9 9AA"): JsValue = {

      Json.parse(s"""
                    |{
                    |  "tradingName": "$tradingName",
                    |  "businessDescription": "$businessDescription",
                    |  "businessAddressLineOne": "$businessAddressLineOne",
                    |  "businessAddressLineTwo": "$businessAddressLineTwo",
                    |  "businessAddressLineThree": "$businessAddressLineThree",
                    |  "businessAddressLineFour": "$businessAddressLineFour",
                    |  "businessPostcode": "$businessPostcode"
                    |}
         """.stripMargin)
    }

    def annualSummary(annualInvestmentAllowance: BigDecimal = 500.25,
                      capitalAllowanceMainPool: BigDecimal = 500.25,
                      capitalAllowanceSpecialRatePool: BigDecimal = 500.25,
                      enhancedCapitalAllowance: BigDecimal = 500.25,
                      allowanceOnSales: BigDecimal = 500.25,
                      zeroEmissionGoodsVehicleAllowance: BigDecimal = 500.25,
                      includedNonTaxableProfits: BigDecimal = 500.25,
                      basisAdjustment: BigDecimal = -500.25,
                      overlapReliefUsed: BigDecimal = 500.25,
                      accountingAdjustment: BigDecimal = 500.25,
                      averagingAdjustment: BigDecimal = -500.25,
                      lossBroughtForward: BigDecimal = 500.25,
                      outstandingBusinessIncome: BigDecimal = 500.25,
                      balancingChargeBPRA: BigDecimal = 500.25,
                      balancingChargeOther: BigDecimal = 500.25,
                      goodsAndServicesOwnUse: BigDecimal = 500.25,
                      capitalAllowanceSingleAssetPool: BigDecimal = 500.25,
                      overlapProfitCarriedForward: BigDecimal = 500.25,
                      overlapProfitBroughtForward: BigDecimal = 500.25,
                      lossCarriedForwardTotal: BigDecimal = 500.25,
                      cisDeductionsTotal: BigDecimal = 500.25,
                      taxDeductionsFromTradingIncome: BigDecimal = 500.25,
                      class4NicProfitAdjustment: BigDecimal = 500.25,
                      businessDetailsChangedRecently: Boolean = true,
                      payVoluntaryClass2Nic: Boolean = false,
                      isExempt: Boolean = true,
                      exemptionCode: String = "003"): JsValue = {
      Json.parse(s"""
           |{
           |  "allowances": {
           |    "annualInvestmentAllowance": $annualInvestmentAllowance,
           |    "capitalAllowanceMainPool": $capitalAllowanceMainPool,
           |    "capitalAllowanceSpecialRatePool": $capitalAllowanceSpecialRatePool,
           |    "enhancedCapitalAllowance": $enhancedCapitalAllowance,
           |    "allowanceOnSales": $allowanceOnSales,
           |    "zeroEmissionGoodsVehicleAllowance": $zeroEmissionGoodsVehicleAllowance,
           |    "capitalAllowanceSingleAssetPool": $capitalAllowanceSingleAssetPool
           |  },
           |  "adjustments": {
           |    "includedNonTaxableProfits": $includedNonTaxableProfits,
           |    "basisAdjustment": $basisAdjustment,
           |    "overlapReliefUsed": $overlapReliefUsed,
           |    "accountingAdjustment": $accountingAdjustment,
           |    "averagingAdjustment": $averagingAdjustment,
           |    "lossBroughtForward": $lossBroughtForward,
           |    "outstandingBusinessIncome": $outstandingBusinessIncome,
           |    "balancingChargeBPRA": $balancingChargeBPRA,
           |    "balancingChargeOther": $balancingChargeOther,
           |    "goodsAndServicesOwnUse": $goodsAndServicesOwnUse,
           |    "overlapProfitCarriedForward": $overlapProfitCarriedForward,
           |    "overlapProfitBroughtForward": $overlapProfitBroughtForward,
           |    "lossCarriedForwardTotal": $lossCarriedForwardTotal,
           |    "cisDeductionsTotal": $cisDeductionsTotal,
           |    "taxDeductionsFromTradingIncome": $taxDeductionsFromTradingIncome,
           |    "class4NicProfitAdjustment": $class4NicProfitAdjustment
           |  },
           |  "nonFinancials": {
           |    "class4NicInfo": {
           |      "isExempt": $isExempt,
           |      "exemptionCode": "$exemptionCode"
           |    },
           |    "payVoluntaryClass2Nic": $payVoluntaryClass2Nic
           |  }
           |}
       """.stripMargin)
    }

    def periodWithSimplifiedExpenses(fromDate: Option[String] = None,
                                     toDate: Option[String] = None,
                                     turnover: BigDecimal = 0,
                                     otherIncome: BigDecimal = 0,
                                     consolidatedExpenses: Option[BigDecimal]) = {

      val (from,to) = fromToDates(fromDate, toDate)

      Json.parse(s"""
                    |{
                    |  $from
                    |  $to
                    |  "incomes": {
                    |    "turnover": { "amount": $turnover },
                    |    "other": { "amount": $otherIncome }
                    |  }
                    |
                    |  ${consolidatedExpenses.fold("")(se => s""","consolidatedExpenses": $se""")}
                    |
                    |}
                  """.stripMargin)
    }

    private def fromToDates(fromDate: Option[String] = None,
                            toDate: Option[String] = None) = {
      (fromDate
          .map { date =>
            s"""
               | "from": "$date",
         """.stripMargin
          }
          .getOrElse(""),
        toDate
          .map { date =>
            s"""
               | "to": "$date",
         """.stripMargin
          }
          .getOrElse("")
      )
    }

    def period(fromDate: Option[String] = None,
               toDate: Option[String] = None,
               turnover: BigDecimal = 0,
               otherIncome: BigDecimal = 0,
               costOfGoodsBought: (BigDecimal, BigDecimal) = (0, 0),
               cisPaymentsToSubcontractors: (BigDecimal, BigDecimal) = (0, 0),
               staffCosts: (BigDecimal, BigDecimal) = (0, 0),
               travelCosts: (BigDecimal, BigDecimal) = (0, 0),
               premisesRunningCosts: (BigDecimal, BigDecimal) = (0, 0),
               maintenanceCosts: (BigDecimal, BigDecimal) = (0, 0),
               adminCosts: (BigDecimal, BigDecimal) = (0, 0),
               advertisingCosts: (BigDecimal, BigDecimal) = (0, 0),
               interest: (BigDecimal, BigDecimal) = (0, 0),
               financialCharges: (BigDecimal, BigDecimal) = (0, 0),
               badDebt: (BigDecimal, BigDecimal) = (0, 0),
               professionalFees: (BigDecimal, BigDecimal) = (0, 0),
               depreciation: (BigDecimal, BigDecimal) = (0, 0),
               otherExpenses: (BigDecimal, BigDecimal) = (0, 0),
               consolidatedExpenses: Option[BigDecimal] = None): JsValue = {

      val (from,to) = fromToDates(fromDate, toDate)

      Json.parse(s"""
           |{
           |  $from
           |  $to
           |  "incomes": {
           |    "turnover": { "amount": $turnover },
           |    "other": { "amount": $otherIncome }
           |  },
           |  "expenses": {
           |    "costOfGoodsBought": { "amount": ${costOfGoodsBought._1}, "disallowableAmount": ${costOfGoodsBought._2} },
           |    "cisPaymentsToSubcontractors": { "amount": ${cisPaymentsToSubcontractors._1}, "disallowableAmount": ${cisPaymentsToSubcontractors._2} },
           |    "staffCosts": { "amount": ${staffCosts._1}, "disallowableAmount": ${staffCosts._2} },
           |    "travelCosts": { "amount": ${travelCosts._1}, "disallowableAmount": ${travelCosts._2} },
           |    "premisesRunningCosts": { "amount": ${premisesRunningCosts._1}, "disallowableAmount": ${premisesRunningCosts._2} },
           |    "maintenanceCosts": { "amount": ${maintenanceCosts._1}, "disallowableAmount": ${maintenanceCosts._2} },
           |    "adminCosts": { "amount": ${adminCosts._1}, "disallowableAmount": ${adminCosts._2} },
           |    "advertisingCosts": { "amount": ${advertisingCosts._1}, "disallowableAmount": ${advertisingCosts._2} },
           |    "interest": { "amount": ${interest._1}, "disallowableAmount": ${interest._2} },
           |    "financialCharges": { "amount": ${financialCharges._1}, "disallowableAmount": ${financialCharges._2} },
           |    "badDebt": { "amount": ${badDebt._1}, "disallowableAmount": ${badDebt._2} },
           |    "professionalFees": { "amount": ${professionalFees._1}, "disallowableAmount": ${professionalFees._2} },
           |    "depreciation": { "amount": ${depreciation._1}, "disallowableAmount": ${depreciation._2} },
           |    "other": { "amount": ${otherExpenses._1}, "disallowableAmount": ${otherExpenses._2} }
           |  }
           |
           |  ${consolidatedExpenses.fold("")(se => s""","consolidatedExpenses": $se""")}
           |
           |}
       """.stripMargin)
    }

  }

  object Dividends {
    def apply(amount: BigDecimal): JsValue = {
      Json.parse(s"""
           |{
           |  "ukDividends": $amount
           |}
         """.stripMargin)
    }
  }

  object Crystallisation {
    def  intentToCrystallise(): JsValue = {
      Json.parse(s"""
                    |{ }
         """.stripMargin)
    }
  }

  object TaxCalculation {
    def apply(): JsValue = {
      Json.parse(
        s"""
         {
           |  "incomeTaxYTD": 1000.25,
           |  "incomeTaxThisPeriod": 1000.25,
           |    "payFromAllEmployments": 200.22,
           |    "benefitsAndExpensesReceived": 200.22,
           |    "allowableExpenses": 200.22,
           |    "payFromAllEmploymentsAfterExpenses": 200.22,
           |    "shareSchemes": 200.22,
           |    "profitFromSelfEmployment": 200.22,
           |    "profitFromPartnerships": 200.22,
           |    "profitFromUkLandAndProperty": 200.22,
           |    "dividendsFromForeignCompanies": 200.22,
           |    "foreignIncome": 200.22,
           |    "trustsAndEstates": 200.22,
           |    "interestReceivedFromUkBanksAndBuildingSocieties": 200.22,
           |    "dividendsFromUkCompanies": 200.22,
           |    "ukPensionsAndStateBenefits": 200.22,
           |    "gainsOnLifeInsurance": 200.22,
           |    "otherIncome": 200.22,
           |    "totalIncomeReceived": 200.22,
           |    "paymentsIntoARetirementAnnuity": 200.22,
           |    "foreignTaxOnEstates": 200.22,
           |    "incomeTaxRelief": 200.22,
           |    "incomeTaxReliefReducedToMaximumAllowable": 200.22,
           |    "annuities": 200.22,
           |    "giftOfInvestmentsAndPropertyToCharity": 200.22,
           |    "personalAllowance": 200,
           |    "marriageAllowanceTransfer": 200.22,
           |    "blindPersonAllowance": 200.22,
           |    "blindPersonSurplusAllowanceFromSpouse": 200.22,
           |    "incomeExcluded": 200.22,
           |    "totalIncomeAllowancesUsed": 200.22,
           |    "totalIncomeOnWhichTaxIsDue": 200.22,
           |    "payPensionsExtender": 200.22,
           |    "giftExtender": 200.22,
           |    "extendedBR": 200.22,
           |    "payPensionsProfitAtBRT": 200.22,
           |    "incomeTaxOnPayPensionsProfitAtBRT": 200.22,
           |    "payPensionsProfitAtHRT": 200.22,
           |    "incomeTaxOnPayPensionsProfitAtHRT": 200.22,
           |    "payPensionsProfitAtART": 200.22,
           |    "incomeTaxOnPayPensionsProfitAtART": 200.22,
           |    "netPropertyFinanceCosts": 200.22,
           |    "interestReceivedAtStartingRate": 200.22,
           |    "incomeTaxOnInterestReceivedAtStartingRate": 200.22,
           |    "interestReceivedAtZeroRate": 200.22,
           |    "incomeTaxOnInterestReceivedAtZeroRate": 200.22,
           |    "interestReceivedAtBRT": 200.22,
           |    "incomeTaxOnInterestReceivedAtBRT": 200.22,
           |    "interestReceivedAtHRT": 200.22,
           |    "incomeTaxOnInterestReceivedAtHRT": 200.22,
           |    "interestReceivedAtART": 200.22,
           |    "incomeTaxOnInterestReceivedAtART": 200.22,
           |    "dividendsAtZeroRate": 200.22,
           |    "incomeTaxOnDividendsAtZeroRate": 200.22,
           |    "dividendsAtBRT": 200.22,
           |    "incomeTaxOnDividendsAtBRT": 200.22,
           |    "dividendsAtHRT": 200.22,
           |    "incomeTaxOnDividendsAtHRT": 200.22,
           |    "dividendsAtART": 200.22,
           |    "incomeTaxOnDividendsAtART": 200.22,
           |    "totalIncomeOnWhichTaxHasBeenCharged": 200.22,
           |    "taxOnOtherIncome": 200.22,
           |    "incomeTaxDue": 200.22,
           |    "incomeTaxCharged": 200.22,
           |    "deficiencyRelief": 200.22,
           |    "topSlicingRelief": 200.22,
           |    "ventureCapitalTrustRelief": 200.22,
           |    "enterpriseInvestmentSchemeRelief": 200.22,
           |    "seedEnterpriseInvestmentSchemeRelief": 200.22,
           |    "communityInvestmentTaxRelief": 200.22,
           |    "socialInvestmentTaxRelief": 200.22,
           |    "maintenanceAndAlimonyPaid": 200.22,
           |    "marriedCouplesAllowance": 200.22,
           |    "marriedCouplesAllowanceRelief": 200.22,
           |    "surplusMarriedCouplesAllowance": 200.22,
           |    "surplusMarriedCouplesAllowanceRelief": 200.22,
           |    "notionalTaxFromLifePolicies": 200.22,
           |    "notionalTaxFromDividendsAndOtherIncome": 200.22,
           |    "foreignTaxCreditRelief": 200.22,
           |    "incomeTaxDueAfterAllowancesAndReliefs": 200.22,
           |    "giftAidPaymentsAmount": 200.22,
           |    "giftAidTaxDue": 200.22,
           |    "capitalGainsTaxDue": 200.22,
           |    "remittanceForNonDomiciles": 200.22,
           |    "highIncomeChildBenefitCharge": 200.22,
           |    "totalGiftAidTaxReduced": 200.22,
           |    "incomeTaxDueAfterGiftAidReduction": 200.22,
           |    "annuityAmount": 200.22,
           |    "taxDueOnAnnuity": 200.22,
           |    "taxCreditsOnDividendsFromUkCompanies": 200.22,
           |    "incomeTaxDueAfterDividendTaxCredits": 200.22,
           |    "nationalInsuranceContributionAmount": 200.22,
           |    "nationalInsuranceContributionCharge": 200.22,
           |    "nationalInsuranceContributionSupAmount": 200.22,
           |    "nationalInsuranceContributionSupCharge": 200.22,
           |    "totalClass4Charge": 200.22,
           |    "nationalInsuranceClass1Amount": 200.22,
           |    "nationalInsuranceClass2Amount": 200.22,
           |    "nicTotal": 200.22,
           |    "underpaidTaxForPreviousYears": 200.22,
           |    "studentLoanRepayments": 200.22,
           |    "pensionChargesGross": 200.22,
           |    "pensionChargesTaxPaid": 200.22,
           |    "totalPensionSavingCharges": 200.22,
           |    "pensionLumpSumAmount": 200.22,
           |    "pensionLumpSumRate": 200.22,
           |    "statePensionLumpSumAmount": 200.22,
           |    "remittanceBasisChargeForNonDomiciles": 200.22,
           |    "additionalTaxDueOnPensions": 200.22,
           |    "additionalTaxReliefDueOnPensions": 200.22,
           |    "incomeTaxDueAfterPensionDeductions": 200.22,
           |    "employmentsPensionsAndBenefits": 200.22,
           |    "outstandingDebtCollectedThroughPaye": 200.22,
           |    "payeTaxBalance": 200.22,
           |    "cisAndTradingIncome": 200.22,
           |    "partnerships": 200.22,
           |    "ukLandAndPropertyTaxPaid": 200.22,
           |    "foreignIncomeTaxPaid": 200.22,
           |    "trustAndEstatesTaxPaid": 200.22,
           |    "overseasIncomeTaxPaid": 200.22,
           |    "interestReceivedTaxPaid": 200.22,
           |    "voidISAs": 200.22,
           |    "otherIncomeTaxPaid": 200.22,
           |    "underpaidTaxForPriorYear": 200.22,
           |    "totalTaxDeducted": 200.22,
           |    "incomeTaxOverpaid": 200.22,
           |    "incomeTaxDueAfterDeductions": 200.22,
           |    "propertyFinanceTaxDeduction": 200.22,
           |    "taxableCapitalGains": 200.22,
           |    "capitalGainAtEntrepreneurRate": 200.22,
           |    "incomeTaxOnCapitalGainAtEntrepreneurRate": 200.22,
           |    "capitalGrainsAtLowerRate": 200.22,
           |    "incomeTaxOnCapitalGainAtLowerRate": 200.22,
           |    "capitalGainAtHigherRate": 200.22,
           |    "incomeTaxOnCapitalGainAtHigherTax": 200.22,
           |    "capitalGainsTaxAdjustment": 200.22,
           |    "foreignTaxCreditReliefOnCapitalGains": 200.22,
           |    "liabilityFromOffShoreTrusts": 200.22,
           |    "taxOnGainsAlreadyCharged": 200.22,
           |    "totalCapitalGainsTax": 200.22,
           |    "incomeAndCapitalGainsTaxDue": 200.22,
           |    "taxRefundedInYear": 200.22,
           |    "unpaidTaxCalculatedForEarlierYears": 200.22,
           |    "marriageAllowanceTransferAmount": 200.22,
           |    "marriageAllowanceTransferRelief": 200.22,
           |    "marriageAllowanceTransferMaximumAllowable": 200.22,
           |    "nationalRegime": "abc",
           |    "allowance": 200,
           |    "limitBRT": 200,
           |    "limitHRT": 200,
           |    "rateBRT": 20.00,
           |    "rateHRT": 40.00,
           |    "rateART": 50.00,
           |    "limitAIA": 200,
           |    "allowanceBRT": 200,
           |    "interestAllowanceHRT": 200,
           |    "interestAllowanceBRT": 200,
           |    "dividendAllowance": 200,
           |    "dividendBRT": 20.00,
           |    "dividendHRT": 40.00,
           |    "dividendART": 50.00,
           |    "class2NICsLimit": 200,
           |    "class2NICsPerWeek": 200.22,
           |    "class4NICsLimitBR": 200,
           |    "class4NICsLimitHR": 200,
           |    "class4NICsBRT": 20.00,
           |    "class4NICsHRT": 40.00,
           |    "proportionAllowance": 200,
           |    "proportionLimitBRT": 200,
           |    "proportionLimitHRT": 200,
           |    "proportionalTaxDue": 200.22,
           |    "proportionInterestAllowanceBRT": 200,
           |    "proportionInterestAllowanceHRT": 200,
           |    "proportionDividendAllowance": 200,
           |    "proportionPayPensionsProfitAtART": 200,
           |    "proportionIncomeTaxOnPayPensionsProfitAtART": 200,
           |    "proportionPayPensionsProfitAtBRT": 200,
           |    "proportionIncomeTaxOnPayPensionsProfitAtBRT": 200,
           |    "proportionPayPensionsProfitAtHRT": 200,
           |    "proportionIncomeTaxOnPayPensionsProfitAtHRT": 200,
           |    "proportionInterestReceivedAtZeroRate": 200,
           |    "proportionIncomeTaxOnInterestReceivedAtZeroRate": 200,
           |    "proportionInterestReceivedAtBRT": 200,
           |    "proportionIncomeTaxOnInterestReceivedAtBRT": 200,
           |    "proportionInterestReceivedAtHRT": 200,
           |    "proportionIncomeTaxOnInterestReceivedAtHRT": 200,
           |    "proportionInterestReceivedAtART": 200,
           |    "proportionIncomeTaxOnInterestReceivedAtART": 200,
           |    "proportionDividendsAtZeroRate": 200,
           |    "proportionIncomeTaxOnDividendsAtZeroRate": 200,
           |    "proportionDividendsAtBRT": 200,
           |    "proportionIncomeTaxOnDividendsAtBRT": 200,
           |    "proportionDividendsAtHRT": 200,
           |    "proportionIncomeTaxOnDividendsAtHRT": 200,
           |    "proportionDividendsAtART": 200,
           |    "proportionIncomeTaxOnDividendsAtART": 200,
           |    "proportionClass2NICsLimit": 200,
           |    "proportionClass4NICsLimitBR": 200,
           |    "proportionClass4NICsLimitHR": 200,
           |    "proportionReducedAllowanceLimit": 200,
           |      "eoyEstimate":{  
           |          "selfEmployment":[  
           |          {
           |              "id":"selfEmploymentId1",
           |              "taxableIncome":89999999.99,
           |              "supplied":true,
           |              "finalised":true
           |          },
           |          {
           |              "id":"selfEmploymentId2",
           |              "taxableIncome":89999999.99,
           |              "supplied":true,
           |              "finalised":true
                      }
           |          ],
           |          "ukProperty":[{  
           |              "taxableIncome":89999999.99,
           |              "supplied":true,
           |              "finalised":true
           |           }],
           |           "totalTaxableIncome":89999999.99,
           |           "incomeTaxAmount":89999999.99,
           |           "nic2":89999999.99,
           |           "nic4":89999999.99,
           |           "totalNicAmount":9999999.99,
           |           "incomeTaxNicAmount":999999.99
           |    }
           |}
         """.stripMargin
      )
    }

    def eta(seconds: Int): JsValue = {
      Json.parse(s"""
           |{
           |  "etaSeconds": $seconds
           |}
         """.stripMargin)
    }

    def request(taxYear: String = "2017-18"): JsValue = {
      Json.parse(s"""
           |{
           |  "taxYear": "$taxYear"
           |}
         """.stripMargin)
    }
  }

  object Obligations {
    def apply(firstMet: Boolean = false,
              secondMet: Boolean = false,
              thirdMet: Boolean = false,
              fourthMet: Boolean = false): JsValue = {
      Json.parse(s"""
           |{
           |  "obligations": [
           |    {
           |      "start": "2017-04-06",
           |      "end": "2017-07-05",
           |      "due": "2017-08-05",
           |      "met": $firstMet
           |    },
           |    {
           |      "start": "2017-07-06",
           |      "end": "2017-10-05",
           |      "due": "2017-11-05",
           |      "met": $secondMet
           |    },
           |    {
           |      "start": "2017-10-06",
           |      "end": "2018-01-05",
           |      "due": "2018-02-05",
           |      "met": $thirdMet
           |    },
           |    {
           |      "start": "2018-01-06",
           |      "end": "2018-04-05",
           |      "due": "2018-05-06",
           |      "met": $fourthMet
           |    }
           |  ]
           |}
         """.stripMargin)
    }


    def eops(firstMet: Boolean = false,
             secondMet: Boolean = false,
             thirdMet: Boolean = false,
             fourthMet: Boolean = false): JsValue = {
      Json.parse(s"""
           |{
           |  "obligations": [
           |    {
           |      "start": "2017-04-06",
           |      "end": "2017-07-05",
           |      "due": "2017-08-05",
           |      "status": "${if (firstMet) "Fulfilled" else "Open"}"
           |    },
           |    {
           |      "start": "2017-07-06",
           |      "end": "2017-10-05",
           |      "due": "2017-11-05",
           |      "processed": "2017-11-01",
           |      "status": "${if (secondMet) "Fulfilled" else "Open"}"
           |    },
           |    {
           |      "start": "2017-10-06",
           |      "end": "2018-01-05",
           |      "due": "2018-02-05",
           |      "processed": "2018-02-01",
           |      "status": "${if (thirdMet) "Fulfilled" else "Open"}"
           |    },
           |    {
           |      "start": "2018-01-06",
           |      "end": "2018-04-05",
           |      "due": "2018-05-06",
           |      "processed": "2018-05-01",
           |      "status": "${if (fourthMet) "Fulfilled" else "Open"}"
           |    }
           |  ]
           |}
         """.
        stripMargin
      )
    }
  }
}
