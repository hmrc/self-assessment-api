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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentAnnualSummaryConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.{des, _}
import uk.gov.hmrc.selfassessmentapi.models.audit.AnnualSummaryUpdate
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentAnnualSummary
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentAnnualSummaryResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentAnnualSummaryResource extends BaseResource {
  private val connector = SelfEmploymentAnnualSummaryConnector

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments, Some("annual")).async(parse.json) { implicit request =>
      validate[SelfEmploymentAnnualSummary, SelfEmploymentAnnualSummaryResponse](request.body) { summary =>
        connector.update(nino, id, taxYear, des.selfemployment.SelfEmploymentAnnualSummary.from(summary))
      } map {
        case Left(errorResult) => handleValidationErrors(errorResult)
        case Right(response) =>
          response.filter {
            case 200 =>
              auditAnnualSummaryUpdate(nino, id, taxYear, request.authContext, response)
              NoContent
            case 404 => NotFound
            case _ => Error.from2(response.json)
          }
      }
    }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments, Some("annual")).async(parse.empty) { implicit request =>
      connector.get(nino, id, taxYear).map { response =>
        response.filter {
          case 200 => response.annualSummary.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
          case 404 => NotFound
          case _ => unhandledResponse(response.status, logger)
        }
      }
    }

  private def auditAnnualSummaryUpdate(
      nino: Nino,
      id: SourceId,
      taxYear: TaxYear,
      authCtx: AuthContext,
      response: SelfEmploymentAnnualSummaryResponse)(implicit hc: HeaderCarrier, request: Request[JsValue]) = {
    AuditService.audit(AnnualSummaryUpdate(nino, id, taxYear, authCtx.toString, response.transactionReference, request.body),
                       "self-employment-annual-summary-update")
  }
}
