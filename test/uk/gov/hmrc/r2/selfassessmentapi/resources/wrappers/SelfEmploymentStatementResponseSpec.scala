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

package uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers

import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models.{EopsObligation, EopsObligations, EopsStatus, InvalidDateError}

class SelfEmploymentStatementResponseSpec extends UnitSpec {

  val nino = "AA111111A"

  val desOpenObligationJson: JsValue = Json.parse(
    s"""
       |{
       |  "obligations": [
       |    {
       |      "identification": {
       |        "referenceNumber": "$nino",
       |        "incomeSourceType": "ITSB",
       |        "referenceType": "NINO"
       |     },
       |    "obligationDetails": [
       |    {
       |      "status": "F",
       |      "inboundCorrespondenceFromDate": "2017-01-01",
       |      "inboundCorrespondenceToDate": "2017-03-31",
       |      "inboundCorrespondenceDueDate": "2017-04-30",
       |      "periodKey": "001"
       |    },
       |    {
       |      "status": "O",
       |      "inboundCorrespondenceFromDate": "2017-04-06",
       |      "inboundCorrespondenceToDate": "2017-12-01",
       |      "inboundCorrespondenceDueDate": "2018-01-01",
       |      "periodKey": "EOPS"
       |    }
       |    ]
       |  }
       |  ]
       |}
    """.stripMargin)

  val desFulfilledObligationJson: JsValue = Json.parse(
    s"""
       |{
       |  "obligations": [
       |    {
       |      "identification": {
       |        "referenceNumber": "$nino",
       |        "incomeSourceType": "ITSB",
       |        "referenceType": "NINO"
       |     },
       |    "obligationDetails": [
       |    {
       |      "status": "F",
       |      "inboundCorrespondenceFromDate": "2017-01-01",
       |      "inboundCorrespondenceToDate": "2017-03-31",
       |      "inboundCorrespondenceDueDate": "2017-04-30",
       |      "periodKey": "001"
       |    },
       |    {
       |      "status": "F",
       |      "inboundCorrespondenceFromDate": "2017-04-06",
       |      "inboundCorrespondenceToDate": "2017-12-01",
       |      "inboundCorrespondenceDueDate": "2018-01-01",
       |      "inboundCorrespondenceDateReceived": "2018-01-01",
       |      "periodKey": "EOPS"
       |    }
       |    ]
       |  }
       |  ]
       |}
    """.stripMargin)

  "SelfEmploymentsStatementResponse" when {
    "obligation details are returned with an open EOPS obligation" should {
      "wrap valid response" in {
        val response = SelfEmploymentStatementResponse(HttpResponse(200, Some(desOpenObligationJson)))

        val eops = response.retrieveEOPSObligation(identifier = nino)
        eops.right.get shouldBe
          Some(
            EopsObligations(
              Seq(
                EopsObligation(
                  start = LocalDate.parse("2017-04-06"),
                  end = LocalDate.parse("2017-12-01"),
                  due = LocalDate.parse("2018-01-01"),
                  processed = None,
                  status = EopsStatus.OPEN
                )
              )
            )
          )
      }
    }

    "obligation details are returned with a fulfilled EOPS obligation" should {
      "wrap valid response" in {
        val response = SelfEmploymentStatementResponse(HttpResponse(200, Some(desFulfilledObligationJson)))

        val eops = response.retrieveEOPSObligation(identifier = nino)
        eops.right.get shouldBe
          Some(
            EopsObligations(
              Seq(
                EopsObligation(
                  start = LocalDate.parse("2017-04-06"),
                  end = LocalDate.parse("2017-12-01"),
                  due = LocalDate.parse("2018-01-01"),
                  processed = Some(LocalDate.parse("2018-01-01")),
                  status = EopsStatus.FULFILLED
                )
              )
            )
          )
      }
    }
    "obligation details are returned with no EOPS obligation" should {
      "return None" in {
        val noEopsObligations =
          s"""
             |{
             |  "obligations": [
             |    {
             |      "identification": {
             |        "referenceNumber": "$nino",
             |        "incomeSourceType": "ITSB",
             |        "referenceType": "NINO"
             |     },
             |    "obligationDetails": [
             |    {
             |      "status": "F",
             |      "inboundCorrespondenceFromDate": "2017-01-01",
             |      "inboundCorrespondenceToDate": "2017-03-31",
             |      "inboundCorrespondenceDueDate": "2017-04-30",
             |      "periodKey": "001"
             |    }
             |   ]
             |  }
             | ]
             |}
           """.stripMargin
        val response = SelfEmploymentStatementResponse(HttpResponse(200, Some(Json.parse(noEopsObligations))))
        val eops = response.retrieveEOPSObligation(identifier = nino)
        eops.right.get shouldBe None
      }
    }
    "obligation details are returned with obligations details that do not match the reference number" should {
      "return None" in {
        val badEops =
          s"""
             |{
             |  "obligations": [
             |    {
             |      "identification": {
             |        "referenceNumber": "ZZ999999Z",
             |        "incomeSourceType": "ITSB",
             |        "referenceType": "NINO"
             |     },
             |    "obligationDetails": [
             |    {
             |      "status": "F",
             |      "inboundCorrespondenceFromDate": "2017-01-01",
             |      "inboundCorrespondenceToDate": "2017-03-31",
             |      "inboundCorrespondenceDueDate": "2017-04-30",
             |      "periodKey": "EOPS"
             |    }
             |   ]
             |  }
             | ]
             |}
          """.stripMargin
        val response = SelfEmploymentStatementResponse(HttpResponse(200, Some(Json.parse(badEops))))
        val eops = response.retrieveEOPSObligation(identifier = nino)
        eops.right.get shouldBe None
      }
    }
    "obligation details are returned with obligations details that do not match the income source type" should {
      "return None" in {
        val badEops =
          s"""
             |{
             |  "obligations": [
             |    {
             |      "identification": {
             |        "referenceNumber": "AA111111A",
             |        "incomeSourceType": "ITSP",
             |        "referenceType": "NINO"
             |     },
             |    "obligationDetails": [
             |    {
             |      "status": "F",
             |      "inboundCorrespondenceFromDate": "2017-01-01",
             |      "inboundCorrespondenceToDate": "2017-03-31",
             |      "inboundCorrespondenceDueDate": "2017-04-30",
             |      "periodKey": "EOPS"
             |    }
             |   ]
             |  }
             | ]
             |}
          """.stripMargin
        val response = SelfEmploymentStatementResponse(HttpResponse(200, Some(Json.parse(badEops))))
        val eops = response.retrieveEOPSObligation(identifier = nino)
        eops.right.get shouldBe None
      }
    }
    "obligation details are returned with obligations details that can not be parsed" should {
      "return None" in {
        val badEops =
          s"""
             |{
             |  "obligations": [
             |    {
             |      "identification": {
             |        "referenceNumber": "AA111111A",
             |        "incomeSourceType": "ITSB",
             |        "referenceType": "NINO"
             |     },
             |    "obligationDetails": [
             |    {
             |      "status": "F",
             |      "inboundCorrespondenceFromDate": "thisisnotadate",
             |      "inboundCorrespondenceToDate": "thisisnotadate",
             |      "inboundCorrespondenceDueDate": "thisisnotadate",
             |      "periodKey": "EOPS"
             |    }
             |   ]
             |  }
             | ]
             |}
          """.stripMargin
        val response = SelfEmploymentStatementResponse(HttpResponse(200, Some(Json.parse(badEops))))
        val eops = response.retrieveEOPSObligation(identifier = nino)
        eops.left.get shouldBe a [InvalidDateError]
      }
    }
  }
}
