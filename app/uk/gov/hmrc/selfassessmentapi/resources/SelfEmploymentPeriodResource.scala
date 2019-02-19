/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.domain.Nino
//import uk.gov.hmrc.selfassessmentapi.config.AppContext._
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentPeriodConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.audit.PeriodicUpdate
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{SelfEmploymentPeriod, SelfEmploymentPeriodUpdate}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentPeriodResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditService
import uk.gov.hmrc.selfassessmentapi.services.{AuditData, AuthorisationService}

import scala.concurrent.Future

class SelfEmploymentPeriodResource @Inject()(
                                              override val appContext: AppContext,
                                              override val authService: AuthorisationService,
                                              connector: SelfEmploymentPeriodConnector,
                                              auditService: AuditService
                                            ) extends BaseResource {

  def createPeriod(nino: Nino, sourceId: SourceId): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments, Some("periods")).async(parse.json) { implicit request =>
      validate[SelfEmploymentPeriod, (PeriodId, SelfEmploymentPeriodResponse)](request.body) { period =>
        connector
          .create(nino, sourceId, period)
          .map((period.periodId, _))
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right((periodId, response)) =>
          auditService.audit(makePeriodCreateAudit(nino, sourceId, request.authContext, response, periodId))
          response.filter {
            case 200 =>
              Created.withHeaders(LOCATION -> response.createLocationHeader(nino, sourceId, periodId))
          }
      } recoverWith exceptionHandling
    }

  def updatePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments, Some("periods")).async(parse.json) { implicit request =>
      periodId match {
        case Period(from, to) =>
          validate[SelfEmploymentPeriodUpdate, SelfEmploymentPeriodResponse](request.body) { period =>
            connector.update(nino, id, from, to, period)
          } map {
            case Left(errorResult) => handleErrors(errorResult)
            case Right(response) =>
              auditService.audit(makePeriodUpdateAudit(nino, id, periodId, request.authContext, response))
              response.filter {
                case 200 => NoContent
              }
          } recoverWith exceptionHandling
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
            }
          } recoverWith exceptionHandling
        case _ => Future.successful(NotFound)
      }
    }

  def retrievePeriods(nino: Nino, id: SourceId): Action[Unit] =
    APIAction(nino, SourceType.SelfEmployments, Some("periods")).async(parse.empty) { implicit request =>
      connector.getAll(nino, id).map { response =>
        response.filter {
          case 200 =>
            response.allPeriods.map(seq => Ok(Json.toJson(seq))).getOrElse(InternalServerError)
        }
      } recoverWith exceptionHandling
    }

  private def makePeriodCreateAudit(
                                     nino: Nino,
                                     id: SourceId,
                                     authCtx: AuthContext,
                                     response: SelfEmploymentPeriodResponse,
                                     periodId: PeriodId)(implicit hc: HeaderCarrier, request: Request[JsValue]): AuditData[PeriodicUpdate] =
    AuditData(
      detail = PeriodicUpdate(
        auditType = "submitPeriodicUpdate",
        httpStatus = response.status,
        nino = nino,
        sourceId = id,
        periodId = periodId,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        transactionReference = response.status / 100 match {
          case 2 => response.transactionReference
          case _ => None
        },
        requestPayload = request.body,
        responsePayload = response.status match {
          case 200 | 400 => Some(response.json)
          case _ => None
        }
      ),
      transactionName = "self-employment-periodic-create"
    )

  private def makePeriodUpdateAudit(nino: Nino,
                                    id: SourceId,
                                    periodId: PeriodId,
                                    authCtx: AuthContext,
                                    response: SelfEmploymentPeriodResponse)(
                                     implicit hc: HeaderCarrier,
                                     request: Request[JsValue]): AuditData[PeriodicUpdate] =
    AuditData(
      detail = PeriodicUpdate(
        auditType = "amendPeriodicUpdate",
        httpStatus = response.status,
        nino = nino,
        sourceId = id,
        periodId = periodId,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        transactionReference = None,
        requestPayload = request.body,
        responsePayload = response.status match {
          case 400 => Some(response.json)
          case _ => None
        }
      ),
      transactionName = "self-employment-periodic-update"
    )
}
