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
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.contexts.FilingOnlyAgent
import uk.gov.hmrc.selfassessmentapi.models.banks.BankAnnualSummary
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceId, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.{AuthorisationService, BanksAnnualSummaryService}
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.selfassessmentapi.config.AppContext


object BanksAnnualSummaryResource extends BanksAnnualSummaryResource {
  val annualSummaryService = BanksAnnualSummaryService
  val appContext = AppContext
  val authService = AuthorisationService
}

trait BanksAnnualSummaryResource extends BaseResource {
  val annualSummaryService: BanksAnnualSummaryService

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.Banks, Some("annual")).async(parse.json) { implicit request =>
      validate[BankAnnualSummary, Boolean](request.body) {
        annualSummaryService.updateAnnualSummary(nino, id, taxYear, _)
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(true) => NoContent
        case Right(false) if request.authContext == FilingOnlyAgent => BadRequest(Json.toJson(Errors.InvalidRequest))
        case _ => NotFound
      } recoverWith exceptionHandling
    }

  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Banks, Some("annual")).async { implicit request =>
      annualSummaryService.retrieveAnnualSummary(nino, id, taxYear).map {
        case Some(summary) => Ok(Json.toJson(summary))
        case None if request.authContext == FilingOnlyAgent => BadRequest(Json.toJson(Errors.InvalidRequest))
        case None => NotFound
      } recoverWith exceptionHandling
    }
}
