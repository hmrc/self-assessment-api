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

package uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{Adjustments, Allowances}

case class UKProperty(id: Option[SourceId] = None,
                      allowances: Option[Allowances] = None,
                      adjustments: Option[Adjustments] = None,
                      rentARoomRelief: Option[BigDecimal] = None)


object UKProperty extends JsonMarshaller[UKProperty] {

  implicit val propertyLocationTypes = EnumJson.enumFormat(PropertyLocationType, Some("Furnished holiday lettings property location type is invalid"))

  implicit val writes = Json.writes[UKProperty]

  implicit val reads: Reads[UKProperty] = (
    Reads.pure(None) and
      (__ \ "allowances").readNullable[Allowances] and
      (__ \ "adjustments").readNullable[Adjustments] and
      (__ \ "rentARoomRelief").readNullable[BigDecimal](positiveAmountValidator("rentARoomRelief"))
    ) (UKProperty.apply _)


  def example(id: Option[SourceId]): UKProperty = UKProperty(
    id,
    Some(Allowances(Some(BigDecimal(1000.00)), Some(BigDecimal(600.00)), Some(BigDecimal(50.00)), Some(BigDecimal(3399.99)))),
    Some(Adjustments(Some(BigDecimal(250.00)))),
    Some(BigDecimal(7500.00)))
}
