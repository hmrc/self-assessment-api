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

package uk.gov.hmrc.selfassessmentapi.models.des

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class ObligationsSpec extends JsonSpec {
  private def createObligationDetails(status: String) =
    ObligationDetail(status, "2017-04-06", "2017-07-07", Some("2017-07-01"), "2017-08-07", Some("004"))

  "Obligations" should {
    "be able to be constructed from a valid JSON" in {
      val json =
        Json.parse(
          s"""
             |{
             |  "obligations": [
             |    {
             |      "id": "XAIS54321543215",
             |      "type": "ITSB",
             |      "details": [
             |        {
             |          "status": "O",
             |          "inboundCorrespondenceFromDate": "2016-10-07",
             |          "inboundCorrespondenceToDate": "2016-10-10",
             |          "inboundCorrespondenceDueDate": "2016-10-31",
             |          "periodKey": "004"
             |        },
             |        {
             |          "status": "F",
             |          "inboundCorrespondenceFromDate": "2016-10-01",
             |          "inboundCorrespondenceToDate": "2016-10-07",
             |          "inboundCorrespondenceDateReceived": "2016-10-15",
             |          "inboundCorrespondenceDueDate": "2016-10-20",
             |          "periodKey": "004"
             |        }
             |      ]
             |    }
             |  ]
             |}
         """.stripMargin)


      val obligations = json.as[Obligations]

      obligations.obligations.size shouldBe 1
      obligations.obligations.head.id shouldBe Some("XAIS54321543215")
      obligations.obligations.head.`type` shouldBe "ITSB"
      obligations.obligations.head.details should contain theSameElementsAs Seq(
        ObligationDetail(
          status = "O",
          inboundCorrespondenceFromDate = "2016-10-07",
          inboundCorrespondenceToDate = "2016-10-10",
          inboundCorrespondenceDateReceived = None,
          inboundCorrespondenceDueDate = "2016-10-31",
          periodKey = Some("004")),
        ObligationDetail(
          status = "F",
          inboundCorrespondenceFromDate = "2016-10-01",
          inboundCorrespondenceToDate = "2016-10-07",
          inboundCorrespondenceDateReceived = Some("2016-10-15"),
          inboundCorrespondenceDueDate = "2016-10-20",
          periodKey = Some("004")))

    }
  }

  "isFulfilled" should {
    "return true if the obligation is fulfilled" in {
      createObligationDetails(status = "F").isFinalised shouldBe true
    }

    "return false if the obligation is not fulfilled" in {
      createObligationDetails(status = "O").isFinalised shouldBe false
    }
  }
}
