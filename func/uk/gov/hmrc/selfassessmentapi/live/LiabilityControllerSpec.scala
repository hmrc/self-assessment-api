package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.Income
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.{Dividend, SavingsIncome}
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SourceType.UnearnedIncomes
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {

    "return a 202 response with a link to retrieve the liability" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
      .when()
        .post(s"/$saUtr/$taxYear/liabilities")
      .thenAssertThat()
        .statusIs(202)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""^/self-assessment/$saUtr/$taxYear/liabilities/.+""".r)
    }
  }

  "retrieve liability" should {

    "return a 200 response with liability details" in {

      /*
          totalAllowancesAndReliefs = 2 * 10000 (lossesBroughtForward from 2 self employments) + 4471 (personalAllowance)
       */

      val expectedJson =
        s"""
           |{
           |    "class4Nic": {
           |        "calculations": [],
           |        "total": 0
           |    },
           |    "credits": [],
           |    "income": {
           |        "incomes": {
           |            "employment": [],
           |            "selfEmployment": [
           |              {
           |                  "profit": 58529,
           |                  "taxableProfit": 48529
           |              },
           |              {
           |                  "profit": 74529,
           |                  "taxableProfit": 64529
           |              }
           |            ],
           |            "interestFromUKBanksAndBuildingSocieties": [
           |              {
           |                "totalInterest": 3000
           |              }
           |            ],
           |            "dividendsFromUKSources": [
           |              {
           |                "totalDividend": 3000
           |              }
           |            ]
           |        },
           |        "deductions": {
           |            "incomeTaxRelief": 20000,
           |            "personalAllowance": 1471,
           |            "totalDeductions": 21471
           |        },
           |        "totalIncomeReceived": 139058,
           |        "totalIncomeOnWhichTaxIsDue": 117587
           |    },
           |    "incomeTaxCalculations": {
           |       "payPensionsProfits": [
           |            {
           |                "chargedAt": "20%",
           |                "tax": 6400,
           |                "taxBand": "basicRate",
           |                "taxableAmount": 32000
           |            },
           |            {
           |                "chargedAt": "40%",
           |                "tax": 31834,
           |                "taxBand": "higherRate",
           |                "taxableAmount": 79587
           |            },
           |            {
           |                "chargedAt": "45%",
           |                "tax": 0,
           |                "taxBand": "additionalHigherRate",
           |                "taxableAmount": 0
           |            }
           |       ],
           |       "savingsIncome": [
           |            {
           |                "chargedAt": "0%",
           |                "tax": 0,
           |                "taxBand": "nilRate",
           |                "taxableAmount": 500
           |            },
           |            {
           |                "chargedAt": "0%",
           |                "tax": 0,
           |                "taxBand": "startingRate",
           |                "taxableAmount": 0
           |            },
           |            {
           |                "chargedAt": "20%",
           |                "tax": 0,
           |                "taxBand": "basicRate",
           |                "taxableAmount": 0
           |            },
           |            {
           |                "chargedAt": "40%",
           |                "tax": 1000,
           |                "taxBand": "higherRate",
           |                "taxableAmount": 2500
           |            },
           |            {
           |                "chargedAt": "45%",
           |                "tax": 0,
           |                "taxBand": "additionalHigherRate",
           |                "taxableAmount": 0
           |            }
           |    ],
           |    "dividends": [{
           |                "chargedAt": "0%",
           |                "tax": 0,
           |                "taxBand": "nilRate",
           |                "taxableAmount": 3000
           |           }],
           |    "incomeTaxCharged": 39234,
           |    },
           |    "incomeTaxDeducted": {
           |      "interestFromUk": 2400,
           |      "total": 600
           |    },
           |    "totalTaxDue": 0
           |}
        """.stripMargin

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 60000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 10000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(SelfEmployments.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 80000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/incomes", Some(toJson(Income.example().copy(amount = 6000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes", Some(UnearnedIncomes.example()))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/savings", Some(toJson(SavingsIncome.example().copy(amount = 800))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/savings", Some(toJson(SavingsIncome.example().copy(amount = 1600))))
        .thenAssertThat()
        .statusIs(201)
       .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/dividends", Some(toJson(Dividend.example().copy(amount = 1000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/unearned-incomes/%sourceId%/dividends", Some(toJson(Dividend.example().copy(amount = 2000))))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .statusIs(202)
        .when()
        .get(s"/$saUtr/$taxYear/liabilities/%liabilityId%")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(expectedJson)
    }
  }

  "delete liability" should {
    "return a resourceIsNotImplemented response" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/$taxYear/liabilities/1234")
        .thenAssertThat()
        .isNotImplemented
    }
  }

  "find liability" should {
    "return a resourceIsNotImplemented response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/liabilities")
        .thenAssertThat()
        .isNotImplemented
    }
  }

}
