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
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.banks.BankAnnualSummary
import uk.gov.hmrc.selfassessmentapi.models.{SourceId, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.BanksAnnualSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object BanksAnnualSummaryResource extends BaseResource {
  private lazy val featureSwitch = FeatureSwitchAction(SourceType.Banks, "annual")
  private val annualSummaryService = BanksAnnualSummaryService
  override val logger: Logger = Logger(BanksAnnualSummaryResource.getClass)

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[JsValue] = featureSwitch.asyncJsonFeatureSwitch { implicit request =>
    authorise(nino) {
      validate[BankAnnualSummary, Boolean](request.body) {
        annualSummaryService.updateAnnualSummary(nino, id, taxYear, _)
      } match {
        case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
        case Right(result) => result.map {
          case true => NoContent
          case false => NotFound
        }
      }
    }
  }

  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[AnyContent] = featureSwitch.asyncFeatureSwitch { implicit headers =>
    authorise(nino) {
      annualSummaryService.retrieveAnnualSummary(nino, id, taxYear).map {
        case Some(summary) => Ok(Json.toJson(summary))
        case None => NotFound
      }
    }
  }
}
