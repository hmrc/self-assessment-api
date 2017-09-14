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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.{AccountingPeriod, AccountingType, ErrorCode}
import uk.gov.hmrc.selfassessmentapi.resources.{JsonSpec, Jsons}

class SelfEmploymentSpec extends JsonSpec {

  "SelfEmployment JSON" should {
    "round ignore the id if it is provided by the user" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")),
        AccountingType.CASH, LocalDate.now.minusDays(1), None, "Acme Ltd.", "Accountancy services", Address("Acme Rd.", None, None, None, Some("A9 9AA"), "GB"))
      val expectedOutput = input.copy(id = None)

      assertJsonIs(input, expectedOutput)
    }

    "return a COMMENCEMENT_DATE_NOT_IN_THE_PAST error when using a commencement date in the future" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02")),
        AccountingType.CASH, LocalDate.now.plusDays(1), None, "Acme Ltd.", "Accountancy services", Address("Acme Rd.", None, None, None, Some("A9 9AA"), "GB"))
      assertValidationErrorWithCode(input,
        "/commencementDate", ErrorCode.DATE_NOT_IN_THE_PAST)
    }

    "return a INVALID_ACCOUNTING_PERIOD error when startDate < endDate" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-04-02"), LocalDate.parse("2017-04-01")),
        AccountingType.CASH, LocalDate.now.minusDays(1), None, "Acme Ltd.", "Accountancy services", Address("Acme Rd.", None, None, None, Some("A9 9AA"), "GB"))
      assertValidationErrorWithCode(input,
        "/accountingPeriod", ErrorCode.INVALID_ACCOUNTING_PERIOD)
    }

    "return a DATE_NOT_IN_THE_PAST error when proving an accounting period with a start date that is before 2017-04-01" in {
      val input = SelfEmployment(Some("myid"), AccountingPeriod(LocalDate.parse("2017-03-01"), LocalDate.parse("2017-03-03")),
        AccountingType.CASH, LocalDate.now.minusDays(1), None, "Acme Ltd.", "Accountancy services", Address("Acme Rd.", None, None, None, Some("A9 9AA"), "GB"))
      assertValidationErrorWithCode(input,
        "/accountingPeriod/start", ErrorCode.START_DATE_INVALID)
    }

    "return a INVALID_VALUE error when providing an invalid accounting type" in {
      val json = Jsons.SelfEmployment(accountingType = "OHNO")

      assertValidationErrorsWithCode[SelfEmployment](json, Map("/accountingType" -> Seq(ErrorCode.INVALID_VALUE)))
    }

    "return a error when providing an empty commencementDate" in {
      val json = Jsons.SelfEmployment(commencementDate = Some(""))

      assertValidationErrorsWithMessage[SelfEmployment](json, Map("/commencementDate" -> Seq("error.expected.jodadate.format")))
    }

    "return a error when providing an non-ISO (i.e. YYYY-MM-DD) commencementDate" in {
      val json = Jsons.SelfEmployment(commencementDate = Some("01-01-2016"))

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
            "/description" -> Seq("error.path.missing"),
            "/address" -> Seq("error.path.missing")))
    }

    "return a error when providing an empty accountingPeriod body" in {
      val json =
        s"""
           |{
           |  "accountingPeriod": {},
           |  "accountingType": "CASH",
           |  "commencementDate": "2016-01-01",
           |  "cessationDate": "2018-04-05",
           |  "tradingName": "Acme Ltd.",
           |  "description": "Accountancy services",
           |  "address": {
           |    "lineOne": "1 Acme Rd.",
           |    "lineTwo": "London",
           |    "lineThree": "Greater London",
           |    "lineFour": "United Kingdom",
           |    "postcode": "A9 9AA",
           |    "country": "GB"
           |  }
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

    "return a error when providing an empty business description" in {
      val json = Jsons.SelfEmployment(description = Some(""))

      assertValidationErrorsWithCode[SelfEmployment](json, Map("/description" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a business description that does not conform to the UK SIC 2007 classifications" in {
      val json = Jsons.SelfEmployment(description = Some("silly-business"))

      assertValidationErrorsWithCode[SelfEmployment](json, Map("/description" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a first address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(lineOne = Some(""))
      val jsonTwo = Jsons.SelfEmployment(lineOne = Some("a" * 36))

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/address/lineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/address/lineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a second address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(lineTwo = Some(""))
      val jsonTwo = Jsons.SelfEmployment(lineTwo = Some("a" * 36))

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/address/lineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/address/lineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a third address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(lineThree = Some(""))
      val jsonTwo = Jsons.SelfEmployment(lineThree = Some("a" * 36))

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/address/lineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/address/lineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a fourth address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(lineFour = Some(""))
      val jsonTwo = Jsons.SelfEmployment(lineFour = Some("a" * 36))

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/address/lineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/address/lineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a postcode that is not between 1 and 10 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(postcode = Some(""))
      val jsonTwo = Jsons.SelfEmployment(postcode = Some("a" * 11))

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/address/postcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/address/postcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
    }

    "return an error when providing a postcode with an invalid format" in {
      val jsonOne = Jsons.SelfEmployment(postcode = Some("!?"))
      val jsonTwo = Jsons.SelfEmployment(postcode = Some("a" * 9))

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/address/postcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/address/postcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
    }

    "return a error when providing a country that is not 2 characters in length" in {
      val jsonOne = Jsons.SelfEmployment(country = Some(""))
      val jsonTwo = Jsons.SelfEmployment(country = Some("Great Britain"))

      assertValidationErrorsWithCode[SelfEmployment](jsonOne, Map("/address/country" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmployment](jsonTwo, Map("/address/country" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }
  }


}
