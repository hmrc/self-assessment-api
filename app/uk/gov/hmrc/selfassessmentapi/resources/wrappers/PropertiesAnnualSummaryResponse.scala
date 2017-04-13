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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.des
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType._
import uk.gov.hmrc.selfassessmentapi.models.properties.{OtherPropertiesAnnualSummary, FHLPropertiesAnnualSummary, PropertiesAnnualSummary, PropertyType}

class PropertiesAnnualSummaryResponse(propertyType: PropertyType, underlying: HttpResponse) {
  private val logger = Logger(classOf[PropertiesAnnualSummaryResponse])

  val status: Int = underlying.status

  def json: JsValue = underlying.json

  def annualSummary: Option[PropertiesAnnualSummary] = propertyType match {
    case PropertyType.OTHER =>
      json.asOpt[des.OtherPropertiesAnnualSummaryDetails] match {
        case Some(other) => Some(OtherPropertiesAnnualSummary.from(des.OtherPropertiesAnnualSummary(Some(other))))
        case None => {
          logger.error(s"The response from DES for $propertyType does not match the expected properties annual summary format.")
          None
        }
      }
    case PropertyType.FHL =>
      json.asOpt[des.FHLPropertiesAnnualSummaryDetails] match {
        case Some(fhl) => Some(FHLPropertiesAnnualSummary.from(des.FHLPropertiesAnnualSummary(Some(fhl))))
        case None => {
          logger.error(s"The response from DES for $propertyType does not match the expected properties annual summary format.")
          None
        }
      }
  }
}


object PropertiesAnnualSummaryResponse {
  def apply(propertyType: PropertyType, httpResponse: HttpResponse): PropertiesAnnualSummaryResponse =
    new PropertiesAnnualSummaryResponse(propertyType, httpResponse)
}
