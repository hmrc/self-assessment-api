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

package uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson.enumFormat

object PropertyLocationType extends Enumeration {
  type PropertyLocationType = Value
  val UK, EEA = Value
  implicit val format = enumFormat(PropertyLocationType, Some("Furnished holiday lettings property location type is invalid"))
}

case class FurnishedHolidayLetting(id: Option[SourceId] = None,
                                   propertyLocation: PropertyLocationType,
                                   allowances: Option[Allowances] = None,
                                   adjustments: Option[Adjustments] = None)


object FurnishedHolidayLetting extends JsonMarshaller[FurnishedHolidayLetting]{

  implicit val writes = Json.writes[FurnishedHolidayLetting]

  implicit val reads: Reads[FurnishedHolidayLetting] = (
    Reads.pure(None) and
      (__ \ "propertyLocation").read[PropertyLocationType] and
      (__ \ "allowances").readNullable[Allowances] and
      (__ \ "adjustments").readNullable[Adjustments]
    ) (FurnishedHolidayLetting.apply _)


  override def example(id: Option[SourceId]): FurnishedHolidayLetting = {

    FurnishedHolidayLetting(
      id, PropertyLocationType.UK,
      Some(Allowances(Some(BigDecimal(1000.00)))),
      Some(Adjustments(Some(BigDecimal(500.00)))))
  }
}
