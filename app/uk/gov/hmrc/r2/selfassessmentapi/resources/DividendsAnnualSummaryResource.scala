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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.contexts.FilingOnlyAgent
import uk.gov.hmrc.r2.selfassessmentapi.models.dividends.Dividends
import uk.gov.hmrc.r2.selfassessmentapi.models.{Errors, SourceType, TaxYear}
import uk.gov.hmrc.r2.selfassessmentapi.services.{AuthorisationService, DividendsAnnualSummaryService}
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext

object DividendsAnnualSummaryResource extends DividendsAnnualSummaryResource {
  val appContext = AppContext
  val authService = AuthorisationService
  val dividendsService = DividendsAnnualSummaryService
}

trait DividendsAnnualSummaryResource extends BaseResource {
  val appContext: AppContext
  val authService: AuthorisationService
  val dividendsService: DividendsAnnualSummaryService

  def updateAnnualSummary(nino: Nino, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.Dividends, Some("annual")).async(parse.json) { implicit request =>
      validate[Dividends, Boolean](request.body) { dividends =>
        dividendsService.updateAnnualSummary(nino, taxYear, dividends)
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(true) => NoContent
        case _ => InternalServerError
      } recoverWith exceptionHandling
    }

  def retrieveAnnualSummary(nino: Nino, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Dividends, Some("annual")).async { implicit request =>
      dividendsService.retrieveAnnualSummary(nino, taxYear).map {
        case Some(summary) => Ok(Json.toJson(summary))
        case None if request.authContext == FilingOnlyAgent => BadRequest(Json.toJson(Errors.InvalidRequest))
        case _ => NotFound
      } recoverWith exceptionHandling
    }
}
