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

import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentPeriodConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.des.Financials
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{SelfEmploymentPeriod, SelfEmploymentPeriodicData}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentPeriodResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfEmploymentPeriodResource extends BaseController {
  private val logger = Logger(SelfEmploymentPeriodResource.getClass)
  private lazy val featureSwitch = FeatureSwitchAction(SourceType.SelfEmployments, "periods")
  private val connector = SelfEmploymentPeriodConnector

  def createPeriod(nino: Nino, sourceId: SourceId): Action[JsValue] = featureSwitch.asyncJsonFeatureSwitch { implicit request =>
    validate[SelfEmploymentPeriod, SelfEmploymentPeriodResponse](request.body) { period =>
      connector.create(nino, sourceId, period)
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(result) => result.map { response =>
        if (response.status == 200) Created.withHeaders(LOCATION -> response.createLocationHeader(nino, sourceId).getOrElse(""))
        else if (response.status == 404) NotFound
        else if (response.containsOverlappingPeriod) Forbidden(Error.asBusinessError(response.json))
        else if (response.status == 400) BadRequest(Error.from(response.json))
        else unhandledResponse(response.status, logger)
      }
    }
  }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def updatePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Action[JsValue] = featureSwitch.asyncJsonFeatureSwitch { implicit request =>
    validate[SelfEmploymentPeriodicData, SelfEmploymentPeriodResponse](request.body) { period =>
      connector.update(nino, id, periodId, period)
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(result) => result.map { response =>
        if (response.status == 204) NoContent
        else if (response.status == 404) NotFound
        else if (response.status == 400) BadRequest(Error.from(response.json))
        else unhandledResponse(response.status, logger)
      }
    }
  }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def retrievePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Action[AnyContent] = featureSwitch.asyncFeatureSwitch { implicit request =>
    connector.get(nino, id, periodId).map { response =>
      if (response.status == 200) response.period match {
        case Some(period) => Ok(Json.toJson(period))
        case None => NotFound
      }
      else if (response.status == 404) NotFound
      else if (response.status == 400) BadRequest(Error.from(response.json))
      else unhandledResponse(response.status, logger)
    }
  }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def retrievePeriods(nino: Nino, id: SourceId): Action[AnyContent] = featureSwitch.asyncFeatureSwitch { implicit request =>
    connector.getAll(nino, id).map { response =>
      if (response.status == 200) Ok(Json.toJson(response.allPeriods))
      else if (response.status == 404) NotFound
      else if (response.status == 400) BadRequest(Error.from(response.json))
      else unhandledResponse(response.status, logger)
    }
  }
}
