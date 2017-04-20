package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.domain.Nino

object DesJsons {

  object Errors {

    private def error(code: String, reason: String): String = {
      s"""
         |{
         |  "code": "$code",
         |  "reason": "$reason"
         |}
       """.stripMargin
    }

    val invalidNino: String = error("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
    val invalidPayload: String = error("INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
    val ninoNotFound: String = error("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found.")
    val notFound: String = error("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")
    val tradingNameConflict: String = error("CONFLICT", "Duplicated trading name.")
    val serverError: String = error("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")
    val serviceUnavailable: String = error("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
    val tooManySources: String = error("TOO_MANY_SOURCES", "You may only have a maximum of one self-employment source.")
    val invalidPeriod: String = error("INVALID_PERIOD", "The remote endpoint has indicated that a overlapping period was submitted.")
    val invalidObligation: String = error("INVALID_REQUEST", "Accounting period should be greater than 6 months.")
    val invalidOriginatorId: String = error("INVALID_ORIGINATOR_ID", "Submission has not passed validation. Invalid header Originator-Id.")
    val invalidCalcId: String = error("INVALID_CALCID", "Submission has not passed validation")
  }

  object SelfEmployment {
    def apply(nino: Nino,
              id: String = "123456789012345",
              accPeriodStart: String = "2017-04-06",
              accPeriodEnd: String = "2018-04-05",
              accountingType: String = "cash",
              commencementDate: String = "2017-01-01",
              cessationDate: Option[String] = Some("2017-01-02"),
              tradingName: String = "Acme Ltd",
              businessDescription: String = "Accountancy services",
              businessAddressLineOne: String = "1 Acme Rd.",
              businessAddressLineTwo: String = "London",
              businessAddressLineThree: String = "Greater London",
              businessAddressLineFour: String = "United Kingdom",
              businessPostcode: String = "A9 9AA"): String = {
      s"""
         |{
         |   "safeId": "XE00001234567890",
         |   "nino": "$nino",
         |   "mtdbsa": "123456789012345",
         |   "propertyIncome": false,
         |   "businessData": [
         |      {
         |         "incomeSourceId": "$id",
         |         "accountingPeriodStartDate": "$accPeriodStart",
         |         "accountingPeriodEndDate": "$accPeriodEnd",
         |         "tradingName": "$tradingName",
         |         "businessAddressDetails": {
         |            "addressLine1": "$businessAddressLineOne",
         |            "addressLine2": "$businessAddressLineTwo",
         |            "addressLine3": "$businessAddressLineThree",
         |            "addressLine4": "$businessAddressLineFour",
         |            "postalCode": "$businessPostcode",
         |            "countryCode": "GB"
         |         },
         |         "businessContactDetails": {
         |            "phoneNumber": "01332752856",
         |            "mobileNumber": "07782565326",
         |            "faxNumber": "01332754256",
         |            "emailAddress": "stephen@manncorpone.co.uk"
         |         },
         |         "tradingStartDate": "$commencementDate",
         |         "cashOrAccruals": "$accountingType",
         |         "seasonal": true
         |      }
         |   ]
         |}
         |
       """.stripMargin
    }

    def emptySelfEmployment(nino: Nino): String = {
      s"""
         |{
         |   "safeId": "XE00001234567890",
         |   "nino": "$nino",
         |   "mtdbsa": "123456789012345",
         |   "propertyIncome": false
         |}
       """.stripMargin
    }

    def createResponse(id: String): String = {
      s"""
         |{
         |  "safeId": "XA0001234567890",
         |  "mtditId": "mdtitId001",
         |  "incomeSources": [
         |    {
         |      "incomeSourceId": "$id"
         |    }
         |  ]
         |}
      """.stripMargin
    }

    object Period {
      def apply(id: String = "abc", from: String = "2017-04-05", to: String = "2018-04-04"): String = {
        s"""
           |{
           |   "id": "$id",
           |   "from": "$from",
           |   "to": "$to",
           |   "financials": {
           |      "incomes": {
           |         "turnover": 200.00,
           |         "other": 200.00
           |      },
           |      "deductions": {
           |         "costOfGoods": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "constructionIndustryScheme": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "staffCosts": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "travelCosts": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "premisesRunningCosts": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "maintenanceCosts": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "adminCosts": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "advertisingCosts": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "interest": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "financialCharges": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "badDebt": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "professionalFees": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "depreciation": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         },
           |         "other": {
           |            "amount": 200.00,
           |            "disallowableAmount": 200.00
           |         }
           |      }
           |   }
           |}
         """.stripMargin
      }

      def periods: String = {
        s"""
           |[
           |  ${apply(id = "abc", from = "2017-04-06", to = "2017-07-04")},
           |  ${apply(id = "def", from = "2017-07-05", to = "2017-08-04")}
           |]
         """.stripMargin
      }

      def createResponse(id: String = "123456789012345"): String = {
        s"""
           |{
           |   "transactionReference": "$id"
           |}
        """.stripMargin
      }
    }

    object AnnualSummary {
      def apply(): String = {
        s"""
           |{
           |   "annualAdjustments": {
           |      "includedNonTaxableProfits": 200.00,
           |      "basisAdjustment": 200.00,
           |      "overlapReliefUsed": 200.00,
           |      "accountingAdjustment": 200.00,
           |      "averagingAdjustment": 200.00,
           |      "lossBroughtForward": 200.00,
           |      "outstandingBusinessIncome": 200.00,
           |      "balancingChargeBpra": 200.00,
           |      "balancingChargeOther": 200.00,
           |      "goodsAndServicesOwnUse": 200.00
           |   },
           |   "annualAllowances": {
           |      "annualInvestmentAllowance": 200.00,
           |      "capitalAllowanceMainPool": 200.00,
           |      "capitalAllowanceSpecialRatePool": 200.00,
           |      "zeroEmissionGoodsVehicleAllowance": 200.00,
           |      "businessPremisesRenovationAllowance": 200.00,
           |      "enhanceCapitalAllowance": 200.00,
           |      "allowanceOnSales": 200.00
           |   },
           |   "annualNonFinancials": {
           |      "businessDetailsChangedRecently": 200.00,
           |      "payClass2Nics": 200.00,
           |      "exemptFromPayingClass2Nics": 200.00
           |   }
           |}
       """.stripMargin
      }
    }

  }

  object Obligations {
    def apply(id: String = "abc"): String = {
      s"""
         |{
         |  "obligations": [
         |    {
         |      "id": "$id",
         |      "type": "ITSB",
         |      "details": [
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondanceDueDate": "2017-08-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondanceDueDate": "2017-11-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondanceDueDate": "2018-02-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondanceDueDate": "2018-05-06",
         |          "periodKey": "004"
         |        }
         |      ]
         |    },
         |    {
         |      "id": "$id",
         |      "type": "ITSP",
         |      "details": [
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondanceDueDate": "2017-08-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondanceDueDate": "2017-11-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondanceDueDate": "2018-02-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondanceDueDate": "2018-05-06",
         |          "periodKey": "004"
         |        }
         |      ]
         |    }
         |  ]
         |}
         """.stripMargin
    }
  }

  object TaxCalculation {
    def apply(id: String = "abc"): String = {
      s"""
         |{
         |    "calcName": "abcdefghijklmnopqr",
         |    "calcVersion": "abcdef",
         |    "calcVersionDate": "2016-01-01",
         |    "calcID": "$id",
         |    "sourceName": "abcdefghijklmno",
         |    "sourceRef": "abcdefghijklmnopqrs",
         |    "identifier": "abcdefghijklm",
         |    "year": 2016,
         |    "periodFrom": "2016-01-01",
         |    "periodTo": "2016-01-01",
         |    "calcAmount": 200.00,
         |    "calcTimestamp": "4498-07-06T21:42:24.294Z",
         |    "calcResult": {
         |        "incomeTaxYTD": 200.00,
         |        "incomeTaxThisPeriod": 200.00,
         |        "calcDetail": {
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
         |        },
         |        "previousCalc": {
         |            "calcTimestamp": "4498-07-06T21:42:24.294Z",
         |            "calcID": "12345678",
         |            "calcAmount": 200.00
         |        }
         |    }
         |}
       """.stripMargin
    }

    def createResponse(id: String = "abc"): String = {
      s"""
         |{
         |  "id": "$id"
         |}
       """.stripMargin
    }
  }

}
