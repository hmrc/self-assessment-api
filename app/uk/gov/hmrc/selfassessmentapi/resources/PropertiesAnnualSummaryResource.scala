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
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesAnnualSummaryConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties.{
  FHLPropertiesAnnualSummary,
  OtherPropertiesAnnualSummary,
  PropertiesAnnualSummary,
  PropertyType
}
import uk.gov.hmrc.selfassessmentapi.models.{SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesAnnualSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesAnnualSummaryResource extends BaseResource {
  private lazy val FeatureSwitch = FeatureSwitchAction(SourceType.Properties, "annual")
  private val connector = PropertiesAnnualSummaryConnector

  def updateAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[JsValue] = FeatureSwitch.async(parse.json) { implicit request =>
    withAuth(nino) {
      validateProperty(propertyId, request.body, connector.update(nino, propertyId,taxYear, _)) match {
        case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
        case Right(result) => result.map {response =>
              response.status match {
          case 200 => NoContent
          case 404 => NotFound
          case 400 => BadRequest(Error.from(response.json))
                case _ => unhandledResponse(response.status, logger)
              }
        }
      }
    }
  }

  def retrieveAnnualSummary(nino: Nino, propertyId: PropertyType, taxYear: TaxYear): Action[AnyContent] = FeatureSwitch.async { implicit request =>
    withAuth(nino) {
      connector.get(nino, propertyId, taxYear).map {response =>
          response.status match {
            case 200 =>
              response.annualSummary match {
        case Some(summary ) => summary match {
                    case other: OtherPropertiesAnnualSummary => Ok(Json.toJson(other))
        case fhl: FHLPropertiesAnnualSummary => Ok(Json.toJson(fhl))
                  }
        case None => NotFound}
            case 404 => NotFound
            case 400 => BadRequest(Error.from(response.json))
            case _ => unhandledResponse(response.status, logger)
          }
      }
    }
  }

  private def validateProperty(propertyId: PropertyType,
                               body: JsValue,
                               f: PropertiesAnnualSummary => Future[PropertiesAnnualSummaryResponse]) = {
    val validationFunc = propertyId match {
      case PropertyType.OTHER => validate[OtherPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](body) _
      case PropertyType.FHL => validate[FHLPropertiesAnnualSummary, PropertiesAnnualSummaryResponse](body) _
    }

    validationFunc(f(_))
  }
}
