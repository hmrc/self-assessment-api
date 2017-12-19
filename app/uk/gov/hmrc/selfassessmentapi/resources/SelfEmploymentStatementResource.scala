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

import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.{Declaration, Errors, SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.connectors.{ObligationsConnector, SelfEmploymentStatementConnector}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{EmptyResponse, Response}
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.services.AuditData
import uk.gov.hmrc.selfassessmentapi.services.AuditService.audit
import uk.gov.hmrc.selfassessmentapi.models.audit.EndOfPeriodStatementDeclaration
import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.models.Period
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams

import scala.concurrent.ExecutionContext.Implicits._
import cats.implicits._

object SelfEmploymentStatementResource extends BaseResource {

  private val statementConnector = SelfEmploymentStatementConnector
  private val obligationsConnector = ObligationsConnector

  def finaliseEndOfPeriodStatement(nino: Nino, id: SourceId, start: LocalDate, end: LocalDate): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments).async(parse.json) { implicit request =>

      val accountingPeriod = Period(start, end)

      def handleSuccess(desResponse: EmptyResponse) = {

        def businessJsonError(error: Errors.Error) = Json.toJson(Errors.businessError(error))

        audit(buildAuditEvent(nino, id, accountingPeriod, request.authContext, desResponse))
        desResponse.filter {
          case 204                                                     => NoContent
          case 403 if desResponse.errorCodeIs(PERIODIC_UPDATE_MISSING) => Forbidden(businessJsonError(Errors.PeriodicUpdateMissing))
          case 403 if desResponse.errorCodeIs(NON_MATCHING_PERIOD)     => Forbidden(businessJsonError(Errors.NonMatchingPeriod))
        }

      }

      val now = new LocalDate()

      BusinessResult.desToResult(handleSuccess) {
        for {

          _ <- validate(accountingPeriod) {
            case _ if(!accountingPeriod.valid)          => Errors.InvalidDateRange
            case _ if(accountingPeriod.to.isAfter(now)) => Errors.EarlySubmission
          }

          declaration <- validateJson[Declaration](request.body)

          _ <- authorise(declaration) { case _ if (!declaration.finalised) => Errors.NotFinalisedDeclaration }

          desResponse <- execute[EmptyResponse] { _ => statementConnector.create(nino, id, accountingPeriod) }
        } yield desResponse
      }

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

  def retrieveObligationsById(nino: Nino, id: SourceId, params: ObligationQueryParams): Action[Unit] =
  APIAction(nino, SourceType.SelfEmployments, Some("statements")).async(parse.empty) { implicit request =>
    obligationsConnector.get(nino).map { response =>
      response.filter {
        case 200 =>
          logger.debug("Self-employment statements from DES = " + Json.stringify(response.json))
          // ITEOY is made up for now, it needs to be agreed with the des folks!
          response.obligations("ITSBEOPS", Some(id)) match {
            case Right(obj) => obj.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
            case Left(ex) =>
              logger.error(ex.msg)
              InternalServerError(Json.toJson(Errors.InternalServerError))
          }
      }
    }
  }

}
