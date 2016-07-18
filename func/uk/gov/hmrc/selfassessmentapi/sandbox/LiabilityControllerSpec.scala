package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {
    "return a 202 response with a link to retrieve the liability" in {
      when()
        .post(s"/sandbox/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liabilities/.+""".r)
    }
  }

  "retrieve liability" should {
    "return a 200 response" in {
      val expectedJson = Json.parse(
        s"""
          |{
          |  "income": {
          |    "incomes": {
          |       "selfEmployment": [
          |         {"sourceId": "self-employment-1", "taxableProfit": 8200, "profit": 10000},
          |         {"sourceId": "self-employment-2", "taxableProfit": 25000, "profit": 28000}
          |       ],
          |       "interestFromUKBanksAndBuildingSocieties": [
          |         {"sourceId": "interest-income-1", "totalInterest": 100},
          |         {"sourceId": "interest-income-2", "totalInterest": 200}
          |       ],
          |       "dividendsFromUKSources": [
          |         {"sourceId": "dividend-income-1", "totalDividend": 1000},
          |         {"sourceId": "dividend-income-2", "totalDividend": 2000}
          |       ],
          |       "employment": [
          |         {"sourceId": "employment-1", "taxableProfit": 5000, "profit": 5000}
          |       ]
          |     },
          |     "deductions": {
          |       "incomeTaxRelief": 5000,
          |       "personalAllowance": 9440,
          |       "totalDeductions": 14440
          |     },
          |     "totalIncomeReceived": 93039,
          |     "totalIncomeOnWhichTaxIsDue": 80000
          |  },
          |  "incomeTaxCalculations": {
          |     "payPensionsProfits": [
          |       {"taxBand": "basicRate", "taxableAmount": 10000, "chargedAt": "20%", "tax": 2000},
          |       {"taxBand": "higherRate", "taxableAmount": 10000, "chargedAt": "40%", "tax": 4000},
          |       {"taxBand": "additionalHigherRate", "taxableAmount": 10000, "chargedAt": "45%", "tax": 4500}
          |     ],
          |     "savingsIncome": [
          |       {"taxBand": "startingRate", "taxableAmount": 10000, "chargedAt": "0%", "tax": 0},
          |       {"taxBand": "nilRate", "taxableAmount": 10000, "chargedAt": "0%", "tax": 0},
          |       {"taxBand": "basicRate", "taxableAmount": 10000, "chargedAt": "20%", "tax": 2000},
          |       {"taxBand": "higherRate", "taxableAmount": 10000, "chargedAt": "40%", "tax": 4000},
          |       {"taxBand": "additionalHigherRate", "taxableAmount": 10000, "chargedAt": "45%", "tax": 4500}
          |     ],
          |     "dividends": [
          |       {"taxBand": "nilRate", "taxableAmount": 10000, "chargedAt": "0%", "tax": 0},
          |       {"taxBand": "basicRate", "taxableAmount": 10000, "chargedAt": "20%", "tax": 2000},
          |       {"taxBand": "higherRate", "taxableAmount": 10000, "chargedAt": "40%", "tax": 4000},
          |       {"taxBand": "additionalHigherRate", "taxableAmount": 10000, "chargedAt": "45%", "tax": 4500}
          |     ],
          |     "incomeTaxCharged": 31500
          |  },
          |  "credits": [
          |        {"type": "dividend", "amount": 46.6},
          |        {"type": "interest-charged", "amount": 12.25}
          |  ],
          |  "class4Nic": {
          |      "calculations": [
          |          { "type": "class-4-nic", "amount": 33695, "percentage": 9, "total": 3032.55},
          |          { "type": "class-4-nic", "amount": 41030, "percentage": 2, "total": 820.60}
          |      ],
          |      "total": 3853.15
          |  },
          |  "incomeTaxDeducted": {
          |      "interestFromUk": 0,
          |      "total": 0
          |  },
          |  "totalTaxDue": 25796.95
          |}
        """.stripMargin)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liabilities/1234""".r)
        .bodyIs(expectedJson)
    }

    "return a valid response when retrieving list of liabilities" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/liabilities")
        .bodyHasPath("""_embedded \ liabilities(0) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/liabilities/1234")
        .bodyHasPath("""_embedded \ liabilities(1) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/liabilities/4321")
        .bodyHasPath("""_embedded \ liabilities(2) \ _links \ self \ href""", s"/self-assessment/$saUtr/$taxYear/liabilities/7777")
    }

  }

  "delete liability" should {
    "return a 204 response" in {
      when()
        .delete(s"/sandbox/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .statusIs(204)
    }
  }

}
