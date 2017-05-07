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
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentAnnualSummaryConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentAnnualSummary
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentAnnualSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentAnnualSummaryResource extends BaseResource {
  private lazy val FeatureSwitch = FeatureSwitchAction(SourceType.SelfEmployments, "annual")
  private val connector = SelfEmploymentAnnualSummaryConnector

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[JsValue] =
    FeatureSwitch.async(parse.json) { implicit request =>
      withAuth(nino) { implicit context =>
        validate[SelfEmploymentAnnualSummary, SelfEmploymentAnnualSummaryResponse](request.body) { summary =>
          connector.update(nino, id, taxYear, des.SelfEmploymentAnnualSummary.from(summary))
        } map {
          case Left(errorResult) => handleValidationErrors(errorResult)
          case Right(response) =>
            response.filter {
              case 200 => NoContent
              case 400 => BadRequest(Error.from(response.json))
              case 404 => NotFound
              case _ => unhandledResponse(response.status, logger)
            }
        }
      }
    }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[Unit] =
    FeatureSwitch.async(parse.empty) { implicit request =>
      withAuth(nino) { implicit context =>
        connector.get(nino, id, taxYear).map { response =>
          response.filter {
            case 200 => response.annualSummary.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
            case 404 => NotFound
            case _ => unhandledResponse(response.status, logger)
          }
        }
      }
    }
}
