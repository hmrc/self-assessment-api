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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.httpparsers.NoSubmissionDataExists
import uk.gov.hmrc.selfassessmentapi.models.Errors.{NinoInvalid, NinoNotFound, ServerError, TaxYearInvalid, TaxYearNotFound}
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.{AuthorisationService, PropertiesBISSService}
import play.api.libs.json.Json.toJson

import scala.concurrent.ExecutionContext.Implicits.global

object PropertiesBISSResource extends PropertiesBISSResource {
  override val appContext: AppContext = AppContext
  override val authService: AuthorisationService = AuthorisationService
  override val propertiesBISSService: PropertiesBISSService = PropertiesBISSService
}

trait PropertiesBISSResource extends BaseResource {
  val appContext: AppContext
  val authService: AuthorisationService
  val propertiesBISSService: PropertiesBISSService

  def getSummary(nino: Nino, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("BISS")).async {
      implicit request =>
        propertiesBISSService.getSummary(nino, taxYear).map {
          case Left(error) => error match {
            case NinoInvalid | TaxYearInvalid => BadRequest(toJson(error))
            case NinoNotFound | TaxYearNotFound | NoSubmissionDataExists => NotFound(toJson(error))
            case ServerError => InternalServerError(toJson(error))
            case Errors.ServiceUnavailable => ServiceUnavailable(toJson(error))
          }
          case Right(response) => Ok(toJson(response))
        }
    }
}
