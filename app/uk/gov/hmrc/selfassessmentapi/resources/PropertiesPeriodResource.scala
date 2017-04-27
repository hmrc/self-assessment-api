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
import uk.gov.hmrc.selfassessmentapi.models.audit.{AuditType, PeriodicUpdate}
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties._
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesPeriodResponse
import uk.gov.hmrc.selfassessmentapi.services.{AuditService, FHLPropertiesPeriodService, OtherPropertiesPeriodService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesPeriodResource extends BaseResource {

  lazy val FeatureSwitch = FeatureSwitchAction(SourceType.Properties, "periods")
  private val connector = PropertiesPeriodConnector

  def createPeriod(nino: Nino, id: PropertyType): Action[JsValue] = FeatureSwitch.async(parse.json) {
    implicit request =>
      withAuth(nino) {
        validateCreateRequest(id, nino, request) match {
          case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
          case Right(response) =>
            response.map { response =>
              response.status match {
                case 200 =>
                  auditPeriodicCreate(nino, id, response)
                  Created.withHeaders(LOCATION -> response.createLocationHeader(nino, id))
                case 400 if response.containsOverlappingPeriod => Forbidden(Error.asBusinessError(response.json))
                case 400 => BadRequest(Error.from(response.json))
                case 404 => NotFound
                case _ => unhandledResponse(response.status, logger)
              }
            }
        }
      }
  }

  def updatePeriod(nino: Nino, id: PropertyType, periodId: PeriodId): Action[JsValue] = FeatureSwitch.async(parse.json) { implicit request =>
    withAuth(nino) {
      validateUpdateRequest(id, nino, periodId, request) match {
        case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
        case Right(result) => result.map {
          case true => NoContent
          case false => NotFound
        }
      }
    }
  }

  def retrievePeriod(nino: Nino, id: PropertyType, periodId: PeriodId): Action[AnyContent] = FeatureSwitch.async { implicit request =>
    withAuth(nino) {
      connector.retrieve(nino, periodId, id).map { response =>
        response.status match {
          case 200 =>
            id match {
              case PropertyType.FHL =>
                response
                  .PropertiesPeriodResponseOps[FHL.Properties, des.properties.FHL.Properties]
                  .period
                  .map(period => Ok(Json.toJson(period)))
                  .getOrElse(NotFound)

              case PropertyType.OTHER =>
                response
                  .PropertiesPeriodResponseOps[Other.Properties, des.properties.Other.Properties]
                  .period
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

  def retrievePeriods(nino: Nino, id: PropertyType): Action[AnyContent] = FeatureSwitch.async { implicit request =>
    withAuth(nino) {
      connector.retrieveAll(nino, id).map { response =>
        response.status match {
          case 200 =>
            Ok(Json.toJson(id match {
              case PropertyType.FHL =>
                response.PropertiesPeriodResponseOps[FHL.Properties, des.properties.FHL.Properties].allPeriods
              case PropertyType.OTHER =>
                response.PropertiesPeriodResponseOps[Other.Properties, des.properties.Other.Properties].allPeriods
            }))
          case 400 => BadRequest(Error.from(response.json))
          case 404 => NotFound
          case _ => unhandledResponse(response.status, logger)
        }
      }
    }
  }

  private def validateCreateRequest(id: PropertyType, nino: Nino, request: Request[JsValue])(
    implicit hc: HeaderCarrier): Either[ErrorResult, Future[PropertiesPeriodResponse]] =
    id match {
      case PropertyType.OTHER =>
        validate[Other.Properties, PropertiesPeriodResponse](request.body)(
          PropertiesPeriodConnector[Other.Properties].create(nino, _))

      case PropertyType.FHL =>
        validate[FHL.Properties, PropertiesPeriodResponse](request.body)(
          PropertiesPeriodConnector[FHL.Properties].create(nino, _))

    }

  private def validateUpdateRequest(id: PropertyType,
                                    nino: Nino,
                                    periodId: PeriodId,
                                    request: Request[JsValue]): Either[ErrorResult, Future[Boolean]] = id match {
    case PropertyType.OTHER =>
      validate[Other.Financials, Boolean](request.body) { period =>
        OtherPropertiesPeriodService.updatePeriod(nino, periodId, period)
      }
    case PropertyType.FHL =>
      validate[FHL.Financials, Boolean](request.body) { period =>
        FHLPropertiesPeriodService.updatePeriod(nino, periodId, period)
      }
  }

  private def auditPeriodicCreate(nino: Nino, id: PropertyType, response: PropertiesPeriodResponse)
                                 (implicit hc: HeaderCarrier, request: Request[JsValue]): Unit = {
    AuditService.audit(
      payload = PeriodicUpdate(nino, id.toString, response.getPeriodId, response.transactionReference, request.body),
      s"$id-property-periodic-create")
  }
}
