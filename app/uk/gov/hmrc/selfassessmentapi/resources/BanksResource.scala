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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.contexts.FilingOnlyAgent
import uk.gov.hmrc.selfassessmentapi.models.banks.Bank
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.services.{AuthorisationService, BanksService}

import scala.concurrent.ExecutionContext.Implicits._

object BanksResource extends BaseResource {
  val appContext = AppContext
  private val service = BanksService
  val authService = AuthorisationService

  def create(nino: Nino): Action[JsValue] =
    APIAction(nino, SourceType.Banks).async(parse.json) { implicit request =>
      validate[Bank, Option[SourceId]](request.body) { bank =>
        service.create(nino, bank)
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(Some(id)) => Created.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/savings-accounts/$id")
        case Right(None) => InternalServerError
      }
    }

  def update(nino: Nino, id: SourceId): Action[JsValue] =
    APIAction(nino, SourceType.Banks).async(parse.json) { implicit request =>
      validate[Bank, Boolean](request.body) { bank =>
        service.update(nino, bank, id)
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(true) => NoContent
        case Right(false) if request.authContext == FilingOnlyAgent => BadRequest(Json.toJson(Errors.InvalidRequest))
        case _ => NotFound
      }
    }

  def retrieve(nino: Nino, id: SourceId): Action[AnyContent] =
    APIAction(nino, SourceType.Banks).async { implicit request =>
      service.retrieve(nino, id) map {
        case Some(bank) => Ok(Json.toJson(bank))
        case None if request.authContext == FilingOnlyAgent => BadRequest(Json.toJson(Errors.InvalidRequest))
        case None => NotFound
      }
    }

  def retrieveAll(nino: Nino): Action[AnyContent] =
    APIAction(nino, SourceType.Banks).async { implicit request =>
      service.retrieveAll(nino) map { seq =>
        Ok(Json.toJson(seq))
      }
    }
}
