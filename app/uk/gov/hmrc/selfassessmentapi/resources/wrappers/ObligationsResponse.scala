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
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.selfassessmentapi.models.{Obligation, Obligations, SourceId, des}

case class ObligationsResponse(underlying: HttpResponse) extends ResponseFilter {


  val status: Int = underlying.status
  private val logger: Logger = Logger(classOf[ObligationsResponse])

  def obligations(incomeSourceType: String, id: Option[SourceId] = None): Option[Obligations] = {

    val desObligations = json.asOpt[des.Obligations]

    var errorMessage = "The response from DES does not match the expected obligations format."

    def noneFound: Option[Obligations] = {
      logger.error(errorMessage)
      None
    }

    def oneFound(obligation: des.Obligations): Option[Obligations] = {
      errorMessage = "Obligation for source id and/or business type was not found."
      obligation.obligations.find(o => o.id == id.getOrElse(o.id) && o.`type` == incomeSourceType).fold(noneFound) {
        desObligation =>
          Some(Obligations(for {
            details <- desObligation.details
          } yield Obligation.from(details)))
      }
    }

    desObligations.fold(noneFound)(oneFound)
  }

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)

  def json: JsValue = underlying.json
}
