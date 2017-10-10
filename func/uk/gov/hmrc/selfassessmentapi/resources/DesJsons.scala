package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.CessationReason
import uk.gov.hmrc.selfassessmentapi.models.des.properties.{Common, FHL, Other}
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType

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

    val invalidNino: String = error("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
    val invalidPayload: String = error("INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
    val ninoNotFound: String = error("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found.")
    val notFoundProperty: String = error(
      "NOT_FOUND_PROPERTY",
      "The remote endpoint has indicated that no data can be found for the given property type.")
    val notFound: String = error("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")
    val tradingNameConflict: String = error("CONFLICT", "Duplicated trading name.")
    val serverError: String =
      error("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")
    val serviceUnavailable: String = error("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
    val tooManySources: String =
      error("TOO_MANY_SOURCES", "You may only have a maximum of one self-employment source.")
    val invalidPeriod: String =
      error("INVALID_PERIOD", "The remote endpoint has indicated that a overlapping period was submitted.")
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
    val invalidCalcId: String = error("INVALID_CALCID", "Submission has not passed validation")
    val propertyConflict: String = error("CONFLICT", "Property already exists.")
    val invalidIncomeSource: String = error(
      "INVALID_INCOME_SOURCE",
      "The remote endpoint has indicated that the taxpayer does not have an associated property.")
    val notFoundIncomeSource: String = error("NOT_FOUND_INCOME_SOURCE", "The remote endpoint has indicated that no data can be found for the given income source id.")
    val invalidDateFrom: String =
      error("INVALID_DATE_FROM", "Submission has not passed validation. Invalid parameter from.")
    val invalidDateTo: String = error("INVALID_DATE_TO", "Submission has not passed validation. Invalid parameter to.")
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
           |      "privateUseAdjustment": 0.0
           |   },
           |   "annualAllowances": {
           |      "annualInvestmentAllowance": 0.0,
           |      "otherCapitalAllowance": 0.0,
           |      "zeroEmissionGoodsVehicleAllowance": 0.0,
           |      "businessPremisesRenovationAllowance": 0.0,
           |      "costOfReplacingDomGoods": 0.0
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
           |      "privateUseAdjustment": 0.0
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
         |      "accountingPeriodEndDate": "2001-01-01"
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
              premisesRunningCosts: BigDecimal = 0,
              repairsAndMaintenance: BigDecimal = 0,
              financialCosts: BigDecimal = 0,
              professionalFees: BigDecimal = 0,
              other: BigDecimal = 0): JsValue =
        Json.toJson(
          FHL.Properties(
            transactionReference = Some(transactionReference),
            from = from,
            to = to,
            financials = Some(
              FHL
                .Financials(
                  incomes = Some(FHL.Incomes(rentIncome = Some(Common.Income(rentIncome)))),
                  deductions = Some(FHL.Deductions(
                    premisesRunningCosts = Some(premisesRunningCosts),
                    repairsAndMaintenance = Some(repairsAndMaintenance),
                    financialCosts = Some(financialCosts),
                    professionalFees = Some(professionalFees),
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
                premisesRunningCosts: Option[BigDecimal] = Some(0),
                repairsAndMaintenance: Option[BigDecimal] = Some(0),
                financialCosts: Option[BigDecimal] = Some(0),
                professionalFees: Option[BigDecimal] = Some(0),
                costOfServices: Option[BigDecimal] = Some(0),
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
                                             reversePremiums = reversePremiums)),
                deductions = Some(Other.Deductions(
                  premisesRunningCosts = premisesRunningCosts,
                  repairsAndMaintenance = repairsAndMaintenance,
                  financialCosts = financialCosts,
                  professionalFees = professionalFees,
                  costOfServices = costOfServices,
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
         |      "id": "$id",
         |      "type": "ITSB",
         |      "details": [
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
         |      "id": "$id",
         |      "type": "ITSP",
         |      "details": [
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
  }

  object TaxCalculation {
    def apply(id: String = "abc"): String = {
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
        |						"incomeSourceID": "abcdefghijklm",
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
