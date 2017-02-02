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

package uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.{JsonSpec, Jsons}
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, AccountingType, ErrorCode}

class SelfEmploymentSpec extends JsonSpec {

  "SelfEmployment JSON" should {
    "round ignore the id if it is provided by the user" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")),
        AccountingType.CASH, LocalDate.now.minusDays(1), "Acme Ltd.", "Boxes made of corrugated cardboard (manufacture)", "Acme Rd.", None, None, None, "A9 9AA")
      val expectedOutput = input.copy(id = None, businessDescription = "Boxes made of corrugated cardboard ")

      assertJsonIs(input, expectedOutput)
    }

    "return a COMMENCEMENT_DATE_NOT_IN_THE_PAST error when using a commencement date in the future" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")),
        AccountingType.CASH, LocalDate.now.plusDays(1), "Acme Ltd.", "Boxes made of corrugated cardboard (manufacture)", "Acme Rd.", None, None, None, "A9 9AA")
      assertValidationErrorWithCode(input,
        "/commencementDate", ErrorCode.DATE_NOT_IN_THE_PAST)
    }

    "return a INVALID_ACCOUNTING_PERIOD error when startDate < endDate" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-04-02"), LocalDate.parse("2017-04-01")),
        AccountingType.CASH, LocalDate.now.minusDays(1), "Acme Ltd.", "Boxes made of corrugated cardboard (manufacture)", "Acme Rd.", None, None, None, "A9 9AA")
      assertValidationErrorWithCode(input,
        "/accountingPeriod", ErrorCode.INVALID_ACCOUNTING_PERIOD)
    }

    "return a DATE_NOT_IN_THE_PAST error when proving an accounting period with a start date that is before 2017-04-01" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-03-01"), LocalDate.parse("2017-03-03")),
        AccountingType.CASH, LocalDate.now.minusDays(1), "Acme Ltd.", "Boxes made of corrugated cardboard (manufacture)", "Acme Rd.", None, None, None, "A9 9AA")
      assertValidationErrorWithCode(input,
        "/accountingPeriod/start", ErrorCode.START_DATE_INVALID)
    }

    "return a INVALID_VALUE error when providing an invalid accounting type" in {
      val json = Jsons.SelfEmployment(accountingType = "OHNO")

      assertValidationErrorsWithCode[SelfEmployment](json, Map("/accountingType" -> Seq(ErrorCode.INVALID_VALUE)))
    }

    "return a error when providing an empty commencementDate" in {
      val json = Jsons.SelfEmployment(commencementDate = "")

      assertValidationErrorsWithMessage[SelfEmployment](json, Map("/commencementDate" -> Seq("error.expected.jodadate.format")))
    }

    "return a error when providing an non-ISO (i.e. YYYY-MM-DD) commencementDate" in {
      val json = Jsons.SelfEmployment(commencementDate = "01-01-2016")

      assertValidationErrorsWithMessage[SelfEmployment](json, Map("/commencementDate" -> Seq("error.expected.jodadate.format")))
    }

    "return a error when providing non-ISO (i.e. YYYY-MM-DD) dates to the accountingPeriod" in {
      val json = Jsons.SelfEmployment(accPeriodStart = "01-01-2016", accPeriodEnd = "02-01-2016")

      assertValidationErrorsWithMessage[SelfEmployment](json,
        Map("/accountingPeriod/start" -> Seq("error.expected.jodadate.format"),
          "/accountingPeriod/end" -> Seq("error.expected.jodadate.format")))
    }

    "return a error when providing an empty SelfEmployment body" in {
      val json = "{}"

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json),
        Map("/accountingPeriod" -> Seq("error.path.missing"),
            "/accountingType" -> Seq("error.path.missing"),
            "/commencementDate" -> Seq("error.path.missing"),
            "/tradingName" -> Seq("error.path.missing"),
            "/businessDescription" -> Seq("error.path.missing"),
            "/businessAddressLineOne" -> Seq("error.path.missing"),
            "/businessPostcode" -> Seq("error.path.missing")))
    }

    "return a error when providing an empty accountingPeriod body" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {},
           |  "accountingType": "CASH",
           |  "commencementDate": "2016-01-01",
           |  "tradingName": "Acme Ltd.",
           |  "businessDescription": "Boxes made of corrugated cardboard (manufacture)",
           |  "businessAddressLineOne": "1 Acme Rd.",
           |  "businessAddressLineTwo": "London",
           |  "businessAddressLineThree": "Greater London",
           |  "businessAddressLineFour": "United Kingdom",
           |  "businessPostcode": "A9 9AA"
           |}
         """.stripMargin

      assertValidationErrorsWithMessage[SelfEmployment](Json.parse(json),
        Map("/accountingPeriod/start" -> Seq("error.path.missing"),
            "/accountingPeriod/end" -> Seq("error.path.missing")))
    }

    "return a error when providing a trading name that is not between 1 and 105 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(tradingName = "")
      val jsonTwo = Jsons.SelfEmployment(tradingName = "a" * 106)

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a business description that does not conform to the UK SIC 2007 classifications" in {
      val json = Jsons.SelfEmployment(businessDescription = "silly-business")

      assertValidationErrorsWithCode[SelfEmployment](json, Map("/businessDescription" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a first address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(businessAddressLineOne = "")
      val jsonTwo = Jsons.SelfEmployment(businessAddressLineOne = "a" * 36)

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/businessAddressLineOne" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/businessAddressLineOne" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a second address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(businessAddressLineTwo = "")
      val jsonTwo = Jsons.SelfEmployment(businessAddressLineTwo = "a" * 36)

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/businessAddressLineTwo" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/businessAddressLineTwo" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a third address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(businessAddressLineThree = "")
      val jsonTwo = Jsons.SelfEmployment(businessAddressLineThree = "a" * 36)

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/businessAddressLineThree" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/businessAddressLineThree" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a fourth address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(businessAddressLineFour = "")
      val jsonTwo = Jsons.SelfEmployment(businessAddressLineFour = "a" * 36)

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/businessAddressLineFour" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/businessAddressLineFour" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a postcode that is not between 1 and 10 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(businessPostcode = "")
      val jsonTwo = Jsons.SelfEmployment(businessPostcode = "a" * 11)

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/businessPostcode" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/businessPostcode" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }
  }
}
