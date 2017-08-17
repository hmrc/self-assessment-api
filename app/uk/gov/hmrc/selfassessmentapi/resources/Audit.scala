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

import play.api.mvc.Request
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.SourceId
import uk.gov.hmrc.selfassessmentapi.models.audit.RetrieveObligations
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.ObligationsResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditData

sealed trait RetrieveObligationTransaction

case object UkPropertiesRetrieveObligations extends RetrieveObligationTransaction {
  override def toString: String = "uk-properties-retrieve-obligations"
}

case object SelfEmploymentRetrieveObligations extends RetrieveObligationTransaction {
  override def toString: String = "self-employment-retrieve-obligations"
}

object Audit {
  def makeObligationsRetrievalAudit(nino: Nino,
                                    id: Option[SourceId] = None,
                                    authCtx: AuthContext,
                                    response: ObligationsResponse,
                                    transaction: RetrieveObligationTransaction)(
      implicit hc: HeaderCarrier,
      request: Request[_]): AuditData[RetrieveObligations] =
    AuditData(
      detail = RetrieveObligations(
        httpStatus = response.status,
        nino = nino,
        sourceId = id,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        responsePayload = response.status match {
          case 200 | 400 => Some(response.json)
          case _         => None
        }
      ),
      transactionName = transaction.toString
    )
}
