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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.UnitSpec

class SelfEmploymentPeriodResponseSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {
  private val mockResponse = mock[HttpResponse]
  private val unitUnderTest = SelfEmploymentPeriodResponse(mockResponse)

  "createLocationHeader" should {
    "return a properly formed resource location" in {
      unitUnderTest.createLocationHeader(Nino("AA999999A"), "abc", "def") shouldBe
        "/self-assessment/ni/AA999999A/self-employments/abc/periods/def"
    }
  }

  "isInvalidPeriod" should {
    "return Some(true) if the error code from DES is equal to INVALID_PERIOD" in {
      when(mockResponse.json).thenReturn(Json.parse(
        """
          |{
          |  "code": "INVALID_PERIOD",
          |  "reason": "don't care"
          |}
        """.stripMargin))

      unitUnderTest.isInvalidPeriod shouldBe true
    }

    "return Some(false) if the error code from DES is not equal to INVALID_PERIOD" in {
      when(mockResponse.json).thenReturn(Json.parse(
        """
          |{
          |  "code": "OOPS",
          |  "reason": "don't care"
          |}
        """.stripMargin))

      unitUnderTest.isInvalidPeriod shouldBe false
    }

    "return false if the error code from DES does no match the expected format" in {
      when(mockResponse.json).thenReturn(Json.obj())

      unitUnderTest.isInvalidPeriod shouldBe false
    }
  }

  "period" should {
    "return Some(apiPeriod) if the DES response is correct" in {
      when(mockResponse.json).thenReturn(Json.parse(
        """
          |{
          |   "id": "abc",
          |   "from": "2017-04-05",
          |   "to": "2017-05-04",
          |   "financials": {
          |      "incomes": {
          |         "turnover": 200.00
          |      },
          |      "deductions": {
          |         "other": {
          |            "amount": 200.00,
          |            "disallowableAmount": 200.00
          |         }
          |      }
          |   }
          |}
        """.stripMargin))

      unitUnderTest.period.isDefined shouldBe true
    }

    "return None if the response from DES does not match the expected format" in {
      when(mockResponse.json).thenReturn(Json.obj())

      unitUnderTest.period shouldBe None
    }
  }

  "allPeriods" should {
    "return Some(Seq(apiPeriod)) if the DES response is correct" in {
      when(mockResponse.json).thenReturn(Json.parse(
        """
          |{
          |  "periods": [
          |    {
          |      "transactionReference": "abc",
          |      "from": "2017-04-05",
          |      "to": "2017-05-04"
          |    }
          |  ]
          |}
        """.stripMargin))

      unitUnderTest.allPeriods(86).size shouldBe 1
    }

    "return None if the response from DES does not match the expected format" in {
      when(mockResponse.json).thenReturn(Json.obj())

      unitUnderTest.allPeriods(86) shouldBe empty
    }
  }

  "transactionReference" should {
    "return Some(reference) if the response is correct" in {
      when(mockResponse.json).thenReturn(Json.parse(
        """
          |{
          |  "transactionReference": "abc"
          |}
        """.stripMargin))

      unitUnderTest.transactionReference shouldBe Some("abc")
    }

    "return None if the response from DES does not match the expected format" in {
      when(mockResponse.json).thenReturn(Json.obj())

      unitUnderTest.transactionReference shouldBe None
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockResponse)
  }
}
