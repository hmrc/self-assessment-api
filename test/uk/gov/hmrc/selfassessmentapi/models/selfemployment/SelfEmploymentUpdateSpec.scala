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
import uk.gov.hmrc.selfassessmentapi.resources.{JsonSpec, Jsons}
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode

class SelfEmploymentUpdateSpec extends JsonSpec {
  "SelfEmploymentUpdate JSON" should {
    "return a error when providing an empty SelfEmploymentUpdate body" in {
      assertValidationErrorsWithMessage[SelfEmploymentUpdate](Json.obj(),
        Map("/tradingName" -> Seq("error.path.missing"),
          "/businessDescription" -> Seq("error.path.missing"),
          "/businessAddressLineOne" -> Seq("error.path.missing"),
          "/businessPostcode" -> Seq("error.path.missing")))
    }

    "return a error when providing a trading name that is not between 1 and 105 characters in length" in {
      val jsonOne = Jsons.SelfEmployment.update(tradingName = "")
      val jsonTwo = Jsons.SelfEmployment.update(tradingName = "a" * 106)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/tradingName" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing an empty business description" in {
      val json = Jsons.SelfEmployment.update(businessDescription = "")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](json, Map("/businessDescription" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a business description that does not conform to the UK SIC 2007 classifications" in {
      val json = Jsons.SelfEmployment.update(businessDescription = "silly-business")

      assertValidationErrorsWithCode[SelfEmploymentUpdate](json, Map("/businessDescription" -> Seq(ErrorCode.INVALID_BUSINESS_DESCRIPTION)))
    }

    "return a error when providing a first address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment.update(businessAddressLineOne = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessAddressLineOne = "a" * 36)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessAddressLineOne" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessAddressLineOne" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a second address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment.update(businessAddressLineTwo = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessAddressLineTwo = "a" * 36)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessAddressLineTwo" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessAddressLineTwo" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a third address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment.update(businessAddressLineThree = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessAddressLineThree = "a" * 36)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessAddressLineThree" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessAddressLineThree" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a fourth address line that is not between 1 and 35 characters in length" in {
      val jsonOne = Jsons.SelfEmployment.update(businessAddressLineFour = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessAddressLineFour = "a" * 36)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessAddressLineFour" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessAddressLineFour" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }

    "return a error when providing a postcode that is not between 1 and 10 characters in length" in {
      val jsonOne = Jsons.SelfEmployment.update(businessPostcode = "")
      val jsonTwo = Jsons.SelfEmployment.update(businessPostcode = "a" * 11)

      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonOne, Map("/businessPostcode" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
      assertValidationErrorsWithCode[SelfEmploymentUpdate](jsonTwo, Map("/businessPostcode" -> Seq(ErrorCode.INVALID_FIELD_LENGTH)))
    }
  }
}
