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

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, Writes, __}
import uk.gov.hmrc.selfassessmentapi.models._


case class Address(lineOne: String,
                   lineTwo: Option[String],
                   lineThree: Option[String],
                   lineFour: Option[String],
                   postcode: Option[String],
                   country: String)

object Address {
  implicit val writes: Writes[Address] = Json.writes[Address]

  implicit val reads: Reads[Address] = (
    (__ \ "lineOne").read[String](regexValidator("lineOne", stringRegex(35))) and
      (__ \ "lineTwo").readNullable[String](regexValidator("lineTwo", stringRegex(35))) and
      (__ \ "lineThree").readNullable[String](regexValidator("lineThree", stringRegex(35))) and
      (__ \ "lineFour").readNullable[String](regexValidator("lineFour", stringRegex(35))) and
      (__ \ "postcode").readNullable[String](postcodeValidator) and
      (__ \ "country").read[String](lengthIs(2))
    ) (Address.apply _).filter(
    ValidationError(
      "postcode mandatory when country = GB",
      ErrorCode.MANDATORY_FIELD_MISSING)) { address =>
    if (address.country == "GB") address.postcode.isDefined
    else true
  }

  def from(address: des.selfemployment.SelfEmploymentAddress): Address = {
    Address(address.addressLine1,
      address.addressLine2,
      address.addressLine3,
      address.addressLine4,
      address.postalCode,
      address.countryCode)
  }
}