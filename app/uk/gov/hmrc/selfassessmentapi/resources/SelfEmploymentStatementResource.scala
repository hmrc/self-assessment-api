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

import cats.implicits._
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentStatementConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.audit.EndOfPeriodStatementDeclaration
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.models.{Declaration, Errors, Period, SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.utils.EopsObligationQueryParams
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{EmptyResponse, Response}
import uk.gov.hmrc.selfassessmentapi.services.AuditService.audit
import uk.gov.hmrc.selfassessmentapi.services.{AuditData, AuthorisationService}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

object SelfEmploymentStatementResource extends BaseResource {
  val appContext = AppContext
  val authService = AuthorisationService
  private val statementConnector = SelfEmploymentStatementConnector

  def finaliseEndOfPeriodStatement(nino: Nino, id: SourceId, start: LocalDate, end: LocalDate): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments).async(parse.json) { implicit request =>

      val accountingPeriod = Period(start, end)
      val fromDateCutOff = new LocalDate(2017, 4, 6)
      val now = new LocalDate()

      {
        for {
          _ <- validate(accountingPeriod) {
            case _ if accountingPeriod.from.isBefore(fromDateCutOff)              => Errors.InvalidStartDate
            case _ if !accountingPeriod.valid                                     => Errors.InvalidDateRange
            case _ if !AppContext.sandboxMode & accountingPeriod.to.isAfter(now)  => Errors.EarlySubmission
          }

          declaration <- validateJson[Declaration](request.body)

          _ <- authorise(declaration) { case _ if (!declaration.finalised) => Errors.NotFinalisedDeclaration }

          desResponse <- execute[EmptyResponse] { _ => statementConnector.create(nino, id, accountingPeriod, getRequestDateTimestamp) }
        } yield desResponse
      } onDesSuccess { desResponse =>

        def businessJsonError(error: Errors.Error) = Json.toJson(Errors.businessError(error))

        audit(buildAuditEvent(nino, id, accountingPeriod, request.authContext, desResponse))
        desResponse.filter {
          case 204                                                     => NoContent
          case 400 if desResponse.errorCodeIs(EARLY_SUBMISSION)        => Forbidden(Json.toJson(Errors.EarlySubmission))
          case 403 if desResponse.errorCodeIs(PERIODIC_UPDATE_MISSING) => Forbidden(businessJsonError(Errors.PeriodicUpdateMissing))
          case 403 if desResponse.errorCodeIs(NON_MATCHING_PERIOD)     => Forbidden(businessJsonError(Errors.NonMatchingPeriod))
          case 403 if desResponse.errorCodeIs(ALREADY_SUBMITTED)       => Forbidden(businessJsonError(Errors.AlreadySubmitted))
        }
      } recoverWith exceptionHandling
    }

  private def buildAuditEvent(
    nino: Nino,
    id: SourceId,
    accountingPeriod: Period,
    authCtx: AuthContext,
    response: Response
  )(
    implicit hc: HeaderCarrier,
    request: Request[JsValue]
  ): AuditData[EndOfPeriodStatementDeclaration] =
    AuditData(
      detail = EndOfPeriodStatementDeclaration(
        httpStatus = response.status,
        nino = nino,
        sourceId = id.toString,
        accountingPeriodId = accountingPeriod.periodId,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode
      ),
      transactionName = s"$id-end-of-year-statement-finalised"
    )

  def retrieveObligationsById(nino: Nino, id: SourceId, params: EopsObligationQueryParams): Action[Unit] =
  APIAction(nino, SourceType.SelfEmployments, Some("statements")).async(parse.empty) { implicit request =>
    val selfEmploymentPattern = "^[A-Za-z0-9]{15}$"
    if (id.matches(selfEmploymentPattern)) {
      statementConnector.get(nino, params).map { response =>
        response.filter {
          case 200 =>
            logger.debug("Self-employment statements from DES = " + Json.stringify(response.json))
            response.retrieveEOPSObligation(id) match {
              case Right(obj) =>
                obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
              case Left(ex) =>
                logger.error(ex.msg)
                InternalServerError(Json.toJson(Errors.InternalServerError))
            }
          case 400 if response.errorCodeIsOneOf(INVALID_STATUS, INVALID_REGIME, INVALID_IDTYPE) =>
            InternalServerError(Json.toJson(Errors.InternalServerError))
          case 400 if response.errorCodeIsOneOf(INVALID_DATE_TO, INVALID_DATE_FROM) =>
            BadRequest(Json.toJson(Errors.InvalidDate))
          case 400 if response.errorCodeIsOneOf(INVALID_DATE_RANGE) =>
            BadRequest(Json.toJson(Errors.InvalidDateRange_2))
          case 400 if response.errorCodeIsOneOf(INVALID_IDNUMBER) =>
            BadRequest(Json.toJson(Errors.NinoInvalid))
          case 403 if response.errorCodeIs(NOT_FOUND_BPKEY) =>
            NotFound
        }
      } recoverWith exceptionHandling
    } else {
      Future.successful(BadRequest(Json.toJson(Errors.SelfEmploymentIDInvalid)))
    }
  }
}
