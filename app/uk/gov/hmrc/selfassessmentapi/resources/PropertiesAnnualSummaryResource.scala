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
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesAnnualSummaryConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.audit.AnnualSummaryUpdate
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary, PropertiesAnnualSummary, PropertyType}
import uk.gov.hmrc.selfassessmentapi.models.{SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesAnnualSummaryResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesAnnualSummaryResource extends BaseResource {
  private val connector = PropertiesAnnualSummaryConnector

  def updateAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.Properties, Some("annual")).async(parse.json) { implicit request =>
      validateProperty(propertyId, request.body, connector.update(nino, propertyId, taxYear, _)) map {
        case Left(errorResult) => handleValidationErrors(errorResult)
        case Right(response) =>
          response.filter {
            case 200 =>
              auditAnnualSummaryUpdate(nino, propertyId, taxYear, request.authContext, response)
              NoContent
            case 404 => NotFound
            case _ => Error.from2(response.json)
          }
      }
    }

  def retrieveAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("annual")).async { implicit request =>
      connector.get(nino, propertyId, taxYear).map { response =>
        response.filter {
          case 200 =>
            response.annualSummary match {
              case Some(summary) =>
                summary match {
                  case other: OtherPropertiesAnnualSummary => Ok(Json.toJson(other))
                  case fhl: FHLPropertiesAnnualSummary => Ok(Json.toJson(fhl))
                }
              case None => NotFound
            }
          case 404 => NotFound
          case 400 => BadRequest(Error.from(response.json))
          case _ => unhandledResponse(response.status, logger)
        }
      }
    }

  private def validateProperty(propertyId: PropertyType,
                               body: JsValue,
                               f: PropertiesAnnualSummary => Future[PropertiesAnnualSummaryResponse]) =
    propertyId match {
      case PropertyType.OTHER => validate[OtherPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](body)(f)
      case PropertyType.FHL => validate[FHLPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](body)(f)
    }

  private def auditAnnualSummaryUpdate(
      nino: Nino,
      id: PropertyType,
      taxYear: TaxYear,
      authCtx: AuthContext,
      response: PropertiesAnnualSummaryResponse)(implicit hc: HeaderCarrier, request: Request[JsValue]) = {
    AuditService.audit(AnnualSummaryUpdate(nino, id.toString, taxYear, authCtx.toString, response.transactionReference, request.body),
                       s"$id-property-annual-summary-update")
  }
}
