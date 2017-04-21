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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary, PropertiesAnnualSummary, PropertyType}
import uk.gov.hmrc.selfassessmentapi.models.{SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.services.PropertiesAnnualSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesAnnualSummaryResource extends BaseResource {
  private lazy val featureSwitch = FeatureSwitchAction(SourceType.Properties, "annual")
  private val service = PropertiesAnnualSummaryService

  def updateAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[JsValue] = featureSwitch.asyncJsonFeatureSwitch { implicit request =>
    withAuth(nino) {
      validateProperty(propertyId, request.body, service.updateAnnualSummary(nino, taxYear, _)) match {
        case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
        case Right(result) => result.map {
          case true => NoContent
          case false => NotFound
        }
      }
    }
  }

  def retrieveAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[AnyContent] = featureSwitch.asyncFeatureSwitch { implicit headers =>
    withAuth(nino) {
      service.retrieveAnnualSummary(nino, propertyId, taxYear).map {
        case Some(summary @ OtherPropertiesAnnualSummary(_, _)) => Ok(Json.toJson(summary))
        case Some(summary @ FHLPropertiesAnnualSummary(_, _)) => Ok(Json.toJson(summary))
        case None => NotFound
      }
    }
  }

  private def validateProperty(propertyId: PropertyType, body: JsValue, f: PropertiesAnnualSummary => Future[Boolean]) = {
    val validationFunc = propertyId match {
      case PropertyType.OTHER => validate[OtherPropertiesAnnualSummary, Boolean](body) _
      case PropertyType.FHL => validate[FHLPropertiesAnnualSummary, Boolean](body) _
    }

    validationFunc(f(_))
  }
}
