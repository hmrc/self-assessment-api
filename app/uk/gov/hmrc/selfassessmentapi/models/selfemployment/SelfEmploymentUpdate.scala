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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models._

case class SelfEmploymentUpdate(tradingName: String,
                                businessDescription: String,
                                businessAddressLineOne: String,
                                businessAddressLineTwo: Option[String],
                                businessAddressLineThree: Option[String],
                                businessAddressLineFour: Option[String],
                                businessPostcode: String)

object SelfEmploymentUpdate {

  implicit val writes: Writes[SelfEmploymentUpdate] = Json.writes[SelfEmploymentUpdate]

  implicit val reads: Reads[SelfEmploymentUpdate] = (
    (__ \ "tradingName").read[String](lengthIsBetween(1, 105)) and
      (__ \ "businessDescription").read[String] and
      (__ \ "businessAddressLineOne").read[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineTwo").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineThree").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessAddressLineFour").readNullable[String](lengthIsBetween(1, 35)) and
      (__ \ "businessPostcode").read[String](lengthIsBetween(1, 10))
    ) (SelfEmploymentUpdate.apply _)
}
