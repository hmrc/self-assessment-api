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

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.selfassessmentapi.resources.{JsonSpec, Jsons}

class SelfEmploymentUpdateSpec extends JsonSpec {
  "SelfEmploymentUpdate JSON" should {
    "return a error when providing an empty SelfEmploymentUpdate body" in {
      assertValidationErrorsWithMessage[SelfEmploymentUpdate](Json.obj(),
        Map("/accountingPeriod" -> Seq("error.path.missing"),
          "/accountingType" -> Seq("error.path.missing"),
          "/commencementDate" -> Seq("error.path.missing"),
          "/tradingName" -> Seq("error.path.missing"),
          "/businessDescription" -> Seq("error.path.missing"),
          "/businessAddressLineOne" -> Seq("error.path.missing"),
          "/businessCountry" -> Seq("error.path.missing"),
          "/paperless" -> Seq("error.path.missing"),
          "/seasonal" -> Seq("error.path.missing")))
    }

    "return a error when providing a trading name that is not between 1 and 105 in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(tradingName = "")
      val jsonTwo = Jsons.SelfEmployment.update(tradingName = "a" * 106)
      val jsonThree = Jsons.SelfEmployment.update(tradingName = "currency £$ inc")
      val jsonFour = Jsons.SelfEmployment.update(tradingName = "test (@ ^ *) name")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonFour, Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing an empty business description" in {
      val json = Jsons.SelfEmployment.update(businessDescription = "")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](json, Map("/businessDescription" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a business description that does not conform to the UK SIC 2007 classifications" in {
      val json = Jsons.SelfEmployment.update(businessDescription = "silly-business")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](json, Map("/businessDescription" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a first address line that is not between 1 and 35 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(businessAddressLineOne = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessAddressLineOne = "a" * 36)
      val jsonThree = Jsons.SelfEmployment.update(businessAddressLineOne = "Line 1(£$)" )

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessAddressLineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessAddressLineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/businessAddressLineOne" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a second address line that is not between 1 and 35 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(businessAddressLineTwo = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessAddressLineTwo = "a" * 36)
      val jsonThree = Jsons.SelfEmployment.update(businessAddressLineTwo = "Line 1(£$)" )

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessAddressLineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessAddressLineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/businessAddressLineTwo" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a third address line that is not between 1 and 35 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(businessAddressLineThree = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessAddressLineThree = "a" * 36)
      val jsonThree = Jsons.SelfEmployment.update(businessAddressLineThree = "Line 1(£$)" )

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessAddressLineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessAddressLineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/businessAddressLineThree" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a fourth address line that is not between 1 and 35 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(businessAddressLineFour = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessAddressLineFour = "a" * 36)
      val jsonThree = Jsons.SelfEmployment.update(businessAddressLineFour = "Line 1(£$)" )

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessAddressLineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessAddressLineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/businessAddressLineFour" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a postcode that is not between 1 and 10 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(businessPostcode = Some(""))
      val jsonTwo = Jsons.SelfEmployment.update(businessPostcode = Some("a" * 11))
      val jsonThree = Jsons.SelfEmployment.update(businessPostcode = Some("1(£$)") )

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessPostcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessPostcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/businessPostcode" -> Seq(ErrorCode.INVALID_POSTCODE)))
    }

    "return a error when country is GB and postcode is not provided" in {
      val jsonOne = Jsons.SelfEmployment.update(businessPostcode =  None)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("" -> Seq(ErrorCode.MANDATORY_FIELD_MISSING)))
    }

    "pass when country is FR and postcode is not provided" in {
      val jsonOne = Jsons.SelfEmployment.update(businessPostcode =  None, businessCountry = "FR")

      assertJsonValidationPasses[SelfEmploymentUpdate](jsonOne)
    }

    "return a error when providing a country that is not 2 characters in length" in {
      val jsonOne = Jsons.SelfEmployment.update(businessCountry = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessCountry = "Great Britain")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessCountry" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessCountry" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a contactPrimaryPhoneNumber that is not between 1 and 24 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(contactPrimaryPhoneNumber = "")
      val jsonTwo = Jsons.SelfEmployment.update(contactPrimaryPhoneNumber = "a" * 25)
      val jsonThree = Jsons.SelfEmployment.update(contactPrimaryPhoneNumber = "1(£$)" )

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/contactDetails/contactPrimaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/contactDetails/contactPrimaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/contactDetails/contactPrimaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a contactSecondaryPhoneNumber that is not between 1 and 24 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(contactSecondaryPhoneNumber = "")
      val jsonTwo = Jsons.SelfEmployment.update(contactSecondaryPhoneNumber = "a" * 25)
      val jsonThree = Jsons.SelfEmployment.update(contactSecondaryPhoneNumber = "1(£$)" )

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/contactDetails/contactSecondaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/contactDetails/contactSecondaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/contactDetails/contactSecondaryPhoneNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a contactFaxNumber that is not between 1 and 24 characters in length and does not have the valid format" in {
      val jsonOne = Jsons.SelfEmployment.update(contactFaxNumber = "")
      val jsonTwo = Jsons.SelfEmployment.update(contactFaxNumber = "a" * 25)
      val jsonThree = Jsons.SelfEmployment.update(contactFaxNumber = "1(£$)" )

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/contactDetails/contactFaxNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/contactDetails/contactFaxNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonThree, Map("/contactDetails/contactFaxNumber" -> Seq(ErrorCode.INVALID_FIELD_FORMAT)))
    }

    "return a error when providing a contactEmailAddress that is not between 3 and 132" in {
      val jsonOne = Jsons.SelfEmployment.update(contactEmailAddress = "a" * 2)
      val jsonTwo = Jsons.SelfEmployment.update(contactEmailAddress = "a" * 133)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/contactDetails/contactEmailAddress" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/contactDetails/contactEmailAddress" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }
  }
}
