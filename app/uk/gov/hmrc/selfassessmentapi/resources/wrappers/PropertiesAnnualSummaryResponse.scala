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

import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.des
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType._
import uk.gov.hmrc.selfassessmentapi.models.properties.{
  FHLPropertiesAnnualSummary,
  OtherPropertiesAnnualSummary,
  PropertiesAnnualSummary,
  PropertyType
}

case class PropertiesAnnualSummaryResponse(propertyType: PropertyType, underlying: HttpResponse) extends Response {
  def annualSummary: Option[PropertiesAnnualSummary] = propertyType match {
    case PropertyType.OTHER =>
      json.asOpt[des.OtherPropertiesAnnualSummary] match {
        case Some(other) =>
          Some(
            OtherPropertiesAnnualSummary.from(
              des.OtherPropertiesAnnualSummary(other.annualAllowances, other.annualAdjustments)))
        case None => {
          logger.error(
            s"The response from DES for $propertyType does not match the expected properties annual summary format.")
          None
        }
      }
    case PropertyType.FHL =>
      json.asOpt[des.FHLPropertiesAnnualSummary] match {
        case Some(fhl) =>
          Some(
            FHLPropertiesAnnualSummary.from(
              des.FHLPropertiesAnnualSummary(fhl.annualAllowances, fhl.annualAdjustments)))
        case None => {
          logger.error(
            s"The response from DES for $propertyType does not match the expected properties annual summary format.")
          None
        }
      }
  }

  def transactionReference: Option[String] = {
    (json \ "transactionReference").asOpt[String] match {
      case x @ Some(_) => x
      case None =>
        logger.error("The 'transactionReference' field was not found in the response from DES")
        None
    }
  }
}