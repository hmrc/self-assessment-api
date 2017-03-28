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
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors._
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.des.Properties
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesResource extends BaseController {
  private val logger = Logger(PropertiesResource.getClass)
  lazy val featureSwitch: FeatureSwitchAction = FeatureSwitchAction(SourceType.Properties)
  private val connector = PropertiesConnector

  def create(nino: Nino): Action[JsValue] = featureSwitch.asyncJsonFeatureSwitch { implicit request =>
    validate[properties.Properties, PropertiesResponse](request.body) { props =>
      connector.create(nino, props)
    } match {
      case Left(errorResult) =>
        Future.successful(handleValidationErrors(errorResult))
      case Right(response) =>
        response.map { response =>
          if (response.status == 200) Created.withHeaders(LOCATION -> response.createLocationHeader(nino))
          else if (response.status == 403) Conflict.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/uk-properties")
          else if (response.status == 400) BadRequest(Error.from(response.json))
          else if (response.status == 404) NotFound
          else unhandledResponse(response.status, logger)
        }
    }
  }

  def retrieve(nino: Nino): Action[AnyContent] = featureSwitch.asyncFeatureSwitch { implicit request =>
    connector.retrieve(nino).map { response =>
      if (response.status == 200) response.property match {
        case Some(property) => Ok(Json.toJson(property))
        case None => NotFound
      }
      else if (response.status == 404) NotFound
      else if (response.status == 400) BadRequest(Error.from(response.json))
      else unhandledResponse(response.status, logger)
    }

  }
}
