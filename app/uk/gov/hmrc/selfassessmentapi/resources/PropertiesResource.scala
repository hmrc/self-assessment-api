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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.Errors._
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.services.PropertiesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesResource extends BaseResource {

  private lazy val FeatureSwitch: FeatureSwitchAction = FeatureSwitchAction(SourceType.Properties)

  private val service = PropertiesService()

  def create(nino: Nino): Action[JsValue] = FeatureSwitch.async(parse.json) { implicit request =>
    withAuth(nino) {
      validate[properties.Properties, Either[Error, Boolean]](request.body) {
        service.create(nino, _)
      } match {
        case Left(errorResult) =>
          Future.successful(handleValidationErrors(errorResult))
        case Right(result) => result.map {
          case Right(successful) =>
            if (successful) Created.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/uk-properties")
            else InternalServerError
          case Left(_) => Conflict.withHeaders(LOCATION ->  s"/self-assessment/ni/$nino/uk-properties")
        }
      }
    }
  }

  def retrieve(nino: Nino): Action[AnyContent] = FeatureSwitch.async { implicit headers =>
    withAuth(nino) {
      service.retrieve(nino).map {
        case Some(properties) => Ok(Json.toJson(properties))
        case None => NotFound
      }
    }
  }
}
