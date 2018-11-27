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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.{FHL, Other}

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
    val notAllowedConsolidatedExpenses = businessErrorWithMessage(
      "NOT_ALLOWED_CONSOLIDATED_EXPENSES" -> "The submission contains consolidated expenses but the accumulative turnover amount exceeds the threshold")
    val invalidTaxYear: String = businessErrorWithMessage(
      "TAX_YEAR_INVALID" -> "The provided tax year is not valid.")

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
      Json.obj("accountName" -> accountName)
    }

    def annualSummary(taxedUkInterest: Option[BigDecimal], untaxedUkInterest: Option[BigDecimal]): JsValue = {
      taxedUkInterest
        .fold[JsObject](Json.obj())(v => Json.obj("taxedUkInterest" -> v)) ++
      untaxedUkInterest
        .fold(Json.obj())(v => Json.obj("untaxedUkInterest" -> v))
    }
  }

  object Properties {
    def apply(): JsValue = Json.obj()

    def fhlPeriod(fromDate: Option[String] = None,
                  toDate: Option[String] = None,
                  rentIncome: BigDecimal = 0,
                  premisesRunningCosts: Option[BigDecimal] = None,
                  repairsAndMaintenance: Option[BigDecimal] = None,
                  financialCosts: Option[BigDecimal] = None,
                  professionalFees: Option[BigDecimal] = None,
                  costOfServices: Option[BigDecimal] = None,
                  otherCost: Option[BigDecimal] = None,
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

      Json.parse(s"""
           |{
           |  $from
           |  $to
           |  "incomes": {
           |    "rentIncome": { "amount": $rentIncome }
           |  },
           |  "expenses":
           |    ${fhlExpensesJson(premisesRunningCosts,
                                  repairsAndMaintenance,
                                  financialCosts,
                                  professionalFees,
                                  costOfServices,
                                  otherCost,
                                  consolidatedExpenses)}
           |}
       """.stripMargin)
    }

    def fhlExpensesJson(premisesRunningCosts: Option[BigDecimal] = None,
                        repairsAndMaintenance: Option[BigDecimal] = None,
                        financialCosts: Option[BigDecimal] = None,
                        professionalFees: Option[BigDecimal] = None,
                        costOfServices: Option[BigDecimal] = None,
                        otherCost: Option[BigDecimal] = None,
                        consolidatedExpenses: Option[BigDecimal] = None): String = {
      Json
        .toJson(
          FHL.Expenses(
            premisesRunningCosts.map(FHL.Expense(_)),
            repairsAndMaintenance.map(FHL.Expense(_)),
            financialCosts.map(FHL.Expense(_)),
            professionalFees.map(FHL.Expense(_)),
            costOfServices.map(FHL.Expense(_)),
            consolidatedExpenses.map(FHL.Expense(_)),
            otherCost.map(FHL.Expense(_))
          ))
        .toString
    }

    def otherPeriod(fromDate: Option[String] = None,
                    toDate: Option[String] = None,
                    rentIncome: BigDecimal = 0,
                    rentIncomeTaxDeducted: BigDecimal = 0,
                    premiumsOfLeaseGrant: Option[BigDecimal] = None,
                    reversePremiums: BigDecimal = 0,
                    otherPropertyIncome: Option[BigDecimal] = None,
                    premisesRunningCosts: Option[BigDecimal] = None,
                    repairsAndMaintenance: Option[BigDecimal] = None,
                    financialCosts: Option[BigDecimal] = None,
                    professionalFees: Option[BigDecimal] = None,
                    costOfServices: Option[BigDecimal] = None,
                    otherCost: Option[BigDecimal] = None,
                    residentialFinancialCost: Option[BigDecimal] = None,
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
           |  "expenses":
           |    ${otherExpensesJson(premisesRunningCosts,
                                    repairsAndMaintenance,
                                    financialCosts,
                                    professionalFees,
                                    costOfServices,
                                    otherCost,
                                    residentialFinancialCost,
                                    consolidatedExpenses)}
           |}
       """.stripMargin)
    }

    def otherExpensesJson(premisesRunningCosts: Option[BigDecimal] = None,
                          repairsAndMaintenance: Option[BigDecimal] = None,
                          financialCosts: Option[BigDecimal] = None,
                          professionalFees: Option[BigDecimal] = None,
                          costOfServices: Option[BigDecimal] = None,
                          otherCost: Option[BigDecimal] = None,
                          residentialFinancialCost: Option[BigDecimal] = None,
                          consolidatedExpenses: Option[BigDecimal] = None): String = {
      Json
        .toJson(
          Other.Expenses(
            premisesRunningCosts.map(Other.Expense(_)),
            repairsAndMaintenance.map(Other.Expense(_)),
            financialCosts.map(Other.Expense(_)),
            professionalFees.map(Other.Expense(_)),
            costOfServices.map(Other.Expense(_)),
            consolidatedExpenses.map(Other.Expense(_)),
            residentialFinancialCost.map(Other.Expense(_)),
            otherCost.map(Other.Expense(_))
          ))
        .toString
    }

    def consolidationPeriod(fromDate: Option[String] = None,
                            toDate: Option[String] = None,
                            rentIncome: BigDecimal = 0,
                            rentIncomeTaxDeducted: BigDecimal = 0,
                            premiumsOfLeaseGrant: Option[BigDecimal] = None,
                            reversePremiums: BigDecimal = 0,
                            otherPropertyIncome: Option[BigDecimal] = None,
                            premisesRunningCosts: Option[BigDecimal] = None,
                            repairsAndMaintenance: Option[BigDecimal] = None,
                            financialCosts: Option[BigDecimal] = None,
                            professionalFees: Option[BigDecimal] = None,
                            costOfServices: Option[BigDecimal] = None,
                            otherCost: Option[BigDecimal] = None,
                            residentialFinancialCost: Option[BigDecimal] = None,
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
               | "consolidatedExpenses": { "amount": $ce }
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
                    |    $ce
                    |  }
                    |}
       """.stripMargin)
    }

    def residentialPeriod(fromDate: Option[String] = None,
                          toDate: Option[String] = None,
                          rentIncome: BigDecimal = 0,
                          rentIncomeTaxDeducted: BigDecimal = 0,
                          premiumsOfLeaseGrant: Option[BigDecimal] = None,
                          reversePremiums: BigDecimal = 0,
                          otherPropertyIncome: Option[BigDecimal] = None,
                          premisesRunningCosts: Option[BigDecimal] = None,
                          repairsAndMaintenance: Option[BigDecimal] = None,
                          financialCosts: Option[BigDecimal] = None,
                          professionalFees: Option[BigDecimal] = None,
                          costOfServices: Option[BigDecimal] = None,
                          otherCost: Option[BigDecimal] = None,
                          residentialFinancialCost: Option[BigDecimal] = None,
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

      val rfc =
        residentialFinancialCost
          .map { rfc =>
            s"""
               | "residentialFinancialCost": { "amount": $rfc }
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
                    |    $rfc
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
                         balancingCharge: BigDecimal = 0.0,
                         periodOfGraceAdjustment: Boolean = false): JsValue = {
      Json.parse(s"""
           |{
           |  "allowances": {
           |    "annualInvestmentAllowance": $annualInvestmentAllowance,
           |    "otherCapitalAllowance": $otherCapitalAllowance
           |  },
           |  "adjustments": {
           |   "lossBroughtForward": $lossBroughtForward,
           |   "privateUseAdjustment": $privateUseAdjustment,
           |   "balancingCharge": $balancingCharge,
           |   "periodOfGraceAdjustment": $periodOfGraceAdjustment
           |  }
           |}
    """.stripMargin)
    }

    val invalidFhlAnnualSummary: JsValue = {
      Json.parse(
        """
          |{
          | "adjustments" : {
          |   "periodOfGraceAdjustment" : "not a boolean"
          | }
          |}
        """.stripMargin
      )
    }

    def otherAnnualSummary(annualInvestmentAllowance: BigDecimal = 0.0,
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
}
