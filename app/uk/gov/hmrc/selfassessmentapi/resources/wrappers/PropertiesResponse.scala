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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.des
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.selfassessmentapi.models.properties.Properties
import uk.gov.hmrc.http.HttpResponse

case class PropertiesResponse(underlying: HttpResponse) extends Response { self =>
  def createLocationHeader(nino: Nino): String = s"/self-assessment/ni/$nino/uk-properties"

  def property: Option[Properties] = {
    (json \ "propertyData").asOpt[des.properties.Properties] match {
      case Some(property) =>
        Some(Properties.from(property))
      case None =>
        logger.error(s"The response from DES does not match the expected format. JSON: [$json]")
        None
    }
  }

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)
}
