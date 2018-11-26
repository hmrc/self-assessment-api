/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.connectors.PropertiesBISSConnector
import uk.gov.hmrc.r2.selfassessmentapi.models.Errors.{NinoInvalid, NinoNotFound, NoSubmissionDataExists, ServerError, TaxYearInvalid, TaxYearNotFound}
import uk.gov.hmrc.r2.selfassessmentapi.models.{Errors, SourceType, TaxYear}
import uk.gov.hmrc.r2.selfassessmentapi.services.AuthorisationService

import scala.concurrent.ExecutionContext.Implicits.global

object PropertiesBISSResource extends PropertiesBISSResource {
  override val appContext: AppContext = AppContext
  override val authService: AuthorisationService = AuthorisationService
  override val propertiesBISSConnector = PropertiesBISSConnector
}

trait PropertiesBISSResource extends BaseResource {
  val appContext: AppContext
  val authService: AuthorisationService
  val propertiesBISSConnector: PropertiesBISSConnector

  def getSummary(nino: Nino, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("BISS")).async {
      implicit request =>
        propertiesBISSConnector.getSummary(nino, taxYear).map {
          case Left(error) => error.error match {
            case NinoInvalid | TaxYearInvalid => BadRequest(toJson(error))
            case NinoNotFound | TaxYearNotFound | NoSubmissionDataExists => NotFound(toJson(error))
            case ServerError => InternalServerError(toJson(error))
            case Errors.ServiceUnavailable => ServiceUnavailable(toJson(error))
            case Errors.InvalidRequest => BadRequest(toJson(error))
          }
          case Right(response) => Ok(toJson(response))
        }
    }
}
