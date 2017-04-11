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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentObligationsConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models._

import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentObligationsResource extends BaseController {
  private val logger = Logger(SelfEmploymentObligationsResource.getClass)
  private lazy val featureSwitch = FeatureSwitchAction(SourceType.SelfEmployments, "obligations")
  private val connector = SelfEmploymentObligationsConnector

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def retrieveObligations(nino: Nino, id: SourceId): Action[AnyContent] = featureSwitch.asyncFeatureSwitch { implicit headers =>
    connector.get(nino, id).map { response =>
      response.status match {
        case 200 => Ok(response.json)
        case 400 => BadRequest(Error.from(response.json))
        case 404 => NotFound
        case _ => unhandledResponse(response.status, logger)
      }
    }
  }
}
