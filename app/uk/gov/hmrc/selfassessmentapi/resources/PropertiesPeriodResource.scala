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
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesPeriodConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.audit.PeriodicUpdate
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties._
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{PropertiesPeriodResponse, ResponseMapper}
import uk.gov.hmrc.selfassessmentapi.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesPeriodResource extends BaseResource {

  lazy val FeatureSwitch = FeatureSwitchAction(SourceType.Properties, "periods")
  private val connector = PropertiesPeriodConnector

  def createPeriod(nino: Nino, id: PropertyType): Action[JsValue] =
    FeatureSwitch.async(parse.json) { implicit request =>
      withAuth(nino) { implicit context =>
        validateCreateRequest(id, nino, request) map {
          case Left(errorResult) => handleValidationErrors(errorResult)
          case Right((periodId, response)) =>
            response.filter {
              case 200 =>
                auditPeriodicCreate(nino, id, response, periodId)
                Created.withHeaders(LOCATION -> response.createLocationHeader(nino, id, periodId))
              case 400 if response.isInvalidPeriod =>
                Forbidden(Json.toJson(Errors.businessError(Errors.InvalidPeriod)))
              case 400 if response.isInvalidPayload => BadRequest(Json.toJson(Errors.InvalidRequest))
              case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
              case 404 | 403 => NotFound
              case _ => unhandledResponse(response.status, logger)
            }
        }
      }
    }

  def updatePeriod(nino: Nino, id: PropertyType, periodId: PeriodId): Action[JsValue] =
    FeatureSwitch.async(parse.json) { implicit request =>
      withAuth(nino) { implicit context =>
        validateUpdateRequest(id, nino, periodId, request) map {
          case Left(errorResult) => handleValidationErrors(errorResult)
          case Right(response) =>
            response.filter {
              case 204 => NoContent
              case 404 => NotFound
              case _ => unhandledResponse(response.status, logger)
            }
        }
      }
    }

  def retrievePeriod(nino: Nino, id: PropertyType, periodId: PeriodId): Action[AnyContent] =
    FeatureSwitch.async { implicit request =>
      withAuth(nino) { implicit context =>
        connector.retrieve(nino, periodId, id).map { response =>
          response.filter {
            case 200 =>
              id match {
                case PropertyType.FHL =>
                  ResponseMapper[FHL.Properties, des.properties.FHL.Properties]
                    .period(response)
                    .map(period => Ok(Json.toJson(period)))
                    .getOrElse(NotFound)
                case PropertyType.OTHER =>
                  ResponseMapper[Other.Properties, des.properties.Other.Properties]
                    .period(response)
                    .map(period => Ok(Json.toJson(period)))
                    .getOrElse(NotFound)
              }
            case 400 => BadRequest(Error.from(response.json))
            case 404 => NotFound
            case _ => unhandledResponse(response.status, logger)
          }
        }
      }
    }

  def retrievePeriods(nino: Nino, id: PropertyType): Action[AnyContent] =
    FeatureSwitch.async { implicit request =>
      withAuth(nino) { implicit context =>
        connector.retrieveAll(nino, id).map { response =>
          response.filter {
            case 200 =>
              Ok(Json.toJson(id match {
                case PropertyType.FHL =>
                  ResponseMapper[FHL.Properties, des.properties.FHL.Properties].allPeriods(response)
                case PropertyType.OTHER =>
                  ResponseMapper[Other.Properties, des.properties.Other.Properties].allPeriods(response)
              }))
            case 400 => BadRequest(Error.from(response.json))
            case 404 => NotFound
            case _ => unhandledResponse(response.status, logger)
          }
        }
      }
    }

  private def validateCreateRequest(id: PropertyType, nino: Nino, request: Request[JsValue])(
      implicit hc: HeaderCarrier): Future[Either[ErrorResult, (PeriodId, PropertiesPeriodResponse)]] =
    id match {
      case PropertyType.OTHER =>
        validate[Other.Properties, (PeriodId, PropertiesPeriodResponse)](request.body) { period =>
          PropertiesPeriodConnector[Other.Properties, Other.Financials]
            .create(nino, period)
            .map((period.createPeriodId, _))
        }

      case PropertyType.FHL =>
        validate[FHL.Properties, (PeriodId, PropertiesPeriodResponse)](request.body) { period =>
          PropertiesPeriodConnector[FHL.Properties, FHL.Financials]
            .create(nino, period)
            .map((period.createPeriodId, _))
        }
    }

  private def validateUpdateRequest(id: PropertyType, nino: Nino, periodId: PeriodId, request: Request[JsValue])(
      implicit hc: HeaderCarrier): Future[Either[ErrorResult, PropertiesPeriodResponse]] =
    id match {
      case PropertyType.OTHER =>
        validate[Other.Financials, PropertiesPeriodResponse](request.body)(
          PropertiesPeriodConnector[Other.Properties, Other.Financials].update(nino, id, periodId, _))
      case PropertyType.FHL =>
        validate[FHL.Financials, PropertiesPeriodResponse](request.body)(
          PropertiesPeriodConnector[FHL.Properties, FHL.Financials].update(nino, id, periodId, _))
    }

  private def auditPeriodicCreate(nino: Nino,
                                  id: PropertyType,
                                  response: PropertiesPeriodResponse,
                                  periodId: PeriodId)(implicit hc: HeaderCarrier, request: Request[JsValue]): Unit = {
    AuditService.audit(payload =
                         PeriodicUpdate(nino, id.toString, periodId, response.transactionReference, request.body),
                       s"$id-property-periodic-create")
  }
}
