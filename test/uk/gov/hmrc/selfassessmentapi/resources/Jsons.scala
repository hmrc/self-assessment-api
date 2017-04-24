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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}

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

    val invalidNino: String = errorWithMessage("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
    val ninoInvalid: String = errorWithMessage("NINO_INVALID", "The provided Nino is invalid")
    val invalidPayload: String = errorWithMessage("INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
    val invalidRequest: String = errorWithMessage("INVALID_REQUEST", "Invalid request")
    val ninoNotFound: String = errorWithMessage("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found.")
    val desNotFound: String = errorWithMessage("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")
    val duplicateTradingName: String = errorWithMessage("CONFLICT", "Duplicated trading name.")
    val notFound: String = errorWithMessage("NOT_FOUND", "Resource was not found")
    val invalidPeriod: String = businessErrorWithMessage("INVALID_PERIOD" -> "The remote endpoint has indicated that a overlapping period was submitted.")
    val invalidOriginatorId: String = errorWithMessage("INVALID_ORIGINATOR_ID", "Submission has not passed validation. Invalid header Originator-Id.")
    val internalServerError: String = errorWithMessage("INTERNAL_SERVER_ERROR", "An internal server error occurred")
    val invalidCalcId: String = errorWithMessage("INVALID_CALCID", "Submission has not passed validation")
    val unauthorised: String = errorWithMessage("UNAUTHORIZED", "Bearer token is missing or not authorized")

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
         |    ${ errors.map { error }.mkString(",") }
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
         |    ${
        errors.map {
          case (code, msg) => errorWithMessage(code, msg)
        }.mkString(",")
      }
         |  ]
         |}
         """.stripMargin
    }
  }

  object Banks {
    def apply(accountName: String = "Savings Account"): JsValue = {
      Json.parse(
        s"""
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

      Json.parse(
        s"""
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
                  otherCost: BigDecimal = 0): JsValue = {

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

      Json.parse(
        s"""
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
                    otherCost: BigDecimal = 0): JsValue = {

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

      Json.parse(
        s"""
           |{
           |  $from
           |  $to
           |  "incomes": {
           |    "rentIncome": { "amount": $rentIncome, "taxDeducted": $rentIncomeTaxDeducted },
           |    ${
          premiumsOfLeaseGrant
            .map(income => s""" "premiumsOfLeaseGrant": { "amount": $income },""")
            .getOrElse("")
        }
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
           |}
       """.stripMargin)
    }

    def periodSummary(dates: (String, String)*): JsValue = {
      val nestedDates = dates
        .map { date =>
          s"""
             |{
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
      Json.parse(
        s"""
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
      Json.parse(
        s"""
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

      val cessation = cessationDate.map(date =>
        s"""
           |  "cessationDate": "$date",
         """.stripMargin).getOrElse("")

      val commencement = commencementDate.map(date =>
        s"""
           |  "commencementDate": "$date",
       """.stripMargin).getOrElse("")

      val businessDesc = businessDescription.map(desc =>
        s"""
           |  "businessDescription": "$desc",
       """.stripMargin).getOrElse("")

      val addrOne = businessAddressLineOne.map(line =>
        s"""
           |  "businessAddressLineOne": "$line",
       """.stripMargin).getOrElse("")

      val addrTwo = businessAddressLineTwo.map(line =>
        s"""
           |  "businessAddressLineTwo": "$line",
       """.stripMargin).getOrElse("")

      val addrThree = businessAddressLineThree.map(line =>
        s"""
           |  "businessAddressLineThree": "$line",
       """.stripMargin).getOrElse("")

      val addrFour = businessAddressLineFour.map(line =>
        s"""
           |  "businessAddressLineFour": "$line",
       """.stripMargin).getOrElse("")

      val addrPostcode = businessPostcode.map(code =>
        s"""
           |  "businessPostcode": "$code",
       """.stripMargin).getOrElse("")

      Json.parse(
        s"""
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

      Json.parse(
        s"""
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
                      businessPremisesRenovationAllowance: BigDecimal = 500.25,
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
                      goodsAndServicesOwnUse: BigDecimal = 500.25): JsValue = {
      Json.parse(
        s"""
           |{
           |  "allowances": {
           |    "annualInvestmentAllowance": $annualInvestmentAllowance,
           |    "capitalAllowanceMainPool": $capitalAllowanceMainPool,
           |    "capitalAllowanceSpecialRatePool": $capitalAllowanceSpecialRatePool,
           |    "businessPremisesRenovationAllowance": $businessPremisesRenovationAllowance,
           |    "enhancedCapitalAllowance": $enhancedCapitalAllowance,
           |    "allowanceOnSales": $allowanceOnSales,
           |    "zeroEmissionGoodsVehicleAllowance": $zeroEmissionGoodsVehicleAllowance
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
           |    "goodsAndServicesOwnUse": $goodsAndServicesOwnUse
           |  }
           |}
       """.stripMargin)
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
               otherExpenses: (BigDecimal, BigDecimal) = (0, 0)): JsValue = {

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

      Json.parse(
        s"""
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
           |}
       """.stripMargin)
    }

  }

  object Dividends {
    def apply(amount: BigDecimal): JsValue = {
      Json.parse(
        s"""
           |{
           |  "ukDividends": $amount
           |}
         """.stripMargin)
    }
  }

  object TaxCalculation {
    def apply(): JsValue = {
      Json.parse(
        s"""
           |{
           |            "profitFromSelfEmployment": 200.00,
           |            "profitFromUkLandAndProperty": 200.00,
           |            "interestReceivedFromUkBanksAndBuildingSocieties": 200.00,
           |            "dividendsFromUkCompanies": 200.00,
           |            "totalIncomeReceived": 200.00,
           |            "personalAllowance": 200.00,
           |            "totalIncomeOnWhichTaxIsDue": 200.00,
           |            "payPensionsProfitAtBRT": 200.00,
           |            "incomeTaxOnPayPensionsProfitAtBRT": 200.00,
           |            "payPensionsProfitAtHRT": 200.00,
           |            "incomeTaxOnPayPensionsProfitAtHRT": 200.00,
           |            "payPensionsProfitAtART": 200.00,
           |            "incomeTaxOnPayPensionsProfitAtART": 200.00,
           |            "interestReceivedAtStartingRate": 200.00,
           |            "incomeTaxOnInterestReceivedAtStartingRate": 200.00,
           |            "interestReceivedAtZeroRate": 200.00,
           |            "incomeTaxOnInterestReceivedAtZeroRate": 200.00,
           |            "interestReceivedAtBRT": 200.00,
           |            "incomeTaxOnInterestReceivedAtBRT": 200.00,
           |            "interestReceivedAtHRT": 200.00,
           |            "incomeTaxOnInterestReceivedAtHRT": 200.00,
           |            "interestReceivedAtART": 200.00,
           |            "incomeTaxOnInterestReceivedAtART": 200.00,
           |            "dividendsAtZeroRate": 200.00,
           |            "incomeTaxOnDividendsAtZeroRate": 200.00,
           |            "dividendsAtBRT": 200.00,
           |            "incomeTaxOnDividendsAtBRT": 200.00,
           |            "dividendsAtHRT": 200.00,
           |            "incomeTaxOnDividendsAtHRT": 200.00,
           |            "dividendsAtART": 200.00,
           |            "incomeTaxOnDividendsAtART": 200.00,
           |            "totalIncomeOnWhichTaxHasBeenCharged": 200.00,
           |            "incomeTaxDue": 200.00,
           |            "incomeTaxCharged": 200.00,
           |            "taxCreditsOnDividendsFromUkCompanies": 200.00,
           |            "incomeTaxDueAfterDividendTaxCredits": 200.00,
           |            "allowance": 200.00,
           |            "limitBRT": 200.00,
           |            "limitHRT": 200.00,
           |            "rateBRT": 200.00,
           |            "rateHRT": 200.00,
           |            "rateART": 200.00,
           |            "limitAIA": 200.00,
           |            "allowanceBRT": 200.00,
           |            "interestAllowanceHRT": 200.00,
           |            "interestAllowanceBRT": 200.00,
           |            "dividendAllowance": 200.00,
           |            "dividendBRT": 200.00,
           |            "dividendHRT": 200.00,
           |            "dividendART": 200.00,
           |            "proportionAllowance": 200.00,
           |            "proportionLimitBRT": 200.00,
           |            "proportionLimitHRT": 200.00,
           |            "proportionalTaxDue": 200.00,
           |            "proportionInterestAllowanceBRT": 200.00,
           |            "proportionInterestAllowanceHRT": 200.00,
           |            "proportionDividendAllowance": 200.00,
           |            "proportionPayPensionsProfitAtART": 200.00,
           |            "proportionIncomeTaxOnPayPensionsProfitAtART": 200.00,
           |            "proportionPayPensionsProfitAtBRT": 200.00,
           |            "proportionIncomeTaxOnPayPensionsProfitAtBRT": 200.00,
           |            "proportionPayPensionsProfitAtHRT": 200.00,
           |            "proportionIncomeTaxOnPayPensionsProfitAtHRT": 200.00,
           |            "proportionInterestReceivedAtZeroRate": 200.00,
           |            "proportionIncomeTaxOnInterestReceivedAtZeroRate": 200.00,
           |            "proportionInterestReceivedAtBRT": 200.00,
           |            "proportionIncomeTaxOnInterestReceivedAtBRT": 200.00,
           |            "proportionInterestReceivedAtHRT": 200.00,
           |            "proportionIncomeTaxOnInterestReceivedAtHRT": 200.00,
           |            "proportionInterestReceivedAtART": 200.00,
           |            "proportionIncomeTaxOnInterestReceivedAtART": 200.00,
           |            "proportionDividendsAtZeroRate": 200.00,
           |            "proportionIncomeTaxOnDividendsAtZeroRate": 200.00,
           |            "proportionDividendsAtBRT": 200.00,
           |            "proportionIncomeTaxOnDividendsAtBRT": 200.00,
           |            "proportionDividendsAtHRT": 200.00,
           |            "proportionIncomeTaxOnDividendsAtHRT": 200.00,
           |            "proportionDividendsAtART": 200.00,
           |            "proportionIncomeTaxOnDividendsAtART": 200.00
           |        }
         """.stripMargin
      )
    }

    def eta(seconds: Int): JsValue = {
      Json.parse(
        s"""
           |{
           |  "etaSeconds": $seconds
           |}
         """.stripMargin)
    }

    def request(taxYear: String = "2017-18"): JsValue = {
      Json.parse(
        s"""
           |{
           |  "taxYear": "$taxYear"
           |}
         """.stripMargin)
    }
  }

  object Obligations {
    def apply(firstMet: Boolean = false, secondMet: Boolean = false,
              thirdMet: Boolean = false, fourthMet: Boolean = false): JsValue = {
      Json.parse(
        s"""
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
  }

}
