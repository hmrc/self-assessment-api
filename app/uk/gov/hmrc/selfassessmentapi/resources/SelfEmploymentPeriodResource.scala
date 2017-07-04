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
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext._
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentPeriodConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.audit.PeriodicUpdate
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{SelfEmploymentPeriod, SelfEmploymentPeriodUpdate}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentPeriodResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfEmploymentPeriodResource extends BaseResource {
  private val connector = SelfEmploymentPeriodConnector

  def createPeriod(nino: Nino, sourceId: SourceId): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments, Some("periods")).async(parse.json) { implicit request =>
      validate[SelfEmploymentPeriod, (PeriodId, SelfEmploymentPeriodResponse)](request.body) { period =>
        connector
          .create(nino, sourceId, period)
          .map((period.periodId, _))
      } map {
        case Left(errorResult) => handleValidationErrors(errorResult)
        case Right((periodId, response)) =>
          response.filter {
            case 200 =>
              auditPeriodicCreate(nino, sourceId, request.authContext, response, periodId)
              Created.withHeaders(LOCATION -> response.createLocationHeader(nino, sourceId, periodId))
            case 400 if response.isInvalidBusinessId => NotFound
            case 400 if response.isInvalidPeriod =>
              Forbidden(Json.toJson(Errors.businessError(Errors.InvalidPeriod)))
            case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
            case 400 if response.isInvalidPayload => BadRequest(Json.toJson(Errors.InvalidRequest))
            case 404 => NotFound
            case _ => unhandledResponse(response.status, logger)
          }
      }
    }

  def updatePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments, Some("periods")).async(parse.json) { implicit request =>
      periodId match {
        case Period(from, to) =>
          validate[SelfEmploymentPeriodUpdate, SelfEmploymentPeriodResponse](request.body) { period =>
            connector.update(nino, id, from, to, period)
          } map {
            case Left(errorResult) => handleValidationErrors(errorResult)
            case Right(response) =>
              response.filter {
                case 200 => NoContent
                case 404 => NotFound
                case _ => unhandledResponse(response.status, logger)
              }
          }
        case _ => Future.successful(NotFound)
      }
    }

  def retrievePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments, Some("periods")).async(parse.empty) { implicit request =>
      periodId match {
        case Period(from, to) =>
          connector.get(nino, id, from, to).map { response =>
            response.filter {
              case 200 => response.period.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
              case 400 if response.isInvalidBusinessId => NotFound
              case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
              case 404 => NotFound
              case _ => unhandledResponse(response.status, logger)
            }
          }
        case _ => Future.successful(NotFound)
      }
    }

  def retrievePeriods(nino: Nino, id: SourceId): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments, Some("periods")).async(parse.empty) { implicit request =>
      connector.getAll(nino, id).map { response =>
        response.filter {
          case 200 => Ok(Json.toJson(response.allPeriods(getMaxPeriodTimeSpan)))
          case 400 if response.isInvalidBusinessId => NotFound
          case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
          case 404 => NotFound
          case _ => unhandledResponse(response.status, logger)
        }
      }
    }

  private def auditPeriodicCreate(nino: Nino,
                                  id: SourceId,
                                  authCtx: AuthContext,
                                  response: SelfEmploymentPeriodResponse,
                                  periodId: PeriodId)(implicit hc: HeaderCarrier, request: Request[JsValue]): Unit = {
    AuditService.audit(payload = PeriodicUpdate(nino, id, periodId, authCtx.toString, response.transactionReference, request.body),
                       "self-employment-periodic-create")
  }
}
