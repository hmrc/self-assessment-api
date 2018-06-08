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

import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.Errors.{NinoInvalid, NinoNotFound, NoSubmissionDataExists, SelfEmploymentIDInvalid, SelfEmploymentIDNotFound, ServerError, TaxYearInvalid, TaxYearNotFound}
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.{AuthorisationService, SelfEmploymentBISSService}

import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentBISSResource extends SelfEmploymentBISSResource {
  override val service: SelfEmploymentBISSService = SelfEmploymentBISSService
  override val appContext = AppContext
  override val authService = AuthorisationService
}

trait SelfEmploymentBISSResource extends BaseResource {
  val appContext: AppContext
  val authService: AuthorisationService
  val service : SelfEmploymentBISSService

  def getSummary(nino: Nino, taxYear: TaxYear, selfEmploymentId: String): Action[AnyContent] =
  APIAction(nino, SourceType.SelfEmployments, Some("BISS")).async {
    implicit request =>
      logger.debug(s"[SelfEmploymentBISSResource][getSummary] Get BISS for NI number : $nino with selfEmploymentId: $selfEmploymentId")
      service.getSummary(nino, taxYear, selfEmploymentId).map{
        case Left(error) => error match {
          case NinoInvalid | TaxYearInvalid | SelfEmploymentIDInvalid => BadRequest(toJson(error))
          case NinoNotFound | TaxYearNotFound | NoSubmissionDataExists | SelfEmploymentIDNotFound => NotFound(toJson(error))
          case ServerError | Errors.ServiceUnavailable => InternalServerError(toJson(error))
        }
        case Right(response) => Ok(toJson(response))
      }
  }
}
