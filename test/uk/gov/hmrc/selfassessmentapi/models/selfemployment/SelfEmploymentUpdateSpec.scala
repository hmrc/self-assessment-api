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
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.selfassessmentapi.resources.{JsonSpec, Jsons}

class SelfEmploymentUpdateSpec extends JsonSpec {
  "SelfEmploymentUpdate JSON" should {
    "return a error when providing an empty SelfEmploymentUpdate body" in {
      assertValidationErrorsWithMessage[SelfEmploymentUpdate](
        Json.obj(),
        Map(
          "/accountingPeriod" -> Seq("error.path.missing"),
          "/accountingType" -> Seq("error.path.missing"),
          "/commencementDate" -> Seq("error.path.missing"),
          "/tradingName" -> Seq("error.path.missing"),
          "/description" -> Seq("error.path.missing"),
          "/address" -> Seq("error.path.missing"),
          "/paperless" -> Seq("error.path.missing"),
          "/seasonal" -> Seq("error.path.missing"),
          "/effectiveDate" -> Seq("error.path.missing")
        )
      )
    }

    "return a error when providing a trading name that is not between 1 and 105 in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(tradingName = "")
      val jsonTwo = Jsons.SelfEmployment.update(tradingName = "a" * 106)
      val jsonThree = Jsons.SelfEmployment.update(tradingName = "currency £$ inc")
      val jsonFour = Jsons.SelfEmployment.update(tradingName = "test (@ ^ *) name")
      val jsonFive = Jsons.SelfEmployment.update(tradingName = "          ")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne,
                                                           Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo,
                                                           Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree,
                                                           Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonFour,
                                                           Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonFive,
                                                           Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing an empty business description" in {
      val json = Jsons.SelfEmployment.update(description = "")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        json,
        Map("/description" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a business description that does not conform to the UK SIC 2007 classifications" in {
      val json = Jsons.SelfEmployment.update(description = "silly-business")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        json,
        Map("/description" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a first address line that is not between 1 and 35 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(lineOne = "")
      val jsonTwo = Jsons.SelfEmployment.update(lineOne = "a" * 36)
      val jsonThree = Jsons.SelfEmployment.update(lineOne = "Line 1(£$)")
      val jsonFour = Jsons.SelfEmployment.update(lineOne = "    ")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/address/lineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/address/lineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonThree,
        Map("/address/lineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonFour,
        Map("/address/lineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a second address line that is not between 1 and 35 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(lineTwo = "")
      val jsonTwo = Jsons.SelfEmployment.update(lineTwo = "a" * 36)
      val jsonThree = Jsons.SelfEmployment.update(lineTwo = "Line 1(£$)")
      val jsonFour = Jsons.SelfEmployment.update(lineTwo = "      ")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/address/lineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/address/lineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonThree,
        Map("/address/lineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonFour,
        Map("/address/lineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a third address line that is not between 1 and 35 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(lineThree = "")
      val jsonTwo = Jsons.SelfEmployment.update(lineThree = "a" * 36)
      val jsonThree = Jsons.SelfEmployment.update(lineThree = "Line 1(£$)")
      val jsonFour = Jsons.SelfEmployment.update(lineThree = "      ")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/address/lineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/address/lineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonThree,
        Map("/address/lineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonFour,
        Map("/address/lineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a fourth address line that is not between 1 and 35 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(lineFour = "")
      val jsonTwo = Jsons.SelfEmployment.update(lineFour = "a" * 36)
      val jsonThree = Jsons.SelfEmployment.update(lineFour = "Line 1(£$)")
      val jsonFour = Jsons.SelfEmployment.update(lineFour = "      ")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/address/lineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/address/lineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonThree,
        Map("/address/lineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonFour,
        Map("/address/lineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a postcode that is not between 1 and 10 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(postcode = Some(""))
      val jsonTwo = Jsons.SelfEmployment.update(postcode = Some("a" * 11))
      val jsonThree = Jsons.SelfEmployment.update(postcode = Some("1(£$)"))

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne,
                                                           Map("/address/postcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo,
                                                           Map("/address/postcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree,
                                                           Map("/address/postcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
    }

    "return a error when country is GB and postcode is not provided" in {
      val jsonOne = Jsons.SelfEmployment.update(postcode = None)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne,
                                                           Map("/address" -> Seq(ErrorCode.MANDATORY_FIELD_MISSING)))
    }

    "pass when country is FR and postcode is not provided" in {
      val jsonOne = Jsons.SelfEmployment.update(postcode = None, country = "FR")

      assertJsonValidationPasses[SelfEmploymentUpdate](jsonOne)
    }

    "return a error when providing a country that is not 2 characters in length" in {
      val jsonOne = Jsons.SelfEmployment.update(country = "")
      val jsonTwo = Jsons.SelfEmployment.update(country = "Great Britain")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/address/country" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/address/country" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a primaryPhoneNumber that is not between 1 and 24 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(primaryPhoneNumber = "")
      val jsonTwo = Jsons.SelfEmployment.update(primaryPhoneNumber = "1" * 25)
      val jsonThree = Jsons.SelfEmployment.update(primaryPhoneNumber = "1(£$)")
      val jsonFour = Jsons.SelfEmployment.update(primaryPhoneNumber = "      ")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/contactDetails/primaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/contactDetails/primaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonThree,
        Map("/contactDetails/primaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonFour,
        Map("/contactDetails/primaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a secondaryPhoneNumber that is not between 1 and 24 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(secondaryPhoneNumber = "")
      val jsonTwo = Jsons.SelfEmployment.update(secondaryPhoneNumber = "1" * 25)
      val jsonThree = Jsons.SelfEmployment.update(secondaryPhoneNumber = "1(£$)")
      val jsonFour = Jsons.SelfEmployment.update(secondaryPhoneNumber = "      ")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/contactDetails/secondaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/contactDetails/secondaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonThree,
        Map("/contactDetails/secondaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonFour,
        Map("/contactDetails/secondaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a faxNumber that is not between 1 and 24 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(faxNumber = "")
      val jsonTwo = Jsons.SelfEmployment.update(faxNumber = "1" * 25)
      val jsonThree = Jsons.SelfEmployment.update(faxNumber = "1(£$)")
      val jsonFour = Jsons.SelfEmployment.update(faxNumber = "      ")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/contactDetails/faxNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/contactDetails/faxNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonThree,
        Map("/contactDetails/faxNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonFour,
        Map("/contactDetails/faxNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a emailAddress that is not valid" in {
      val jsonOne = Jsons.SelfEmployment.update(emailAddress = "a" * 2)
      val jsonTwo = Jsons.SelfEmployment.update(emailAddress = "a" * 133)
      val jsonThree = Jsons.SelfEmployment.update(emailAddress = "admin at mail.com")
      val jsonFour = Jsons.SelfEmployment.update(emailAddress = "admin@")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonOne,
        Map("/contactDetails/emailAddress" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonTwo,
        Map("/contactDetails/emailAddress" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonThree,
        Map("/contactDetails/emailAddress" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](
        jsonFour,
        Map("/contactDetails/emailAddress" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a DATE_NOT_IN_THE_PAST error when using an effective date in the future" in {
      val input =
        Jsons.SelfEmployment.update(postcode = None, country = "FR", effectiveDate = LocalDate.now.plusDays(1).toString)
      assertValidationErrorsWithCode[SelfEmploymentUpdate](input,
                                                           Map("/effectiveDate" -> Seq(ErrorCode.DATE_NOT_IN_THE_PAST)))
    }
  }
}
