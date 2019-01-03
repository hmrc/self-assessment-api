/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.connectors.PropertiesPeriodStatementConnector
import uk.gov.hmrc.selfassessmentapi.models.des.DesErrorCode._
import uk.gov.hmrc.selfassessmentapi.models.{Declaration, Errors, Period, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.utils.ResourceHelper
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.EmptyResponse
import uk.gov.hmrc.selfassessmentapi.services.{AuditService, AuthorisationService}

import scala.concurrent.ExecutionContext.Implicits._

object PropertiesPeriodStatementResource extends PropertiesPeriodStatementResource{
  val appContext = AppContext
  val authService = AuthorisationService
  override lazy val statementConnector = PropertiesPeriodStatementConnector
  override lazy val auditService = AuditService
}

trait PropertiesPeriodStatementResource extends BaseResource {

  val statementConnector: PropertiesPeriodStatementConnector
  val auditService: AuditService

  def finaliseEndOfPeriodStatement(nino: Nino, start: LocalDate, end: LocalDate): Action[JsValue] =
    APIAction(nino, SourceType.Properties).async(parse.json) { implicit request =>

      val accountingPeriod = Period(start, end)

      {
        for {
          _ <- ResourceHelper.validatePeriodDates(accountingPeriod)
          declaration <- validateJson[Declaration](request.body)
          _ <- authorise(declaration) { case _ if (!declaration.finalised) => Errors.NotFinalisedDeclaration }

          desResponse <- execute[EmptyResponse] { _ => statementConnector.create(nino, accountingPeriod, getRequestDateTimestamp) }
        } yield desResponse
      } onDesSuccess { desResponse =>
        auditService.audit(ResourceHelper.buildAuditEvent(nino, "", accountingPeriod, request.authContext, desResponse))
        desResponse.filter {
          case 204                                                     => NoContent
          case 400 if desResponse.errorCodeIs(EARLY_SUBMISSION)        => Forbidden(Errors.businessJsonError(Errors.EarlySubmission))
          case 403 if desResponse.errorCodeIs(PERIODIC_UPDATE_MISSING) => Forbidden(Errors.businessJsonError(Errors.PeriodicUpdateMissing))
          case 403 if desResponse.errorCodeIs(NON_MATCHING_PERIOD)     => Forbidden(Errors.businessJsonError(Errors.NonMatchingPeriod))
          case 403 if desResponse.errorCodeIs(ALREADY_SUBMITTED)       => Forbidden(Errors.businessJsonError(Errors.AlreadySubmitted))
        }
      } recoverWith exceptionHandling
    }
}
