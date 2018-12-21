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

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.TaxCalculationConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.audit.TaxCalculationTrigger
import uk.gov.hmrc.selfassessmentapi.models.calculation.CalculationRequest
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.TaxCalculationResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditService.audit
import uk.gov.hmrc.selfassessmentapi.services.{AuditData, AuthorisationService}

import scala.concurrent.Future

object TaxCalculationResource extends BaseResource {
  val appContext = AppContext
  val authService = AuthorisationService
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
        case Left(errorResult) => handleErrors(errorResult)
        case Right(response)   =>
          audit(makeTaxCalcTriggerAudit(nino, request.authContext, response))
          response.filter {
            case 200 =>
              Accepted(Json.parse(cannedEtaResponse))
                .withHeaders(
                  LOCATION -> response.calcId
                    .map(id => s"/self-assessment/ni/$nino/calculations/$id")
                    .getOrElse(""))
            case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
            case 400 if response.isInvalidRequest =>
              logger.warn("[TaxCalculationResource] [requestCalculation] DES returned INVALID_REQUEST. This could be due to;" +
                "\n1. No valid income sources at backend\n2. No income submissions exist at backend")
              unhandledResponse(response.status, logger)
            case _                             => unhandledResponse(response.status, logger)
          }
      } recoverWith exceptionHandling
    }

  def retrieveCalculation(nino: Nino, calcId: SourceId): Action[Unit] =
    APIAction(nino, SourceType.Calculation).async(parse.empty) { implicit request =>
      Future.successful(Gone(Json.toJson(Errors.TaxCalcGone)))
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
}
