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

package uk.gov.hmrc.selfassessmentapi.resources.utils

import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.JsValue
import play.api.mvc.Request
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.{Errors, Period, SourceId}
import uk.gov.hmrc.selfassessmentapi.models.audit.EndOfPeriodStatementDeclaration
import uk.gov.hmrc.selfassessmentapi.models.giftaid.GiftAidPayments
import uk.gov.hmrc.selfassessmentapi.resources.validate
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.Response
import uk.gov.hmrc.selfassessmentapi.services.AuditData

object ResourceHelper {

  def buildAuditEvent(nino: Nino,
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
      transactionName = s"$nino-uk-property-end-of-year-statement-finalised"
    )

  def validatePeriodDates(accountingPeriod: Period) = {

    val fromDateCutOff: LocalDate = LocalDate.parse(AppContext.mtdDate, ISODateTimeFormat.date())
    val now = new LocalDate()

    validate(accountingPeriod) {
      case _ if accountingPeriod.from.isBefore(fromDateCutOff)              => Errors.InvalidStartDate
      case _ if !accountingPeriod.valid                                     => Errors.InvalidDateRange
      case _ if !AppContext.sandboxMode & accountingPeriod.to.isAfter(now)  => Errors.EarlySubmission
    }
  }
}
