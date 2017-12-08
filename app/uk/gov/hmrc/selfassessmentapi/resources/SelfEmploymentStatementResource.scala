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

import play.api.mvc._
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.{SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentStatementConnector
import uk.gov.hmrc.selfassessmentapi.models.{Declaration, Errors}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.EmptyResponse
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.services.AuditData
import uk.gov.hmrc.selfassessmentapi.services.AuditService.audit
import uk.gov.hmrc.selfassessmentapi.models.audit.EndOfPeriodStatementDeclaration

import scala.concurrent.ExecutionContext.Implicits._

object SelfEmploymentStatementResource extends BaseResource {

  private val connector = SelfEmploymentStatementConnector

  def finaliseEndOfPeriodStatement(nino: Nino, id: SourceId, year: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments).async(parse.json) { implicit request =>

      def isAuthorised(declaration: Declaration) =
        if (!declaration.finalised) Some(Errors.NotFinalisedDeclaration)
        else None

      def handleSuccess[A](response: EmptyResponse) = {
        audit(buildAuditEvent(nino, id, year, request.authContext, response))
        response.filter {
          case 204                                                  => NoContent
          case 403 if response.errorCodeIs(PERIODIC_UPDATE_MISSING) => Forbidden(Json.toJson(Errors.businessError(Errors.PeriodicUpdateMissing)))
          case 403 if response.errorCodeIs(ALREADY_FINALISED)       => Forbidden(Json.toJson(Errors.businessError(Errors.AlreadyFinalised)))
        }
      }

      for {
        declaration <- validate[Declaration](request.body)
        result <- authorise[Declaration, EmptyResponse](declaration, isAuthorised) { _ =>
          connector.create(nino, id, year)
        }
      } yield result match {
        case Left(errors)    => handleErrors(errors)
        case Right(response) => handleSuccess(response)
      }
    }

  private def buildAuditEvent(nino: Nino,
                                         id: SourceId,
                                         taxYear: TaxYear,
                                         authCtx: AuthContext,
                                         response: EmptyResponse)(
      implicit hc: HeaderCarrier,
      request: Request[JsValue]): AuditData[EndOfPeriodStatementDeclaration] =
        AuditData(
          detail = EndOfPeriodStatementDeclaration(
            httpStatus = response.status,
            nino = nino,
            sourceId = id.toString,
            taxYear = taxYear,
            affinityGroup = authCtx.affinityGroup,
            agentCode = authCtx.agentCode
          ),
          transactionName = s"$id-end-of-year-statement-finalised"
        )

}
