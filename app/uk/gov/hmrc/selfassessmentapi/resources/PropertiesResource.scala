/*
 * Copyright 2016 HM Revenue & Customs
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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{PropertiesAnnualSummary, PropertiesPeriod, PropertiesPeriodicData}
import uk.gov.hmrc.selfassessmentapi.services.PropertiesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesResource extends PeriodResource[PropertyId, PropertiesPeriod, Properties, PropertiesPeriodicData]
  with AnnualSummaryResource[PropertiesAnnualSummary, Properties] with BaseResource {

  override val context = AppContext.apiGatewayLinkContext
  override val sourceType = SourceType.Properties

  override implicit val periodFormat: Format[PropertiesPeriod] = Format(PropertiesPeriod.reads, PropertiesPeriod.writes)
  override implicit val periodicDataFormat: Format[PropertiesPeriodicData] = Format(PropertiesPeriodicData.reads, PropertiesPeriodicData.writes)
  override implicit val annualSummaryFormat: Format[PropertiesAnnualSummary] = Format(PropertiesAnnualSummary.reads, PropertiesAnnualSummary.writes)
  override val annualSummaryFeatureSwitch: FeatureSwitchAction = FeatureSwitchAction(sourceType)

  private val service = PropertiesService()
  override val periodService = service
  override val annualSummaryService = service

  def create(nino: Nino) = annualSummaryFeatureSwitch.asyncFeatureSwitch { request =>
    validate[properties.Properties, Either[Error, Boolean]](request.body) {
      service.create(nino, _)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case Right(successful) =>
          if (successful) Created.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/uk-properties")
          else InternalServerError
        case Left(err) => Conflict(Json.toJson(Errors.businessError(err)))
      }
    }
  }
}
