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
import uk.gov.hmrc.selfassessmentapi.connectors.TaxCalculationConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.audit.{TaxCalculationRequest, TaxCalculationTrigger}
import uk.gov.hmrc.selfassessmentapi.models.calculation.CalculationRequest
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.TaxCalculationResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditData
import uk.gov.hmrc.selfassessmentapi.services.AuditService.audit

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.http.HeaderCarrier

object TaxCalculationResource extends BaseResource {
  private val connector = TaxCalculationConnector

  private val cannedEtaResponse =
    s"""
       |{
       |  "etaSeconds": 5
       |}
     """.stripMargin

  def requestCalculation(nino: Nino): Action[JsValue] =
    APIAction(nino, SourceType.Calculation).async(parse.json) { implicit request =>
      validate[CalculationRequest, TaxCalculationResponse](request.body) { req =>
        connector.requestCalculation(nino, req.taxYear)
      } map {
        case Left(errorResult) => handleValidationErrors(errorResult)
        case Right(response)   =>
          audit(makeTaxCalcTriggerAudit(nino, request.authContext, response))
          response.filter {
            case 202 =>
              Accepted(Json.parse(cannedEtaResponse))
                .withHeaders(
                  LOCATION -> response.calcId
                    .map(id => s"/self-assessment/ni/$nino/calculations/$id")
                    .getOrElse(""))
            case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
            case _                             => unhandledResponse(response.status, logger)
          }
      }
    }

  def retrieveCalculation(nino: Nino, calcId: SourceId): Action[Unit] =
    APIAction(nino, SourceType.Calculation).async(parse.empty) { implicit request =>
      connector.retrieveCalculation(nino, calcId).map { response =>
        audit(makeTaxCalcRequestAudit(nino, calcId, request.authContext, response))
        response.filter {
          case 200                                 => Ok(Json.toJson(response.calculation))
          case 204                                 => NoContent
          case 400 if response.isInvalidCalcId     => NotFound
          case 400 if response.isInvalidIdentifier => BadRequest(Json.toJson(Errors.NinoInvalid))
          case 404                                 => NotFound
          case _                                   => unhandledResponse(response.status, logger)
        }
      }
    }

  private def makeTaxCalcTriggerAudit(nino: Nino, authCtx: AuthContext, response: TaxCalculationResponse)(
      implicit hc: HeaderCarrier,
      request: Request[JsValue]): AuditData[TaxCalculationTrigger] =
    AuditData(
      detail = TaxCalculationTrigger(
        httpStatus = response.status,
        nino = nino,
        taxYear = request.body.as[CalculationRequest].taxYear,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        calculationId = response.status / 100 match {
          case 2 => Some(response.calcId.getOrElse(""))
          case _ => None
        },
        responsePayload = response.status match {
          case 202 | 400 => Some(response.json)
          case _         => None
        }
      ),
      transactionName = "trigger-tax-calculation"
    )

  private def makeTaxCalcRequestAudit(nino: Nino,
                                      calcId: String,
                                      authCtx: AuthContext,
                                      response: TaxCalculationResponse)(
      implicit hc: HeaderCarrier,
      request: Request[_]): AuditData[TaxCalculationRequest] =
    AuditData(
      detail = TaxCalculationRequest(
        httpStatus = response.status,
        nino = nino,
        calculationId = calcId,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        responsePayload = response.status match {
          case 200 | 400 => Some(response.json)
          case _         => None
        }
      ),
      transactionName = "retrieve-tax-calculation"
    )
}
