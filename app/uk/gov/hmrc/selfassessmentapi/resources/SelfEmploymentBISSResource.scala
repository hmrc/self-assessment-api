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
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentBISSConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.{NinoInvalid, NinoNotFound, NoSubmissionDataExists, SelfEmploymentIDNotFound, ServerError, TaxYearInvalid, TaxYearNotFound}
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.AuthorisationService

import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentBISSResource extends SelfEmploymentBISSResource {
  override val connector: SelfEmploymentBISSConnector = SelfEmploymentBISSConnector
  override val appContext = AppContext
  override val authService = AuthorisationService
}

trait SelfEmploymentBISSResource extends BaseResource {
  val appContext: AppContext
  val authService: AuthorisationService
  val connector : SelfEmploymentBISSConnector

  def getSummary(nino: Nino, taxYear: TaxYear, id: String): Action[AnyContent] =
  APIAction(nino, SourceType.SelfEmployments, Some("BISS")).async {
    implicit request =>
      connector.getSummary(nino, taxYear, id).map{
        case Left(error) => error match {
          case NinoInvalid | TaxYearInvalid => BadRequest(toJson(error))
          case NinoNotFound | TaxYearNotFound | NoSubmissionDataExists => NotFound(toJson(error))
          case ServerError | Errors.ServiceUnavailable => InternalServerError(toJson(error))
          case SelfEmploymentIDNotFound => BadRequest(toJson(error))
        }
        case Right(response) => Ok(toJson(response))
      }
  }
}
