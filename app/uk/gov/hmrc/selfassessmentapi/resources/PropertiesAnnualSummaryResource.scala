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
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesAnnualSummaryConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.audit.AnnualSummaryUpdate
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary, PropertiesAnnualSummary, PropertyType}
import uk.gov.hmrc.selfassessmentapi.models.{ErrorResult, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesAnnualSummaryResponse
import uk.gov.hmrc.selfassessmentapi.services.{AuditData, AuditService, AuthorisationService}

import scala.concurrent.Future


class PropertiesAnnualSummaryResource @Inject()(
                                                 override val appContext: AppContext,
                                                 override val authService: AuthorisationService,
                                                 connector: PropertiesAnnualSummaryConnector,
                                                 auditService: AuditService
                                               ) extends BaseResource {

  def updateAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.Properties, Some("annual")).async(parse.json) { implicit request =>
      validateProperty(propertyId, request.body, connector.update(nino, propertyId, taxYear, _)) map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(response) =>
          auditService.audit(makeAnnualSummaryUpdateAudit(nino, propertyId, taxYear, request.authContext, response))
          response.filter {
            case 200 => NoContent
            case 404 if response.errorCodeIs(DesErrorCode.NOT_FOUND_PROPERTY) =>
              logger.warn(s"[PropertiesAnnualSummaryResource] [updateAnnualSummary #$propertyId] - DES Returned: ${DesErrorCode.NOT_FOUND_PROPERTY} " +
                s"CorrelationId: ${correlationId(response)}")
              NotFound
          }
      } recoverWith exceptionHandling
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
          case 404 if response.errorCodeIsOneOf(DesErrorCode.NOT_FOUND_PROPERTY, DesErrorCode.NOT_FOUND_PERIOD) => {
            logger.warn(
              s"[PropertiesAnnualSummaryResource] [retrieveAnnualSummary#$propertyId]\n" +
                s"Received from DES:\n ${response.underlying.body}\n" +
                s"CorrelationId: ${correlationId(response)}")
            NotFound
          }
        }
      } recoverWith exceptionHandling
    }

  private def validateProperty(propertyId: PropertyType,
                               body: JsValue,
                               f: PropertiesAnnualSummary => Future[PropertiesAnnualSummaryResponse]): Future[Either[ErrorResult, PropertiesAnnualSummaryResponse]] =
    propertyId match {
      case PropertyType.OTHER => validate[OtherPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](body)(f)
      case PropertyType.FHL => validate[FHLPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](body)(f)
    }

  private def makeAnnualSummaryUpdateAudit(nino: Nino,
                                           id: PropertyType,
                                           taxYear: TaxYear,
                                           authCtx: AuthContext,
                                           response: PropertiesAnnualSummaryResponse)(
                                            implicit hc: HeaderCarrier,
                                            request: Request[JsValue]): AuditData[AnnualSummaryUpdate] =
    AuditData(
      detail = AnnualSummaryUpdate(
        httpStatus = response.status,
        nino = nino,
        sourceId = id.toString,
        taxYear = taxYear,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        transactionReference = response.status / 100 match {
          case 2 => response.transactionReference
          case _ => None
        },
        requestPayload = request.body,
        responsePayload = response.status match {
          case 400 => Some(response.json)
          case _ => None
        }
      ),
      transactionName = s"$id-property-annual-summary-update"
    )
}
