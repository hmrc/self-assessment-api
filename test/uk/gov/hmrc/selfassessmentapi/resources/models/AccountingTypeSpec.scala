/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources.models

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.Properties

class AccountingTypeSpec extends JsonSpec {
  "Properties" should {
    "round trip" in {
      val properties = Properties(AccountingType.CASH)
      roundTripJson(properties)
    }
  }

  "validate" should {

  def properties(propertiesType: String = "FHL", accPeriodStart: String = "2017-04-06",
              accPeriodEnd: String = "2018-04-05", accountingType: String = "CASH"): JsValue = {
      Json.parse(
        s"""
           |{
           |  "propertiesType": "$propertiesType",
           |  "accountingPeriod": {
           |    "start": "$accPeriodStart",
           |    "end": "$accPeriodEnd"
           |  },
           |  "accountingType": "$accountingType"
           |}
         """.stripMargin)
    }

    "return INVALID_VALUE when provided with an invalid accounting type" in {
      val json = properties(accountingType = "INVALID")

      assertValidationErrorsWithCode[Properties](json, Map("/accountingType" -> ErrorCode.INVALID_VALUE))
    }

    "pass when provided all valid accounting types" in {
      Seq(properties(accountingType = "CASH"), properties(accountingType = "ACCRUAL"))
        .foreach(assertValidationPasses(_))
    }
  }
}
