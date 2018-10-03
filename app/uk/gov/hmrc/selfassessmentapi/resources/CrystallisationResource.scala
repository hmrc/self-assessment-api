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
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.CrystallisationConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.audit.{IntentToCrystallise, SubmitCrystallisation}
import uk.gov.hmrc.selfassessmentapi.models.crystallisation.CrystallisationRequest
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{CrystallisationIntentResponse, EmptyResponse}
import uk.gov.hmrc.selfassessmentapi.services.AuditService.extendedAudit
import uk.gov.hmrc.selfassessmentapi.services.{AuthorisationService, ExtendedAuditData}

object CrystallisationResource extends CrystallisationResource {
  override val appContext = AppContext
  override val authService = AuthorisationService
  override val crystallisationConnector = CrystallisationConnector
}

trait CrystallisationResource extends BaseResource {
  val appContext: AppContext
  val authService: AuthorisationService
  val crystallisationConnector: CrystallisationConnector

  def intentToCrystallise(nino: Nino, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.Crystallisation).async { implicit request =>
      crystallisationConnector.intentToCrystallise(nino, taxYear) map { response =>
        response.filter {
          case 200 =>
            extendedAudit(makeIntentToCrystalliseAudit(nino, taxYear, request.authContext, response))
            val contextPrefix = AppContext.selfAssessmentContextRoute
            val url = response.calculationId.map(id => s"$contextPrefix/ni/$nino/calculations/$id").getOrElse("")
            SeeOther(url).withHeaders(LOCATION -> url)
          case 400 if response.errorCodeIs(INVALID_TAX_CRYSTALLISE) =>
            InternalServerError(toJson(Errors.InternalServerError))
          case 400 if response.errorCodeIs(INVALID_REQUEST) =>
            Forbidden(toJson(Errors.businessError(Errors.RequiredEndOfPeriodStatement)))
        }
      } recoverWith exceptionHandling
    }

  def crystallisation(nino: Nino, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.Crystallisation).async(parse.json) { implicit request =>
      validate[CrystallisationRequest, EmptyResponse](request.body) {
         crystallisationConnector.crystallise(nino, taxYear, _)
      } map {
        case Left(error) => handleErrors(error)
        case Right(response) => response.filter {
          case 204 =>
            extendedAudit(makeSubmitCrystallisationAudit(nino, taxYear, request.authContext, response))
            Created
          case 400 if response.errorCodeIsOneOf(INVALID_TAXYEAR) =>
            BadRequest(Json.toJson(Errors.TaxYearInvalid))
          case 400 if response.errorCodeIsOneOf(INVALID_CALCID) =>
            Forbidden(Json.toJson(Errors.businessError(Errors.InvalidTaxCalculationId)))
          case 404 =>
            NotFound
          case 409 => Forbidden(Json.toJson(Errors.businessError(Errors.RequiredIntentToCrystallise)))
        }
      } recoverWith exceptionHandling
    }

  def retrieveObligation(nino: Nino, taxYear: TaxYear, queryParams: ObligationQueryParams): Action[AnyContent] =
    APIAction(nino, SourceType.Crystallisation).async { implicit request =>
      crystallisationConnector.get(nino, queryParams.copy(from = Some(taxYear.taxYearFromDate), to = Some(taxYear.taxYearToDate))) map { response =>
        response.filter {
          case 200 =>
            response.obligations("ITSA", nino, taxYear.taxYearFromDate) match {
              case Right(obj) => obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
              case Left(ex) =>
                logger.warn(ex.msg)
                InternalServerError(Json.toJson(Errors.InternalServerError))
            }
        }
      } recoverWith exceptionHandling
    }


  private def makeIntentToCrystalliseAudit(nino: Nino, taxYear: TaxYear,
                                           authCtx: AuthContext, response: CrystallisationIntentResponse)(
    implicit hc: HeaderCarrier): ExtendedAuditData[IntentToCrystallise] =
    ExtendedAuditData(
      detail = IntentToCrystallise(
        nino = nino,
        taxYear = taxYear.toDesTaxYear,
        userType = authCtx.affinityGroup,
        agentReferenceNumber = authCtx.agentCode,
        calculationId = response.calculationId.getOrElse(""),
        `X-CorrelationId` = response.underlying.header("CorrelationId")
      ),
      transactionName = "intent-to-crystallise",
      auditType = "submitIntentToCrystallise"
    )

  private def makeSubmitCrystallisationAudit(nino: Nino, taxYear: TaxYear,
                                           authCtx: AuthContext, response: EmptyResponse)(
    implicit hc: HeaderCarrier,
    requestJson: Request[JsValue]): ExtendedAuditData[SubmitCrystallisation] =
    ExtendedAuditData(
      detail = SubmitCrystallisation(
        nino = nino,
        taxYear = taxYear.toDesTaxYear,
        userType = authCtx.affinityGroup,
        agentReferenceNumber = authCtx.agentCode,
        request = requestJson.body,
        `X-CorrelationId` = response.underlying.header("CorrelationId")
      ),
      transactionName = "crystallisation",
      auditType = "submitCrystallisation"
    )
}
