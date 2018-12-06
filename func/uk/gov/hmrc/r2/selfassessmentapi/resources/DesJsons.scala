package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.models.des.properties.{Common, FHL, Other}
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.{CessationReason, TaxYear}

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

    private def multiError(codeReason: (String, String)*): String = {
      val errors = codeReason map {
        case (code, reason) =>
          JsObject(Seq("code" -> JsString(code), "reason" -> JsString(reason)))
      }
      Json
        .obj("failures" -> errors)
        .toString()
    }

    val multipleErrors: String = multiError(("INVALID_IDVALUE", "Submission has not passed validation. Invalid parameter idValue."),
      ("INVALID_TAXYEAR", "Submission has not passed validation. Invalid parameter taxYear."))
    val invalidIdType: String = error("INVALID_IDTYPE", "Submission has not passed validation. Invalid parameter idType.")
    val invalidIdValue: String = error("INVALID_IDVALUE", "Submission has not passed validation. Invalid parameter idValue.")
    val invalidTaxYear: String = error("INVALID_TAXYEAR", "Submission has not passed validation. Invalid parameter taxYear.")
    val invalidNino: String = error("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
    val invalidPayload: String = error("INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
    val invalidRequest: String = error("INVALID_REQUEST", "does not matter")
    val ninoNotFound: String = error("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found.")
    val notFoundProperty: String = error(
      "NOT_FOUND_PROPERTY",
      "The remote endpoint has indicated that no data can be found for the given property type.")
    val notFoundPeriod: String = error(
      "NOT_FOUND_PERIOD",
      "The remote endpoint has indicated that no data can be found for the given period.")
    val notFound: String = error("NOT_FOUND", "DES     The remote endpoint has indicated that no data can be found.")
    val tradingNameConflict: String = error("CONFLICT", "Duplicated trading name.")
    val serverError: String =
      error("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")
    val serviceUnavailable: String = error("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
    val tooManySources: String =
      error("TOO_MANY_SOURCES", "You may only have a maximum of one self-employment source.")
    val invalidPeriod: String =
      error("INVALID_PERIOD", "The remote endpoint has indicated that a overlapping period was submitted.")
    val invalidCreatePeriod: String = error("INVALID_PERIOD", "The remote endpoint has indicated that the submission period already exists.")
    val overlappingPeriod: String =
      error("OVERLAPS_IN_PERIOD",
        "The remote endpoint has indicated that the submission period overlaps another period submitted.")
    val nonContiguousPeriod: String =
      error(
        "NOT_CONTIGUOUS_PERIOD",
        "The remote endpoint has indicated that the submission period is not contiguous with another period submission.")
    val misalignedPeriod: String =
      error("NOT_ALIGN_PERIOD",
        "The remote endpoint has indicated that the submission period is outside the Accounting Period.")
    val invalidObligation: String = error("INVALID_REQUEST", "Accounting period should be greater than 6 months.")
    val invalidBusinessId: String = error("INVALID_BUSINESSID", "Submission has not passed validation. Invalid parameter businessId.")
    val invalidOriginatorId: String =
      error("INVALID_ORIGINATOR_ID", "Submission has not passed validation. Invalid header Originator-Id.")
    val invalidCalcId: String = error("INVALID_CALCID", "Submission has not passed validation. Invalid parameter calcId.")
    val propertyConflict: String = error("CONFLICT", "Property already exists.")
    val invalidIncomeSource: String = error(
      "INVALID_INCOME_SOURCE",
      "The remote endpoint has indicated that the taxpayer does not have an associated property.")
    val bothExpensesSupplied: String = error(
      "BOTH_EXPENSES_SUPPLIED",
      "The remote endpoint has indicated that the submission contains both simplified and a full breakdown of expenses.")
    val notFoundIncomeSource: String = error("NOT_FOUND_INCOME_SOURCE", "The remote endpoint has indicated that no data can be found for the given income source id.")
    val invalidDateFrom: String =
      error("INVALID_DATE_FROM", "Submission has not passed validation. Invalid parameter from.")
    val invalidDateTo: String = error("INVALID_DATE_TO", "Submission has not passed validation. Invalid parameter to.")
    val periodicUpdateMissing: String = error("PERIODIC_UPDATE_MISSING", "Cannot finalise statement with missing periodic update")
    val earlySubmission = error("EARLY_SUBMISSION", "End-of-period statement cannot be submitted early.")
    val lateSubmission = error("LATE_SUBMISSION", "End-of-period statement cannot be submitted for this period later than 31 January 20XX.")
    val nonMatchingPeriod = error("NON_MATCHING_PERIOD", "Statement period does not match you accounting period.")
    val requiredEndOfPeriodStatement = error("INVALID_REQUEST", "The remote endpoint has indicated that it is an Invalid Request")
    val invalidTaxCalculationId = error("INVALID_TAX_CALCULATION_ID", "The remote endpoint has indicated that the calculation id does not match the calculation id returned by the latest intent to crystallise")
    val requiredIntentToCrystallise = error("REQUIRED_INTENT_TO_CRYSTALLISE", "The remote endpoint has indicated that the Crystallisation could occur only after an intent to crystallise is sent")
    val alreadySubmitted = error("ALREADY_SUBMITTED", "You cannot submit a statement for the same accounting period twice.")
    val notAllowedConsolidatedExpenses = error("NOT_ALLOWED_SIMPLIFIED_EXPENSES", "The remote endpoint has indicated that the submission contains simplified expenses but the accumulative turnover amount exceeds the threshold.")
  }

  object SelfEmployment {
    def apply(nino: Nino,
              mtdId: String,
              id: String = "123456789012345",
              accPeriodStart: String = "2017-04-06",
              accPeriodEnd: String = "2018-04-05",
              accountingType: String = "cash",
              commencementDate: String = "2017-01-01",
              cessationDate: String = "2017-01-02",
              cessationReason: String = CessationReason.Bankruptcy.toString,
              tradingName: String = "Acme Ltd",
              description: String = "Accountancy services",
              addressLineOne: String = "1 Acme Rd.",
              addressLineTwo: String = "London",
              addressLineThree: String = "Greater London",
              addressLineFour: String = "United Kingdom",
              postalCode: String = "A9 9AA",
              countryCode: String = "GB"): String = {
      s"""
         |{
         |   "safeId": "XE00001234567890",
         |   "nino": "$nino",
         |   "mtdbsa": "$mtdId",
         |   "propertyIncome": false,
         |   "businessData": [
         |      {
         |         "incomeSourceId": "$id",
         |         "accountingPeriodStartDate": "$accPeriodStart",
         |         "accountingPeriodEndDate": "$accPeriodEnd",
         |         "tradingName": "$tradingName",
         |         "businessAddressDetails": {
         |            "addressLine1": "$addressLineOne",
         |            "addressLine2": "$addressLineTwo",
         |            "addressLine3": "$addressLineThree",
         |            "addressLine4": "$addressLineFour",
         |            "postalCode": "$postalCode",
         |            "countryCode": "$countryCode"
         |         },
         |         "businessContactDetails": {
         |            "phoneNumber": "01332752856",
         |            "mobileNumber": "07782565326",
         |            "faxNumber": "01332754256",
         |            "emailAddress": "stephen@manncorpone.co.uk"
         |         },
         |         "tradingStartDate": "$commencementDate",
         |         "cashOrAccruals": "$accountingType",
         |         "seasonal": true,
         |         "cessationDate": "$cessationDate",
         |         "cessationReason": "$cessationReason"
         |      }
         |   ]
         |}
         |
       """.stripMargin
    }

    def emptySelfEmployment(nino: Nino, mtdId: String): String = {
      s"""
         |{
         |   "safeId": "XE00001234567890",
         |   "nino": "$nino",
         |   "mtdbsa": "$mtdId",
         |   "propertyIncome": false
         |}
       """.stripMargin
    }

    def createResponse(id: String, mtdId: String): String = {
      s"""
         |{
         |  "safeId": "XA0001234567890",
         |  "mtdsba": "$mtdId",
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
           |         "businessEntertainmentCosts": {
           |           "amount": 200.00,
           |           "disallowableAmount": 200.00
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
           |{
           |  "periods": [
           |      {
           |          "from": "2017-04-06",
           |          "to": "2017-07-04",
           |          "transactionReference": "abc"
           |      },
           |      {
           |          "from": "2017-07-05",
           |          "to": "2017-08-04",
           |          "transactionReference": "def"
           |      }
           |   ]
           |}
         """.stripMargin
      }

      def emptyPeriods: String = {
        s"""
           |{
           |  "periods": []
           |}
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
           |      "goodsAndServicesOwnUse": 200.00,
           |      "overlapProfitCarriedForward": 500.25,
           |      "overlapProfitBroughtForward": 500.25,
           |      "lossCarriedForwardTotal": 500.25,
           |      "cisDeductionsTotal": 500.25,
           |      "taxDeductionsFromTradingIncome": 500.25,
           |      "class4NicProfitAdjustment": 500.25
           |   },
           |   "annualAllowances": {
           |      "annualInvestmentAllowance": 200.00,
           |      "businessPremisesRenovationAllowance": 200.00,
           |      "capitalAllowanceMainPool": 200.00,
           |      "capitalAllowanceSpecialRatePool": 200.00,
           |      "zeroEmissionGoodsVehicleAllowance": 200.00,
           |      "enhanceCapitalAllowance": 200.00,
           |      "allowanceOnSales": 200.00,
           |      "capitalAllowanceSingleAssetPool": 500.25
           |   },
           |   "annualNonFinancials": {
           |      "businessDetailsChangedRecently": true,
           |      "payClass2Nics": false,
           |      "exemptFromPayingClass4Nics": true,
           |      "class4NicsExemptionReason": "003"
           |   }
           |}
       """.stripMargin
      }

      val response: String = {
        s"""
           |{
           |  "transactionReference": "abc"
           |}
         """.stripMargin
      }
    }

  }

  object Properties {
    object AnnualSummary {
      def other: String = {
        s"""
         {
           |   "annualAdjustments": {
           |      "lossBroughtForward": 0.0,
           |      "balancingCharge": 0.0,
           |      "privateUseAdjustment": 0.0,
           |      "businessPremisesRenovationAllowanceBalancingCharges" : 0.0,
           |      "nonResidentLandlord": false,
           |      "ukOtherRentARoom" : {
           |      "jointlyLet": false
           |      }
           |   },
           |   "annualAllowances": {
           |      "annualInvestmentAllowance": 0.0,
           |      "otherCapitalAllowance": 0.0,
           |      "zeroEmissionGoodsVehicleAllowance": 0.0,
           |      "businessPremisesRenovationAllowance": 0.0,
           |      "costOfReplacingDomGoods": 0.0,
           |      "businessPremisesRenovationAllowance": 0.0,
           |      "propertyIncomeAllowance": 0.0
           |   }
           |}
      """.stripMargin
      }

      def fhl: String = {
        s"""
         {
           |   "annualAdjustments": {
           |      "lossBroughtForward": 0.0,
           |      "balancingCharge": 0.0,
           |      "privateUseAdjustment": 0.0,
           |      "periodOfGraceAdjustment": false
           |   },
           |   "annualAllowances": {
           |      "annualInvestmentAllowance": 0.0,
           |      "otherCapitalAllowance": 0.0
           |   }
           |}
      """.stripMargin
      }

      val response: String =
        s"""
           |{
           |  "transactionReference": "abc"
           |}
       """.stripMargin
    }

    def createResponse: String = {
      s"""
         |{
         |  "safeId": "XA0001234567890",
         |  "mtditId": "mdtitId001",
         |  "incomeSource":
         |    {
         |      "incomeSourceId": "1234567"
         |    }
         |}
      """.stripMargin
    }

    def retrieveProperty: String = {
      s"""
         {
         |   "safeId": "XE00001234567890",
         |   "nino": "AA123456A",
         |   "mtdbsa": "123456789012345",
         |   "propertyIncome": false,
         |   "propertyData": {
         |      "incomeSourceId": "123456789012345",
         |      "accountingPeriodStartDate": "2001-01-01",
         |      "accountingPeriodEndDate": "2010-01-01"
         |    }
         |}
      """.stripMargin
    }

    def retrieveNoProperty: String = {
      s"""
         {
         |   "safeId": "XE00001234567890",
         |   "nino": "AA123456A",
         |   "mtdbsa": "123456789012345",
         |   "propertyIncome": false
         |}
      """.stripMargin
    }

    object Period {
      def createResponse(id: String = "123456789012345"): String = {
        s"""
           |{
           |   "transactionReference": "$id"
           |}
        """.stripMargin
      }

      def periodsSummary: String =
        s"""
           |{
           |  "periods": [
           |    {
           |      "transactionReference": "abc",
           |      "from": "2017-04-06",
           |      "to": "2017-07-04"
           |    },
           |    {
           |      "transactionReference": "def",
           |      "from": "2017-07-05",
           |      "to": "2017-08-04"
           |    }
           |  ]
           |}
         """.stripMargin

      def fhl(transactionReference: String = "12345",
              from: String = "",
              to: String = "",
              rentIncome: BigDecimal = 0,
              rarRentReceived: BigDecimal = 0,
              rentIncomeTaxDeducted: Option[BigDecimal] = Some(0),
              premisesRunningCosts: BigDecimal = 0,
              repairsAndMaintenance: BigDecimal = 0,
              financialCosts: BigDecimal = 0,
              professionalFees: BigDecimal = 0,
              costOfServices: BigDecimal = 0,
              consolidatedExpenses: BigDecimal = 0,
              other: BigDecimal = 0): JsValue =
        Json.toJson(
          FHL.Properties(
            transactionReference = Some(transactionReference),
            from = from,
            to = to,
            financials = Some(
              FHL
                .Financials(
                  incomes = Some(FHL.Incomes(rentIncome = Some(Common.Income(rentIncome, rentIncomeTaxDeducted)),
                    rarRentReceived = Some(Common.Income(rarRentReceived)))),
                  deductions = Some(FHL.Deductions(
                    premisesRunningCosts = Some(premisesRunningCosts),
                    repairsAndMaintenance = Some(repairsAndMaintenance),
                    financialCosts = Some(financialCosts),
                    professionalFees = Some(professionalFees),
                    costOfServices = Some(costOfServices),
                    consolidatedExpenses = Some(consolidatedExpenses),
                    other = Some(other)
                  ))
                ))
          ))

      def other(transactionReference: String = "12345",
                from: String = "",
                to: String = "",
                rentIncome: BigDecimal = 0,
                rentIncomeTaxDeducted: Option[BigDecimal] = Some(0),
                premiumsOfLeaseGrant: Option[BigDecimal] = Some(0),
                reversePremiums: Option[BigDecimal] = Some(0),
                otherPropertyIncome: Option[BigDecimal] = Some(0),
                premisesRunningCosts: Option[BigDecimal] = Some(0),
                repairsAndMaintenance: Option[BigDecimal] = Some(0),
                financialCosts: Option[BigDecimal] = Some(0),
                professionalFees: Option[BigDecimal] = Some(0),
                costOfServices: Option[BigDecimal] = Some(0),
                residentialFinancialCost: Option[BigDecimal] = Some(0),
                consolidatedExpenses: Option[BigDecimal] = Some(0),
                other: Option[BigDecimal] = Some(0)): JsValue =
        Json.toJson(
          Other
            .Properties(
              transactionReference = Some(transactionReference),
              from = from,
              to = to,
              financials = Some(Other.Financials(
                incomes = Some(Other.Incomes(rentIncome = Some(Common.Income(rentIncome, rentIncomeTaxDeducted)),
                  premiumsOfLeaseGrant = premiumsOfLeaseGrant,
                  reversePremiums = reversePremiums,
                  otherIncome = otherPropertyIncome)),
                deductions = Some(Other.Deductions(
                  premisesRunningCosts = premisesRunningCosts,
                  repairsAndMaintenance = repairsAndMaintenance,
                  financialCosts = financialCosts,
                  professionalFees = professionalFees,
                  costOfServices = costOfServices,
                  residentialFinancialCost = residentialFinancialCost,
                  consolidatedExpenses = consolidatedExpenses,
                  other = other
                ))
              ))
            ))

      def periods(propertyType: PropertyType): String =
        propertyType match {
          case PropertyType.FHL =>
            Json
              .arr(fhl(transactionReference = "abc", from = "2017-04-06", to = "2017-07-04"),
                fhl(transactionReference = "def", from = "2017-07-05", to = "2017-08-04"))
              .toString()
          case PropertyType.OTHER =>
            Json
              .arr(other(transactionReference = "abc", from = "2017-04-06", to = "2017-07-04"),
                other(transactionReference = "def", from = "2017-07-05", to = "2017-08-04"))
              .toString()
        }
    }
  }

  object Obligations {
    def apply(id: String = "abc"): String = {
      s"""
         |{
         |  "obligations": [
         |    {
         |      "identification": {
         |        "incomeSourceType":"ITSA",
         |        "referenceNumber": "$id",
         |        "referenceType":"NINO"
         |    },
         |     "obligationDetails": [
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondenceDueDate": "2017-08-05"
         |        },
         |        {
         |          "status": "F",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondenceDueDate": "2018-02-05",
         |          "inboundCorrespondenceDateReceived": "2018-02-01"
         |        },
         |        {
         |          "status": "F",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondenceDueDate": "2017-11-05",
         |          "inboundCorrespondenceDateReceived": "2017-11-01"
         |        },
         |        {
         |          "status": "F",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondenceDueDate": "2018-05-06",
         |          "inboundCorrespondenceDateReceived": "2018-05-01"
         |        }
         |      ]
         |    },
         |    {
         |      "identification": {
         |        "incomeSourceType":"ITSB",
         |        "referenceNumber": "$id",
         |        "referenceType":"MTDBIS"
         |    },
         |     "obligationDetails": [
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondenceDueDate": "2017-08-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondenceDueDate": "2017-11-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondenceDueDate": "2018-02-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondenceDueDate": "2018-05-06",
         |          "periodKey": "004"
         |        }
         |      ]
         |    },
         |    {
         |      "identification": {
         |        "incomeSourceType":"ITSP",
         |        "referenceNumber": "$id",
         |        "referenceType":"MTDBIS"
         |    },
         |     "obligationDetails": [
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondenceDueDate": "2017-08-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondenceDueDate": "2017-11-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondenceDueDate": "2018-02-05",
         |          "periodKey": "004"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondenceDueDate": "2018-05-06",
         |          "periodKey": "004"
         |        }
         |      ]
         |    }
         |  ]
         |}
         """.stripMargin
    }

    def crystallisationObligations(id: String, taxYear: TaxYear): String =
      s"""
         |{
         |    "obligations": [
         |        {
         |            "identification": {
         |                "incomeSourceType": "ITSA",
         |                "referenceNumber": "$id",
         |                "referenceType": "NINO"
         |            },
         |            "obligationDetails": [
         |                {
         |                    "status": "F",
         |                    "inboundCorrespondenceFromDate": "2016-04-06",
         |                    "inboundCorrespondenceToDate": "2017-04-05",
         |                    "inboundCorrespondenceDueDate": "2018-01-31",
         |                    "periodKey": "16P0",
         |                    "inboundCorrespondenceDateReceived": "2018-01-01"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "${taxYear.taxYearFromDate}",
         |                    "inboundCorrespondenceToDate": "${taxYear.taxYearToDate}",
         |                    "inboundCorrespondenceDueDate": "2019-01-31",
         |                    "periodKey": "17P0"
         |                }
         |            ]
         |        },
         |        {
         |            "identification": {
         |                "incomeSourceType": "ITSB",
         |                "referenceNumber": "XDIS00000000166",
         |                "referenceType": "MTDBIS"
         |            },
         |            "obligationDetails": [
         |                {
         |                    "status": "F",
         |                    "inboundCorrespondenceFromDate": "2017-04-06",
         |                    "inboundCorrespondenceToDate": "2017-07-05",
         |                    "inboundCorrespondenceDueDate": "2017-08-05",
         |                    "periodKey": "#001",
         |                    "inboundCorrespondenceDateReceived": "2017-06-30"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2017-07-06",
         |                    "inboundCorrespondenceToDate": "2017-10-05",
         |                    "inboundCorrespondenceDueDate": "2017-11-05",
         |                    "periodKey": "#002"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2017-10-06",
         |                    "inboundCorrespondenceToDate": "2018-01-05",
         |                    "inboundCorrespondenceDueDate": "2018-02-05",
         |                    "periodKey": "#003"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2018-01-06",
         |                    "inboundCorrespondenceToDate": "2018-04-05",
         |                    "inboundCorrespondenceDueDate": "2018-05-05",
         |                    "periodKey": "#004"
         |                }
         |            ]
         |        },
         |        {
         |            "identification": {
         |                "incomeSourceType": "ITSP",
         |                "referenceNumber": "XGIS00000000169",
         |                "referenceType": "MTDBIS"
         |            },
         |            "obligationDetails": [
         |                {
         |                    "status": "F",
         |                    "inboundCorrespondenceFromDate": "2017-04-06",
         |                    "inboundCorrespondenceToDate": "2017-07-05",
         |                    "inboundCorrespondenceDueDate": "2017-08-05",
         |                    "periodKey": "#001",
         |                    "inboundCorrespondenceDateReceived": "2017-06-30"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2017-07-06",
         |                    "inboundCorrespondenceToDate": "2017-10-05",
         |                    "inboundCorrespondenceDueDate": "2017-11-05",
         |                    "periodKey": "#002"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2017-10-06",
         |                    "inboundCorrespondenceToDate": "2018-01-05",
         |                    "inboundCorrespondenceDueDate": "2018-02-05",
         |                    "periodKey": "#003"
         |                },
         |                {
         |                    "status": "O",
         |                    "inboundCorrespondenceFromDate": "2018-01-06",
         |                    "inboundCorrespondenceToDate": "2018-04-05",
         |                    "inboundCorrespondenceDueDate": "2018-05-05",
         |                    "periodKey": "#004"
         |                }
         |            ]
         |        }
         |    ]
         |}
       """.stripMargin

    val eopsObligations: String => String = id => {
      s"""
         |{
         |  "obligations": [
         |    {
         |      "identification": {
         |        "incomeSourceType":"ITSB",
         |        "referenceNumber": "$id",
         |        "referenceType":"MTDBIS"
         |    },
         |     "obligationDetails": [
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondenceDueDate": "2018-05-06",
         |          "periodKey": "EOPS"
         |        }
         |      ]
         |    }
         |  ]
         |}""".stripMargin

    }

    val obligationsNoIdentification: String = s"""{
         |  "obligations" : [ {
         |    "obligationDetails" : [ {
         |      "status" : "O",
         |      "inboundCorrespondenceFromDate" : "2017-04-06",
         |      "inboundCorrespondenceToDate" : "2017-07-05",
         |      "inboundCorrespondenceDueDate" : "2017-08-12",
         |      "periodKey" : "EOPS"
         |    } ]
         |  } ]
         |}""".stripMargin


  }

  object Crystallisation {
    def intentToCrystallise(): String = {
      s"""
         |{
         |  "id": "77427777"
         |}
       """.stripMargin
    }
  }

  object TaxCalculation {
    def apply(id: String = "abc"): String = {
      s"""
         |{
         |   "calcName":"abcdefghijklmnopqr",
         |   "calcVersion":"abcdef",
         |   "calcVersionDate":"2016-01-01",
         |   "calcID":"$id",
         |   "sourceName":"abcdefghijklmno",
         |   "sourceRef":"abcdefghijklmnopqrs",
         |   "identifier":"abcdefghijklm",
         |   "year":2016,
         |   "periodFrom":"2016-01-01",
         |   "periodTo":"2016-01-01",
         |   "calcAmount":200.00,
         |   "calcTimestamp":"4498-07-06T21:42:24.294Z",
         |   "calcResult":{
         |      "incomeTaxYTD":1000.25,
         |      "incomeTaxThisPeriod":1000.25,
         |      "calcDetail":{
         |         "payFromAllEmployments":200.22,
         |         "benefitsAndExpensesReceived":200.22,
         |         "allowableExpenses":200.22,
         |         "payFromAllEmploymentsAfterExpenses":200.22,
         |         "shareSchemes":200.22,
         |         "profitFromSelfEmployment":200.22,
         |         "profitFromPartnerships":200.22,
         |         "profitFromUkLandAndProperty":200.22,
         |         "dividendsFromForeignCompanies":200.22,
         |         "foreignIncome":200.22,
         |         "trustsAndEstates":200.22,
         |         "interestReceivedFromUkBanksAndBuildingSocieties":200.22,
         |         "dividendsFromUkCompanies":200.22,
         |         "ukPensionsAndStateBenefits":200.22,
         |         "gainsOnLifeInsurance":200.22,
         |         "otherIncome":200.22,
         |         "totalIncomeReceived":200.22,
         |         "paymentsIntoARetirementAnnuity":200.22,
         |         "foreignTaxOnEstates":200.22,
         |         "incomeTaxRelief":200.22,
         |         "incomeTaxReliefReducedToMaximumAllowable":200.22,
         |         "annuities":200.22,
         |         "giftOfInvestmentsAndPropertyToCharity":200.22,
         |         "personalAllowance":200,
         |         "marriageAllowanceTransfer":200.22,
         |         "blindPersonAllowance":200.22,
         |         "blindPersonSurplusAllowanceFromSpouse":200.22,
         |         "incomeExcluded":200.22,
         |         "totalIncomeAllowancesUsed":200.22,
         |         "totalIncomeOnWhichTaxIsDue":200.22,
         |         "payPensionsExtender":200.22,
         |         "giftExtender":200.22,
         |         "extendedBR":200.22,
         |         "payPensionsProfitAtBRT":200.22,
         |         "incomeTaxOnPayPensionsProfitAtBRT":200.22,
         |         "payPensionsProfitAtHRT":200.22,
         |         "incomeTaxOnPayPensionsProfitAtHRT":200.22,
         |         "payPensionsProfitAtART":200.22,
         |         "incomeTaxOnPayPensionsProfitAtART":200.22,
         |         "netPropertyFinanceCosts":200.22,
         |         "interestReceivedAtStartingRate":200.22,
         |         "incomeTaxOnInterestReceivedAtStartingRate":200.22,
         |         "interestReceivedAtZeroRate":200.22,
         |         "incomeTaxOnInterestReceivedAtZeroRate":200.22,
         |         "interestReceivedAtBRT":200.22,
         |         "incomeTaxOnInterestReceivedAtBRT":200.22,
         |         "interestReceivedAtHRT":200.22,
         |         "incomeTaxOnInterestReceivedAtHRT":200.22,
         |         "interestReceivedAtART":200.22,
         |         "incomeTaxOnInterestReceivedAtART":200.22,
         |         "dividendsAtZeroRate":200.22,
         |         "incomeTaxOnDividendsAtZeroRate":200.22,
         |         "dividendsAtBRT":200.22,
         |         "incomeTaxOnDividendsAtBRT":200.22,
         |         "dividendsAtHRT":200.22,
         |         "incomeTaxOnDividendsAtHRT":200.22,
         |         "dividendsAtART":200.22,
         |         "incomeTaxOnDividendsAtART":200.22,
         |         "totalIncomeOnWhichTaxHasBeenCharged":200.22,
         |         "taxOnOtherIncome":200.22,
         |         "incomeTaxDue":200.22,
         |         "incomeTaxCharged":200.22,
         |         "deficiencyRelief":200.22,
         |         "topSlicingRelief":200.22,
         |         "ventureCapitalTrustRelief":200.22,
         |         "enterpriseInvestmentSchemeRelief":200.22,
         |         "seedEnterpriseInvestmentSchemeRelief":200.22,
         |         "communityInvestmentTaxRelief":200.22,
         |         "socialInvestmentTaxRelief":200.22,
         |         "maintenanceAndAlimonyPaid":200.22,
         |         "marriedCouplesAllowance":200.22,
         |         "marriedCouplesAllowanceRelief":200.22,
         |         "surplusMarriedCouplesAllowance":200.22,
         |         "surplusMarriedCouplesAllowanceRelief":200.22,
         |         "notionalTaxFromLifePolicies":200.22,
         |         "notionalTaxFromDividendsAndOtherIncome":200.22,
         |         "foreignTaxCreditRelief":200.22,
         |         "incomeTaxDueAfterAllowancesAndReliefs":200.22,
         |         "giftAidPaymentsAmount":200.22,
         |         "giftAidTaxDue":200.22,
         |         "capitalGainsTaxDue":200.22,
         |         "remittanceForNonDomiciles":200.22,
         |         "highIncomeChildBenefitCharge":200.22,
         |         "totalGiftAidTaxReduced":200.22,
         |         "incomeTaxDueAfterGiftAidReduction":200.22,
         |         "annuityAmount":200.22,
         |         "taxDueOnAnnuity":200.22,
         |         "taxCreditsOnDividendsFromUkCompanies":200.22,
         |         "incomeTaxDueAfterDividendTaxCredits":200.22,
         |         "nationalInsuranceContributionAmount":200.22,
         |         "nationalInsuranceContributionCharge":200.22,
         |         "nationalInsuranceContributionSupAmount":200.22,
         |         "nationalInsuranceContributionSupCharge":200.22,
         |         "totalClass4Charge":200.22,
         |         "nationalInsuranceClass1Amount":200.22,
         |         "nationalInsuranceClass2Amount":200.22,
         |         "nicTotal":200.22,
         |         "underpaidTaxForPreviousYears":200.22,
         |         "studentLoanRepayments":200.22,
         |         "pensionChargesGross":200.22,
         |         "pensionChargesTaxPaid":200.22,
         |         "totalPensionSavingCharges":200.22,
         |         "pensionLumpSumAmount":200.22,
         |         "pensionLumpSumRate":200.22,
         |         "statePensionLumpSumAmount":200.22,
         |         "remittanceBasisChargeForNonDomiciles":200.22,
         |         "additionalTaxDueOnPensions":200.22,
         |         "additionalTaxReliefDueOnPensions":200.22,
         |         "incomeTaxDueAfterPensionDeductions":200.22,
         |         "employmentsPensionsAndBenefits":200.22,
         |         "outstandingDebtCollectedThroughPaye":200.22,
         |         "payeTaxBalance":200.22,
         |         "cisAndTradingIncome":200.22,
         |         "partnerships":200.22,
         |         "ukLandAndPropertyTaxPaid":200.22,
         |         "foreignIncomeTaxPaid":200.22,
         |         "trustAndEstatesTaxPaid":200.22,
         |         "overseasIncomeTaxPaid":200.22,
         |         "interestReceivedTaxPaid":200.22,
         |         "voidISAs":200.22,
         |         "otherIncomeTaxPaid":200.22,
         |         "underpaidTaxForPriorYear":200.22,
         |         "totalTaxDeducted":200.22,
         |         "incomeTaxOverpaid":200.22,
         |         "incomeTaxDueAfterDeductions":200.22,
         |         "propertyFinanceTaxDeduction":200.22,
         |         "taxableCapitalGains":200.22,
         |         "capitalGainAtEntrepreneurRate":200.22,
         |         "incomeTaxOnCapitalGainAtEntrepreneurRate":200.22,
         |         "capitalGrainsAtLowerRate":200.22,
         |         "incomeTaxOnCapitalGainAtLowerRate":200.22,
         |         "capitalGainAtHigherRate":200.22,
         |         "incomeTaxOnCapitalGainAtHigherTax":200.22,
         |         "capitalGainsTaxAdjustment":200.22,
         |         "foreignTaxCreditReliefOnCapitalGains":200.22,
         |         "liabilityFromOffShoreTrusts":200.22,
         |         "taxOnGainsAlreadyCharged":200.22,
         |         "totalCapitalGainsTax":200.22,
         |         "incomeAndCapitalGainsTaxDue":200.22,
         |         "taxRefundedInYear":200.22,
         |         "unpaidTaxCalculatedForEarlierYears":200.22,
         |         "marriageAllowanceTransferAmount":200.22,
         |         "marriageAllowanceTransferRelief":200.22,
         |         "marriageAllowanceTransferMaximumAllowable":200.22,
         |         "nationalRegime":"abc",
         |         "allowance":200,
         |         "limitBRT":200,
         |         "limitHRT":200,
         |         "rateBRT":20.00,
         |         "rateHRT":40.00,
         |         "rateART":50.00,
         |         "limitAIA":200,
         |         "allowanceBRT":200,
         |         "interestAllowanceHRT":200,
         |         "interestAllowanceBRT":200,
         |         "dividendAllowance":200,
         |         "dividendBRT":20.00,
         |         "dividendHRT":40.00,
         |         "dividendART":50.00,
         |         "class2NICsLimit":200,
         |         "class2NICsPerWeek":200.22,
         |         "class4NICsLimitBR":200,
         |         "class4NICsLimitHR":200,
         |         "class4NICsBRT":20.00,
         |         "class4NICsHRT":40.00,
         |         "proportionAllowance":200,
         |         "proportionLimitBRT":200,
         |         "proportionLimitHRT":200,
         |         "proportionalTaxDue":200.22,
         |         "proportionInterestAllowanceBRT":200,
         |         "proportionInterestAllowanceHRT":200,
         |         "proportionDividendAllowance":200,
         |         "proportionPayPensionsProfitAtART":200,
         |         "proportionIncomeTaxOnPayPensionsProfitAtART":200,
         |         "proportionPayPensionsProfitAtBRT":200,
         |         "proportionIncomeTaxOnPayPensionsProfitAtBRT":200,
         |         "proportionPayPensionsProfitAtHRT":200,
         |         "proportionIncomeTaxOnPayPensionsProfitAtHRT":200,
         |         "proportionInterestReceivedAtZeroRate":200,
         |         "proportionIncomeTaxOnInterestReceivedAtZeroRate":200,
         |         "proportionInterestReceivedAtBRT":200,
         |         "proportionIncomeTaxOnInterestReceivedAtBRT":200,
         |         "proportionInterestReceivedAtHRT":200,
         |         "proportionIncomeTaxOnInterestReceivedAtHRT":200,
         |         "proportionInterestReceivedAtART":200,
         |         "proportionIncomeTaxOnInterestReceivedAtART":200,
         |         "proportionDividendsAtZeroRate":200,
         |         "proportionIncomeTaxOnDividendsAtZeroRate":200,
         |         "proportionDividendsAtBRT":200,
         |         "proportionIncomeTaxOnDividendsAtBRT":200,
         |         "proportionDividendsAtHRT":200,
         |         "proportionIncomeTaxOnDividendsAtHRT":200,
         |         "proportionDividendsAtART":200,
         |         "proportionIncomeTaxOnDividendsAtART":200,
         |         "proportionClass2NICsLimit":200,
         |         "proportionClass4NICsLimitBR":200,
         |         "proportionClass4NICsLimitHR":200,
         |         "proportionReducedAllowanceLimit":200,
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
         |                "type": "03",
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
         |      },
         |      "previousCalc":{
         |         "calcTimestamp":"4498-07-06T21:42:24.294Z",
         |         "calcID":"12345678",
         |         "calcAmount":1000.25
         |      }
         |   }
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

  object CharitableGivings {
    def apply(currentYear: BigDecimal = 10000.32,
              oneOffCurrentYear: BigDecimal = 1000.23,
              currentYearTreatedAsPreviousYear: BigDecimal = 300.27,
              nextYearTreatedAsCurrentYear: BigDecimal = 400.13,
              nonUKCharities: BigDecimal = 2000.19,
              landAndBuildings: BigDecimal = 700.11,
              sharesOrSecurities: BigDecimal = 600.31,
              investmentsNonUKCharities: BigDecimal =  300.22): String = {
      s"""
                    |  {
                    |    "giftAidPayments": {
                    |        "currentYear": $currentYear,
                    |        "oneOffCurrentYear": $oneOffCurrentYear,
                    |        "currentYearTreatedAsPreviousYear": $currentYearTreatedAsPreviousYear,
                    |        "nextYearTreatedAsCurrentYear": $nextYearTreatedAsCurrentYear,
                    |        "nonUKCharities": $nonUKCharities
                    |    },
                    |    "gifts": {
                    |        "landAndBuildings": $landAndBuildings,
                    |        "sharesOrSecurities": $sharesOrSecurities,
                    |        "investmentsNonUKCharities": $investmentsNonUKCharities
                    |    }
                    |}
         """.stripMargin
    }
  }

  object PropertiesBISS {
    val summary: String = {
      s"""
         |{
         |  "totalIncome" : 10.50,
         |  "totalExpenses" : 10.50,
         |  "totalAdditions" : 10.50,
         |  "totalDeductions" : 10.50,
         |  "netProfit" : 10.50,
         |  "netLoss" : 10.50,
         |  "taxableProfit" : 10.50,
         |  "taxableLoss" : 10.50
         |}
       """.stripMargin
    }
  }

  object SelfEmploymentBISS {
    val summary: String = {
      s"""
         |{
         |  "totalIncome" : 10.55,
         |  "totalExpenses" : 10.55,
         |  "totalAdditions" : 10.55,
         |  "totalDeductions" : 10.55,
         |  "accountingAdjustments" : 10.55,
         |  "netProfit" : 10.55,
         |  "netLoss" : 10.55,
         |  "taxableProfit" : 10.55,
         |  "taxableLoss" : 10.55
         |}
       """.stripMargin
    }
  }
}
