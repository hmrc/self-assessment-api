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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType

class PropertiesPeriodResponse(underlying: HttpResponse) {

  private val logger: Logger = Logger(classOf[PropertiesPeriodResponse])

  val status: Int = underlying.status

  def json: JsValue = underlying.json

  def createLocationHeader(nino: Nino, id: PropertyType): String =
    s"/self-assessment/ni/$nino/uk-properties/$id/periods"

  def containsOverlappingPeriod: Boolean = {
    json.asOpt[DesError] match {
      case Some(err) => err.code == DesErrorCode.INVALID_PERIOD
      case None =>
        logger.error("The response from DES does not match the expected error format.")
        false
    }
  }

}

object PropertiesPeriodResponse {
  def apply(response: HttpResponse): PropertiesPeriodResponse = new PropertiesPeriodResponse(response)
}
