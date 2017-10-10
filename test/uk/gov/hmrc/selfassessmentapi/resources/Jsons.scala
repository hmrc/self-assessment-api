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
              description: Option[String] = Some("Accountancy services"),
              lineOne: Option[String] = Some("1 Acme Rd."),
              lineTwo: Option[String] = Some("London"),
              lineThree: Option[String] = Some("Greater London"),
              lineFour: Option[String] = Some("United Kingdom"),
              postalCode: Option[String] = Some("A9 9AA"),
              countryCode: Option[String] = Some("GB")): JsValue = {

      val cessation = cessationDate.map(date => s"""
           |  "cessationDate": "$date",
         """.stripMargin).getOrElse("")

      val commencement = commencementDate.map(date => s"""
           |  "commencementDate": "$date",
       """.stripMargin).getOrElse("")

      val businessDesc = description.map(desc => s"""
           |  "description": "$desc",
       """.stripMargin).getOrElse("")

      val addrOne = lineOne.map(line => s"""
           |  "lineOne": "$line",
       """.stripMargin).getOrElse("")

      val addrTwo = lineTwo.map(line => s"""
           |  "lineTwo": "$line",
       """.stripMargin).getOrElse("")

      val addrThree = lineThree.map(line => s"""
           |  "lineThree": "$line",
       """.stripMargin).getOrElse("")

      val addrFour = lineFour.map(line => s"""
           |  "lineFour": "$line",
       """.stripMargin).getOrElse("")

      val addrPostcode = postalCode.map(code => s"""
           |  "postalCode": "$code",
       """.stripMargin).getOrElse("")

      val addrCountry = countryCode.map(code => s"""
           |  "countryCode": "$code"
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
           |  "address": {
           |  $addrOne
           |  $addrTwo
           |  $addrThree
           |  $addrFour
           |  $addrPostcode
           |  $addrCountry
           |  },
           |  "accountingType": "$accountingType",
           |  "tradingName": "$tradingName"
           |}
         """.stripMargin)
    }

    def update(accPeriodStart: String = "2017-04-06",
               accPeriodEnd: String = "2018-04-05",
               accountingType: String = "CASH",
               commencementDate: String = "2017-01-01",
               effectiveDate: String = "2017-01-02",
               cessationReason: CessationReason = CessationReason.Bankruptcy,
               tradingName: String = "Acme Ltd",
               description: String = "Accountancy services",
               lineOne: String = "1 Acme Rd.",
               lineTwo: String = "London",
               lineThree: String = "Greater London",
               lineFour: String = "United Kingdom",
               postalCode: Option[String] = Some("A9 9AA"),
               countryCode: String = "GB",
               primaryPhoneNumber : String = "0734343434",
               secondaryPhoneNumber : String = "0734343434",
               faxNumber : String = "0734343434",
               emailAddress : String = "admin@mail.com",
               paperless: Boolean = false,
               seasonal: Boolean = false): JsValue = {

      val addrPostcode = postalCode.map(code =>
        s"""
           |  "postalCode": "$code",
       """.stripMargin).getOrElse("")

      Json.parse(
        s"""
           |{
           |  "accountingPeriod": {
           |    "start": "$accPeriodStart",
           |    "end": "$accPeriodEnd"
           |  },
           |  "accountingType": "$accountingType",
           |  "commencementDate": "$commencementDate",
           |  "effectiveDate": "$effectiveDate",
           |  "cessationReason": "$cessationReason",
           |  "tradingName": "$tradingName",
           |  "description": "$description",
           |  "address": {
           |    "lineOne": "$lineOne",
           |    "lineTwo": "$lineTwo",
           |    "lineThree": "$lineThree",
           |    "lineFour": "$lineFour",
           |    $addrPostcode
           |    "countryCode": "$countryCode"
           |  },
           |  "contactDetails": {
           |    "primaryPhoneNumber": "$primaryPhoneNumber",
           |    "secondaryPhoneNumber": "$secondaryPhoneNumber",
           |    "faxNumber": "$faxNumber",
           |    "emailAddress": "$emailAddress"
           |  },
           |  "paperless": $paperless,
           |  "seasonal": $seasonal
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
                      goodsAndServicesOwnUse: BigDecimal = 500.25,
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

  object TaxCalculation {
    def apply(id: String = "abc"): JsValue = {
      Json.parse(
        s"""
          |{
          | "calcOutput": {
          |		"calcName": "abcdefghijklmnopqr",
          |		"calcVersion": "abcdef",
          |		"calcVersionDate": "2016-01-01",
          |		"calcID": "$id",
          |		"sourceName": "abcdefghijklmno",
          |		"sourceRef": "abcdefghijklmnopqrs",
          |		"identifier": "abcdefghijklm",
          |		"year": 2016,
          |		"periodFrom": "2016-01-01",
          |		"periodTo": "2016-01-01",
          |		"calcAmount": 1000.25,
          |		"calcTimestamp": "4498-07-06T21:42:24.294Z",
          |		"calcResult": {
          |			"incomeTaxNicYtd": 1000.25,
          |			"incomeTaxNicDelta": 1000.25,
          |			"crystallised": true,
          |			"nationalRegime": "UK",
          |			"totalTaxableIncome": 1000.25,
          |			"taxableIncome": {
          |				"totalIncomeReceived": 1000.25,
          |				"incomeReceived": {
          |					"employmentIncome": 1000.25,
          |					"employments": {
          |						"totalPay": 1000.25,
          |						"totalBenefitsAndExpenses": 1000.25,
          |						"totalAllowableExpenses": 1000.25,
          |						"employment": [
          |							{
          |								"incomeSourceID": "abcdefghijklm",
          |								"latestDate": "2016-01-01",
          |								"netPay": 1000.25,
          |								"benefitsAndExpenses": 1000.25,
          |								"allowableExpenses": 1000.25
          |							}
          |						]
          |					},
          |					"shareSchemeIncome": 1000.25,
          |					"shareSchemes": {
          |						"incomeSourceID": "abcdefghijklm",
          |						"latestDate": "2016-01-01"
          |					},
          |					"selfEmploymentIncome": 1000.25,
          |					"selfEmployment": [
          |						{
          |							"incomeSourceID": "abcdefghijklm",
          |							"latestDate": "2016-01-01",
          |							"accountStartDate": "2016-01-01",
          |							"accountEndDate": "2016-01-01",
          |							"taxableIncome": 1000.25,
          |							"finalised": true
          |						}
          |					],
          |					"partnershipIncome": 1000.25,
          |					"partnership": [
          |						{
          |							"incomeSourceID": "abcdefghijklm",
          |							"latestDate": "2016-01-01",
          |							"taxableIncome": 1000.25
          |						}
          |					],
          |					"ukPropertyIncome": 1000.25,
          |					"ukProperty": {
          |						"latestDate": "2016-01-01",
          |						"taxableProfit": 1000.25,
          |						"taxableProfitFhlUk": 1000.25,
          |						"taxableProfitFhlEea": 1000.25,
          |						"finalised": true
          |					},
          |					"foreignIncome": 1000.25,
          |					"foreign": {
          |						"incomeSourceID": "abcdefghijklm",
          |						"latestDate": "2016-01-01"
          |					},
          |					"foreignDividendIncome": 1000.25,
          |					"foreignDividends": {
          |						"incomeSourceID": "abcdefghijklm",
          |						"latestDate": "2016-01-01"
          |					},
          |					"trustsIncome": 1000.25,
          |					"trusts": {
          |						"incomeSourceID": "abcdefghijklm",
          |						"latestDate": "2016-01-01"
          |					},
          |					"bbsiIncome": 1000.25,
          |					"bbsi": {
          |						"incomeSourceID": "abcdefghijklm",
          |						"latestDate": "2016-01-01",
          |						"interestReceived": 1000.25
          |					},
          |					"ukDividendIncome": 1000.25,
          |					"ukDividends": {
          |						"incomeSourceID": "abcdefghijklm",
          |						"latestDate": "2016-01-01"
          |					},
          |					"ukPensionsIncome": 1000.25,
          |					"ukPensions": {
          |						"incomeSourceID": "abcdefghijklm",
          |						"latestDate": "2016-01-01"
          |					},
          |					"gainsOnLifeInsuranceIncome": 1000.25,
          |					"gainsOnLifeInsurance": {
          |						"incomeSourceID": "abcdefghijklm",
          |						"latestDate": "2016-01-01"
          |					},
          |					"otherIncome": 1000.25
          |				},
          |				"totalAllowancesAndDeductions": 1000.25,
          |				"allowancesAndDeductions": {
          |					"paymentsIntoARetirementAnnuity": 1000.25,
          |					"foreignTaxOnEstates": 1000.25,
          |					"incomeTaxRelief": 1000.25,
          |					"annuities": 1000.25,
          |					"giftOfInvestmentsAndPropertyToCharity": 1000.25,
          |					"apportionedPersonalAllowance": 1000.25,
          |					"marriageAllowanceTransfer": 1000.25,
          |					"blindPersonAllowance": 1000.25,
          |					"blindPersonSurplusAllowanceFromSpouse": 1000.25,
          |					"incomeExcluded": 1000.25
          |				}
          |			},
          |			"totalIncomeTax": 1000.25,
          |			"incomeTax": {
          |				"totalBeforeReliefs": 1000.25,
          |				"taxableIncome": 1000.25,
          |				"payPensionsProfit": {
          |					"totalAmount": 1000.25,
          |					"taxableIncome": 1000.25,
          |					"band": [
          |						{
          |							"name": "abcdefghijklm",
          |							"rate": 99.99,
          |							"threshold": 99999999,
          |							"apportionedThreshold": 99999999,
          |							"income": 1000.25,
          |							"taxAmount": 1000.25
          |						}
          |					]
          |				},
          |				"savingsAndGains": {
          |					"totalAmount": 1000.25,
          |					"taxableIncome": 1000.25,
          |					"band": [
          |						{
          |							"name": "abcdefghijklm",
          |							"rate": 99.99,
          |							"threshold": 99999999,
          |							"apportionedThreshold": 99999999,
          |							"income": 1000.25,
          |							"taxAmount": 1000.25
          |						}
          |					]
          |				},
          |				"dividends": {
          |					"totalAmount": 1000.25,
          |					"taxableIncome": 1000.25,
          |					"band": [
          |						{
          |							"name": "abcdefghijklm",
          |							"rate": 99.99,
          |							"threshold": 99999999,
          |							"apportionedThreshold": 99999999,
          |							"income": 1000.25,
          |							"taxAmount": 1000.25
          |						}
          |					]
          |				},
          |				"excludedIncome": 1000.25,
          |				"totalAllowancesAndReliefs": 1000.25,
          |				"allowancesAndReliefs": {
          |					"deficiencyRelief": 1000.25,
          |					"topSlicingRelief": 1000.25,
          |					"ventureCapitalTrustRelief": 1000.25,
          |					"enterpriseInvestmentSchemeRelief": 1000.25,
          |					"seedEnterpriseInvestmentSchemeRelief": 1000.25,
          |					"communityInvestmentTaxRelief": 1000.25,
          |					"socialInvestmentTaxRelief": 1000.25,
          |					"maintenanceAndAlimonyPaid": 1000.25,
          |					"marriedCoupleAllowanceRate": 1000.25,
          |					"marriedCoupleAllowanceAmount": 1000.25,
          |					"marriedCoupleAllowanceRelief": 1000.25,
          |					"surplusMarriedCoupleAllowanceAmount": 1000.25,
          |					"surplusMarriedCoupleAllowanceRelief": 1000.25,
          |					"notionalTaxFromLifePolicies": 1000.25,
          |					"notionalTaxFromDividendsAndOtherIncome": 1000.25,
          |					"foreignTaxCreditRelief": 1000.25,
          |					"propertyFinanceRelief": 1000.25
          |				}
          |			},
          |			"totalNic": 1000.25,
          |			"nic": {
          |				"class2": {
          |					"amount": 1000.25,
          |					"weekRate": 1000.25,
          |					"weeks": 1,
          |					"limit": 99999999,
          |					"apportionedLimit": 2
          |				},
          |				"class4": {
          |					"totalAmount": 1000.25,
          |					"band": [
          |						{
          |							"name": "abcdefghijklm",
          |							"rate": 99.99,
          |							"threshold": 99999999,
          |							"apportionedThreshold": 99999999,
          |							"income": 1000.25,
          |							"amount": 1000.25
          |						}
          |					]
          |				}
          |			},
          |			"eoyEstimate": {
          |				"incomeSource": [
          |					{
          |						"id": "abcdefghijklm",
          |						"type": "01",
          |						"taxableIncome": 99999999,
          |						"supplied": true,
          |						"finalised": true
          |					}
          |				],
          |				"totalTaxableIncome": 99999999,
          |				"incomeTaxAmount": 99999999,
          |				"nic2": 99999999,
          |				"nic4": 99999999,
          |				"totalNicAmount": 99999999,
          |				"incomeTaxNicAmount": 2
          |			},
          |			"msgCount": 1,
          |			"msg": [
          |				{
          |					"type": "abcdefghijklm",
          |					"text": "abcdefghijklm"
          |				}
          |			],
          |			"previousCalc": {
          |				"calcTimestamp": "4498-07-06T21:42:24.294Z",
          |				"calcID": "00000000",
          |				"calcAmount": 1000.25
          |			},
          |			"annualAllowances": {
          |				"personalAllowance": 99999999,
          |				"reducedPersonalAllowanceThreshold": 99999999,
          |				"reducedPersonalisedAllowance": 99999999
          |			}
          |		}
          |  }
          |}""".stripMargin
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
  }

}
