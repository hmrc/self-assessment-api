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

package uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers

import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.r2.selfassessmentapi.models.calculation.ApiTaxCalculation
import uk.gov.hmrc.r2.selfassessmentapi.models.des
import uk.gov.hmrc.r2.selfassessmentapi.models.des.{DesError, DesErrorCode}
import play.api.libs.json.{JsError, JsSuccess}

case class TaxCalculationResponse(underlying: HttpResponse) extends Response {
  def calcId: Option[String] = {
    (json \ "id").asOpt[String] match {
      case x @ Some(_) => x
      case None => {
        logger.warn(s"The response from DES does not match the expected format. JSON: [$json]")
        None
      }
    }
  }

  def calculation: Option[ApiTaxCalculation] = {
    (json \ "calcResult").validate[des.TaxCalculation] match {
      case JsSuccess(calcResult, _) => Some(ApiTaxCalculation.from(calcResult))
      case JsError(errors) => {
        logger.warn(s"The response from DES does not match the expected format ($errors). JSON: [$json]")
        None
      }
    }
  }

  def isInvalidCalcId: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_CALCID)

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)

  def isInvalidIdentifier: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_IDENTIFIER)

  def isInvalidRequest: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_REQUEST)
}
