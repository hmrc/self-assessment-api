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

package uk.gov.hmrc.selfassessmentapi.services

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Format, Json}
import play.api.mvc.Request
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceAuditConnector
import uk.gov.hmrc.selfassessmentapi.models.audit.AuditPayload
import uk.gov.hmrc.selfassessmentapi.models.audit.AuditType.AuditType

import scala.concurrent.ExecutionContext.Implicits.global

trait AuditService {
  val auditConnector: AuditConnector

  def audit[T <: AuditPayload](payload: T, transactionName: String)
                              (implicit hc: HeaderCarrier, fmt: Format[T], request: Request[_]): Unit = {
    auditConnector.sendEvent(
      ExtendedDataEvent(
        auditSource = "self-assessment-api",
        auditType = payload.auditType.toString,
        tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, request.path),
        detail = Json.toJson(payload),
        generatedAt = DateTime.now(DateTimeZone.UTC))
    )
  }
}

object AuditService extends AuditService {
  override lazy val auditConnector = MicroserviceAuditConnector
}
