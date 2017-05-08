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
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.connectors.TaxCalculationConnector
import uk.gov.hmrc.selfassessmentapi.models.audit.{TaxCalculationRequest, TaxCalculationTrigger}
import uk.gov.hmrc.selfassessmentapi.models.calculation.CalculationRequest
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.TaxCalculationResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global

object TaxCalculationResource extends BaseResource {

  private lazy val FeatureSwitch = FeatureSwitchAction(SourceType.Calculation)
  private val connector = TaxCalculationConnector

  private val cannedEtaResponse =
    s"""
       |{
       |  "etaSeconds": 5
       |}
     """.stripMargin

  def requestCalculation(nino: Nino): Action[JsValue] =
    FeatureSwitch.async(parse.json) { implicit request =>
      withAuth(nino) { implicit context =>
        validate[CalculationRequest, TaxCalculationResponse](request.body) { req =>
          connector.requestCalculation(nino, req.taxYear)
        } map {
          case Left(errorResult) => handleValidationErrors(errorResult)
          case Right(response) =>
            response.filter {
              case 202 =>
                auditTaxCalcTrigger(nino, response)
                Accepted(Json.parse(cannedEtaResponse))
                  .withHeaders(
                    LOCATION -> response.calcId
                      .map(id => s"/self-assessment/ni/$nino/calculations/$id")
                      .getOrElse(""))
              case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
              case _ => unhandledResponse(response.status, logger)
            }
        }
      }
    }

  def retrieveCalculation(nino: Nino, calcId: SourceId): Action[Unit] =
    FeatureSwitch.async(parse.empty) { implicit request =>
      withAuth(nino) { implicit context =>
        connector.retrieveCalculation(nino, calcId).map { response =>
          response.filter {
            case 200 =>
              auditTaxCalcRequest(nino, calcId, response)
              Ok(Json.toJson(response.calculation))
            case 204 => NoContent
            case 400 if response.isInvalidCalcId => NotFound
            case 400 if response.isInvalidIdentifier => BadRequest(Json.toJson(Errors.NinoInvalid))
            case 404 => NotFound
            case _ => unhandledResponse(response.status, logger)
          }
        }
      }
    }

  private def auditTaxCalcTrigger(nino: Nino, response: TaxCalculationResponse)(implicit hc: HeaderCarrier,
                                                                                request: Request[JsValue]): Unit = {
    AuditService.audit(payload = TaxCalculationTrigger(nino,
                                                       request.body.as[CalculationRequest].taxYear,
                                                       response.calcId.getOrElse("")),
                       "trigger-tax-calculation")
  }

  private def auditTaxCalcRequest(nino: Nino, calcId: String, response: TaxCalculationResponse)(
      implicit hc: HeaderCarrier,
      request: Request[_]): Unit = {
    AuditService.audit(payload = TaxCalculationRequest(nino, calcId, response.json), "retrieve-tax-calculation")
  }
}
